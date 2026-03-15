package com.healthcare.erp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.ofEntries(
                Map.entry("application", "Healthcare ERP"),
                Map.entry("version", "3.0.0"),
                Map.entry("endpoints", Map.ofEntries(
                        Map.entry("login", "POST /api/auth/login"),
                        Map.entry("createHospitalAdmin", "POST /api/admin/users (SUPER_ADMIN only)"),
                        Map.entry("hospitals", "/api/hospitals"),
                        Map.entry("hospitalUsers", "/api/hospitals/{hospitalId}/users"),
                        Map.entry("departments", "/api/hospitals/{hospitalId}/departments"),
                        Map.entry("patients", "/api/hospitals/{hospitalId}/patients"),
                        Map.entry("doctors", "/api/hospitals/{hospitalId}/doctors"),
                        Map.entry("appointments", "/api/hospitals/{hospitalId}/appointments"),
                        Map.entry("medicalRecords", "/api/hospitals/{hospitalId}/medical-records"),
                        Map.entry("patientRecords", "/api/hospitals/{hospitalId}/patients/{patientId}/medical-records")
                )));
    }
}
