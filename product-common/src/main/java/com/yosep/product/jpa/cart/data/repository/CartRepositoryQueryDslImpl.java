package com.yosep.product.jpa.cart.data.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yosep.product.jpa.cart.data.entity.Cart;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.yosep.product.jpa.cart.data.entity.QCart.cart;


@RequiredArgsConstructor
public class CartRepositoryQueryDslImpl implements CartRepositoryQueryDsl{
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Cart> findByUserId(String userId) {
        Cart cartEntity = jpaQueryFactory.selectFrom(cart)
                .where(cart.userId.eq(userId))
                .fetchOne();

        return cartEntity != null ? Optional.of(cartEntity) : Optional.empty();
    }
}