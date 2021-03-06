package com.yosep.product.product.mq.producer;

import com.yosep.product.product.mq.producer.ProductToCouponProducer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutionException;

@Slf4j
@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class ProductToCouponProducerTest {
    private final ProductToCouponProducer productToCouponProducer;

    @Autowired
    public ProductToCouponProducerTest(ProductToCouponProducer productToCouponProducer) {
        this.productToCouponProducer = productToCouponProducer;
    }

//    @Test
//    @DisplayName("[Kafka Test]카프카 메시지 전송 테스트")
//    public void 카프카_메시지_전송_테스트() throws ExecutionException, InterruptedException {
//        productToCouponProducer.sendToCouponTest("!!!");
//    }
}
