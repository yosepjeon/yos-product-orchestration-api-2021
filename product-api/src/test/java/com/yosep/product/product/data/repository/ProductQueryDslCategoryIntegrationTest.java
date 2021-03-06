package com.yosep.product.product.data.repository;

import com.yosep.product.category.service.CategoryService;
import com.yosep.product.common.BaseProductIntegrationTest;
import com.yosep.product.jpa.category.data.repository.CategoryRepository;
import com.yosep.product.jpa.product.data.entity.Product;
import com.yosep.product.jpa.product.data.entity.ProductTest;
import com.yosep.product.jpa.product.data.repository.ProductRepository;
import com.yosep.product.jpa.product.data.repository.ProductTestRepositoryQueryDsl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@Slf4j
public class ProductQueryDslCategoryIntegrationTest extends BaseProductIntegrationTest {
    private final ProductTestRepositoryQueryDsl productTestRepositoryQueryDsl;

    @Autowired
    public ProductQueryDslCategoryIntegrationTest(
            CategoryRepository categoryRepository,
            CategoryService categoryService,
            ProductTestRepositoryQueryDsl productTestRepositoryQueryDsl,
            ProductRepository productRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
        this.productTestRepositoryQueryDsl = productTestRepositoryQueryDsl;
        this.productRepository = productRepository;
    }

    @Test
    @DisplayName("[ProductQueryDsl] QueryDsl 테스트")
    public void findByIdTest() {
        Optional<ProductTest> result = productTestRepositoryQueryDsl.findById("test");

        Assertions.assertEquals(true, result.isEmpty());
    }

    @Test
    @DisplayName("[ProductQueryDsl] 카테고리별 상품 조회 테스트")
    public void 카테고리별_상품_조회_테스트() {
        log.info("[ProductQueryDsl] 카테고리별 상품 조회 테스트");
        System.out.println("??? "+ childCategoryId1);
        Optional<List<Product>> optionalProducts = productRepository.findAllByCategory(childCategoryId1);
        List<Product> products = optionalProducts.get();
        products.forEach(product ->
                log.info(product.toString())
        );
    }
}
