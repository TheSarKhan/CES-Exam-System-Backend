package com.ces.exam.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequest {
    @NotBlank(message = "E-poçt tələb olunur")
    @Email(message = "E-poçt düzgün deyil")
    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
