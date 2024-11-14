package com.clubconnect.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DataController {

    @GetMapping("/private/data")
    public String privateData() {
        return "This is private data for ROLE_USER.";
    }

    @GetMapping("/admin/data")
    public String adminData() {
        return "This is admin data for ROLE_ADMIN.";
    }
}
