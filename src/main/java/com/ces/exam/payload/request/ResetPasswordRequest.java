package com.ces.exam.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {
    @NotBlank(message = "Yeni parol tələb olunur")
    @Size(min = 6, max = 100, message = "Parol ən azı 6 simvol olmalıdır")
    private String password;

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
