package com.yosep.product.product.data.repository;

import com.yosep.product.common.BaseIntegrationTest;
import com.yosep.product.product.data.entity.ProductTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Slf4j
public class ProductTestRepositoryIntegrationTest extends BaseIntegrationTest {
    private final ProductTestRepository productTestRepository;

    @Autowired
    public ProductTestRepositoryIntegrationTest(ProductTestRepository productTestRepository) {
        this.productTestRepository = productTestRepository;
    }

    @Test
    @DisplayName("데이터 연결 및 읽어오기 Test")
    public void readEntityTest() {
        Optional<ProductTest> productTest = productTestRepository.findById("test");

        Assertions.assertEquals(true,productTest.isEmpty());
    }
}