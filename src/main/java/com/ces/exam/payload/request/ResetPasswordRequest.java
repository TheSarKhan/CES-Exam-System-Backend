package com.ces.exam.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ResetPasswordRequest {
    @NotBlank(message = "Yeni parol tələb olunur")
    @Pattern(regexp = ValidationPatterns.PASSWORD, message = ValidationPatterns.PASSWORD_MSG)
    private String password;

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
