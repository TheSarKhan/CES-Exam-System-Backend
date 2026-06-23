package com.ces.exam;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashTest {

    @Test
    public void generateHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("HASH_OUTPUT_START");
        System.out.println(encoder.encode("admin123"));
        System.out.println("HASH_OUTPUT_END");
    }
}
