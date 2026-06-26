package com.ces.exam.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @NotBlank(message = "Ad boş ola bilməz")
    @Size(max = 100, message = "Ad çox uzundur")
    private String firstName;

    @NotBlank(message = "Soyad boş ola bilməz")
    @Size(max = 100, message = "Soyad çox uzundur")
    private String lastName;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}
