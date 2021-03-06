package com.yosep.product.cart.data.repository;

import com.yosep.product.cart.service.CartCommandService;
import com.yosep.product.cart.service.CartQueryService;
import com.yosep.product.common.BaseCartIntegrationTest;
import com.yosep.product.jpa.cart.data.entity.Cart;
import com.yosep.product.jpa.cart.data.repository.CartRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Slf4j
public class CartRepositoryTest extends BaseCartIntegrationTest {
    @Autowired
    public CartRepositoryTest(CartRepository cartRepository, CartQueryService cartQueryService, CartCommandService cartCommandService) {
        this.cartRepository = cartRepository;
        this.cartCommandService = cartCommandService;
        this.cartQueryService = cartQueryService;
    }

    @Test
    @DisplayName("[CartRepository] 장바구니 생성 성공 테스트")
    public void createCartSuccessTest() {
        String userId = "test1";
        String cartId = "cart-test1";

        Cart cart = new Cart(cartId, userId);

        Cart createdCart = cartRepository.save(cart);

        Assertions.assertEquals(true, createdCart.getCartId().equals(cartId));
        Assertions.assertEquals(true, createdCart.getUserId().equals(userId));
        Assertions.assertEquals(true, createdCart.getCartProducts().isEmpty());
    }

    @Test
    @DisplayName("[CartRepository] 장바구니 생성 실패 테스트")
    public void createCartFailTest() {
        String userId = "test1";

        Optional<Cart> optionalCart = cartRepository.findByUserId(userId);

        Assertions.assertEquals(true, optionalCart.isEmpty());
    }

}
