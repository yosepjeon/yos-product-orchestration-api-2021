package com.yosep.product.product.service;

import com.yosep.product.jpa.category.data.entity.Category;
import com.yosep.product.jpa.category.data.repository.CategoryRepository;
import com.yosep.product.jpa.common.exception.InvalidStockValueException;
import com.yosep.product.jpa.common.exception.NotEqualProductPrice;
import com.yosep.product.jpa.common.exception.NotExistCategoryException;
import com.yosep.product.jpa.common.exception.NotExistElementException;
import com.yosep.product.jpa.common.logic.RandomIdGenerator;
import com.yosep.product.jpa.product.data.dto.CreatedProductDto;
import com.yosep.product.jpa.product.data.dto.request.OrderProductDtoForCreation;
import com.yosep.product.jpa.product.data.dto.request.ProductDtoForCreation;
import com.yosep.product.jpa.product.data.dto.request.ProductStepDtoForCreation;
import com.yosep.product.jpa.product.data.entity.Product;
import com.yosep.product.jpa.product.data.entity.ProductEvent;
import com.yosep.product.jpa.product.data.event.RevertProductStepEvent;
import com.yosep.product.jpa.product.data.mapper.product.ProductMapper;
import com.yosep.product.jpa.product.data.repository.ProductEventRepository;
import com.yosep.product.jpa.product.data.repository.ProductRepository;
import com.yosep.product.jpa.product.data.vo.EventId;
import com.yosep.product.jpa.product.data.vo.EventType;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductEventRepository productEventRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    public boolean checkIsPresentProduct(String productId) {
        return productRepository.findById(productId).isPresent();
    }

    public void getProduct() {
        
    }

    public void getProductListByPage() {

    }

    /*
     * ?????? ??????
     * Logic:
     * 1. productId??? ???????????? ?????? ID??? ????????? ????????? ??????
     * 1-1. ?????? ??????????????? 1??? ??????.
     * 1-2. ????????? ???????????? ???????????? 2????????? ??????.
     * 2. ?????? ?????? ??? ????????? ?????? ????????? ??????
     */
    @Transactional(readOnly = false)
    public CreatedProductDto createProduct(ProductDtoForCreation productDtoForCreation) {
        String productId = RandomIdGenerator.createId();

        Optional<Category> optionalCategory = categoryRepository.findById(productDtoForCreation.getCategory());
        if (optionalCategory.isEmpty()) {
            throw new NotExistCategoryException("?????? ??????????????? ???????????? ????????????.");
        }


        Category selectedCategory = optionalCategory.get();

        while (true) {
            if (productRepository.existsById(productId)) {
                productId = RandomIdGenerator.createId();
                continue;
            } else {
                productDtoForCreation.setProductId(productId);
                Product product = ProductMapper.INSTANCE.productDtoForCreationToProduct(productDtoForCreation);
                product.setCategory(selectedCategory);

                Product createdProduct = productRepository.save(product);

                CreatedProductDto createdProductDto = ProductMapper.INSTANCE.productToCreatedProductDto(createdProduct);

                return createdProductDto;
            }
        }
    }

    /*
     * Saga Step ?????? ??? ?????? ?????? ??????
     */
    public ProductStepDtoForCreation validateSagaProductDtos(ProductStepDtoForCreation productStepDtoForCreation) {
        List<OrderProductDtoForCreation> orderProductDtoForCreations = productStepDtoForCreation.getOrderProductDtos();

        for (OrderProductDtoForCreation orderProductDtoForCreation : orderProductDtoForCreations) {
            if (!orderProductDtoForCreation.getState().equals("READY")) {
                continue;
            }

            Optional<Product> optionalSelectedProduct = productRepository.findById(orderProductDtoForCreation.getProductId());

            if (optionalSelectedProduct.isEmpty()) {
                orderProductDtoForCreation.setState("NotExistElementException");
                continue;
            }

            Product selectedProduct = optionalSelectedProduct.get();
            selectedProduct.validateStockNotPublishException(orderProductDtoForCreation);
            selectedProduct.validatePriceNotPublishException(orderProductDtoForCreation);
        }

        return productStepDtoForCreation;
    }

    /*
     * SAGA ?????? ?????? ??????
     * Logic:
     * 1. DTO????????? ?????? ???????????? ????????????.
     * 2. ?????? ??????DTO??? ????????????.
     * 3. ?????? ????????? ????????? PENDING(?????????)?????? ?????????.
     * 4. ?????? ID??? ?????? ?????? ???????????? ????????????.
     * 4-1. ?????? ?????? ????????? ???????????? ???????????? RuntimeException ??????.
     * 5. ?????? ????????? ?????? ?????? ?????? ????????? ????????????. ?????? ????????? RuntimeException ??????.
     * 6. ?????? ????????? ????????????.(?????? ?????? ????????? RuntimeException ??????.)
     * ** ????????? Exception ????????? ?????? Exception???????????? ?????? ??????
     */
    @Transactional(
            readOnly = false,
            rollbackFor = {NotExistElementException.class, RuntimeException.class, NotEqualProductPrice.class, InvalidStockValueException.class},
            propagation = Propagation.REQUIRED
    )
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    public ProductStepDtoForCreation processProductStep(ProductStepDtoForCreation productStepDtoForCreation) {
        EventId id = new EventId(
                productStepDtoForCreation.getOrderId(),
                EventType.PROCESS_ORDER_PRODUCT
        );

        ProductEvent productEvent = new ProductEvent(id);

        productEventRepository.save(productEvent);

        List<OrderProductDtoForCreation> orderProductDtos = productStepDtoForCreation.getOrderProductDtos();
        productStepDtoForCreation.setState("PENDING");

        orderProductDtos.forEach((OrderProductDtoForCreation orderProductDtoForCreation) -> {
            orderProductDtoForCreation.setState("PENDING");
            Optional<Product> optionalSelectedProduct = productRepository.findById(orderProductDtoForCreation.getProductId());

            if (optionalSelectedProduct.isEmpty()) {
                orderProductDtoForCreation.setState("NotExistElementException");
                productStepDtoForCreation.setState("EXCEPTION");
                throw new NotExistElementException(orderProductDtoForCreation.getProductId() + " ?????? ????????? ???????????? ????????????.");
            }

            Product selectedProduct = optionalSelectedProduct.get();

            selectedProduct.validatePrice(orderProductDtoForCreation);

            selectedProduct.decreaseStock(orderProductDtoForCreation);

            orderProductDtoForCreation.setState("COMP");
        });

        if (productStepDtoForCreation.getState().equals("PENDING")) {
            productStepDtoForCreation.setState("COMP");
        }

        return productStepDtoForCreation;
    }

    @Transactional(
            readOnly = false,
            rollbackFor = {NotExistElementException.class, RuntimeException.class, NotEqualProductPrice.class, InvalidStockValueException.class},
            propagation = Propagation.REQUIRED
//            propagation = Propagation.REQUIRES_NEW
    )
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    public ProductStepDtoForCreation processProductStepUseStream(ProductStepDtoForCreation productStepDtoForCreation) {
        ProductEvent productEvent = new ProductEvent(
                new EventId(
                        productStepDtoForCreation.getOrderId(),
                        EventType.PROCESS_ORDER_PRODUCT
                )
        );

        productEventRepository.save(productEvent);

        List<OrderProductDtoForCreation> orderProductDtos = productStepDtoForCreation.getOrderProductDtos();
        productStepDtoForCreation.setState("PENDING");

//        orderProductDtos.stream();

        for (OrderProductDtoForCreation orderProductDtoForCreation : orderProductDtos) {
            orderProductDtoForCreation.setState("PENDING");
            Optional<Product> optionalSelectedProduct = productRepository.findById(orderProductDtoForCreation.getProductId());

            if (optionalSelectedProduct.isEmpty()) {
                orderProductDtoForCreation.setState("NotExistElementException");
                productStepDtoForCreation.setState("EXCEPTION");
                throw new NotExistElementException(orderProductDtoForCreation.getProductId() + " ?????? ????????? ???????????? ????????????.");
            }

            Product selectedProduct = optionalSelectedProduct.get();

            selectedProduct.validatePrice(orderProductDtoForCreation);

            selectedProduct.decreaseStock(orderProductDtoForCreation);

            orderProductDtoForCreation.setState("COMP");
        }

        if (productStepDtoForCreation.getState().equals("PENDING")) {
            productStepDtoForCreation.setState("COMP");
        }

        return productStepDtoForCreation;
    }

    /*
     * SAGA ?????? ?????? Revert
     */
    @Transactional(
            readOnly = false,
            rollbackFor = {NotExistElementException.class, RuntimeException.class, NotEqualProductPrice.class, InvalidStockValueException.class},
            propagation = Propagation.REQUIRED
    )
//    ????????? ?????????????????? ?????? ???????????? ?????????...? ?????? ??????
//    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    public void revertProductStep(RevertProductStepEvent revertProductStepEvent) {
        ProductEvent productEvent = new ProductEvent(
                new EventId(
                        revertProductStepEvent.getEventId(),
                        EventType.REVERT_ORDER_PRODUCT
                )
        );

        productEventRepository.save(productEvent);

        List<OrderProductDtoForCreation> orderProductDtos = revertProductStepEvent.getOrderProductDtos();

        for (OrderProductDtoForCreation orderProductDtoForCreation : orderProductDtos) {
            orderProductDtoForCreation.setState("PENDING");
            Optional<Product> optionalSelectedProduct = productRepository.findById(orderProductDtoForCreation.getProductId());

            if (optionalSelectedProduct.isEmpty()) {
                // revert ???????????? ?????? ????????? ???????????? ????????? ?????? ????????? ????????? ?????????
                orderProductDtoForCreation.setState(NotExistElementException.class.getSimpleName());

//                productStepDtoForCreation.setState(NotExistElementException.class.getSimpleName());
                continue;
            }

            Product selectedProduct = optionalSelectedProduct.get();
            selectedProduct.increaseStock(orderProductDtoForCreation);

            orderProductDtoForCreation.setState("REVERTED");
        }

    }

    @Transactional(readOnly = false)
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    public void setProductStock() {

    }

    @Transactional(readOnly = false)
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    public void increaseProductStock() {

    }

    /*
     * ?????? ??????
     * Logic:
     * 1. ?????? ???????????? ????????? ??????.
     */
    @Transactional(readOnly = false)
    public void deleteProduct(String productId) {
        if (productRepository.findById(productId).isEmpty()) {
            return;
        }

        productRepository.deleteById(productId);
    }

    /*
     * ?????? ?????? ??????
     * Logic:
     * 1. ???????????? ??????????????? ?????? ??????
     */
    @Transactional(readOnly = false)
    public void deleteProducts(List<String> productIds) {
        for (String productId : productIds) {
            productRepository.deleteById(productId);
        }
    }
}
