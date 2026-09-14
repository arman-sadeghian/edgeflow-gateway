package com.sadeghian.edgeflow.payment;

import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @PostMapping
    public Map<String, Object> createPayment() {
        return Map.of(
                "paymentId", UUID.randomUUID().toString(),
                "status", "SUCCESS",
                "service", "payment-service"
        );
    }
}