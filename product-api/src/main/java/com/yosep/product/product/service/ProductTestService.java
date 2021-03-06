package com.yosep.product.product.service;

import com.yosep.product.jpa.product.data.entity.ProductTest;
import com.yosep.product.jpa.product.data.repository.ProductTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductTestService {
    private final ProductTestRepository productTestRepository;

    @Autowired
    public ProductTestService(ProductTestRepository productTestRepository) {
        this.productTestRepository = productTestRepository;
    }

    public ProductTest readProductTest(String id) {
        return productTestRepository.findById(id).orElse(new ProductTest());
    }
}
