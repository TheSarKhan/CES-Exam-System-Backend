package com.ces.exam.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HashController {

    private final PasswordEncoder passwordEncoder;

    public HashController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/api/v1/public/hash/{text}")
    public String hash(@PathVariable String text) {
        return passwordEncoder.encode(text);
    }
}
