package com.healthcare.erp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
                "application", "Healthcare ERP",
                "version", "1.0.0",
                "endpoints", Map.of(
                        "hospitals", "/api/hospitals",
                        "departments", "/api/hospitals/{hospitalId}/departments",
                        "patients", "/api/hospitals/{hospitalId}/patients"));
    }
}
