package com.ces.exam.controller;

import com.ces.exam.payload.response.PublicSettingsResponse;
import com.ces.exam.service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/settings")
public class PublicSettingsController {

    private final SettingsService settingsService;

    public PublicSettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ResponseEntity<PublicSettingsResponse> getPublicSettings() {
        return ResponseEntity.ok(settingsService.getPublicSettings());
    }
}
