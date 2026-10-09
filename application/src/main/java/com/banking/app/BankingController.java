package com.banking.app;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class BankingController {

    @GetMapping("/")
    public String home() {
        return "Retail Banking Application is running successfully";
    }

    @GetMapping("/api/account")
    public Map<String, Object> account() {

        Map<String, Object> account = new HashMap<>();

        account.put("accountNumber", "XXXX-XXXX-1234");
        account.put("customerName", "Demo Customer");
        account.put("accountType", "Savings");
        account.put("balance", 75000);
        account.put("currency", "INR");

        return account;
    }

    @GetMapping("/health")
    public String health() {
        return "UP";
    }
}
