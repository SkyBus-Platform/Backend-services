package com.skybus.user.dto.request;

import jakarta.validation.constraints.Size;

/**
 * All fields are optional — only non-null fields are applied.
 * Using a class (not record) because records make all fields final,
 * which makes partial-update logic awkward.
 */
public class UpdateProfileRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 20)
    private String phone;

    // Getters
    public String getFirstName() { return firstName; }
    public String getLastName()  { return lastName;  }
    public String getPhone()     { return phone;     }
}