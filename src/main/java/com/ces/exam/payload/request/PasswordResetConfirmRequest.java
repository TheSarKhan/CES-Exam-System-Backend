package com.ces.exam.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordResetConfirmRequest {
    @NotBlank(message = "Token tələb olunur")
    private String token;

    @NotBlank(message = "Yeni parol tələb olunur")
    @Size(min = 6, max = 100, message = "Parol ən azı 6 simvol olmalıdır")
    private String newPassword;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
