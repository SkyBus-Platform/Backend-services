package com.skybus.user.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * All fields are optional — only non-null fields are applied.
 * Using a class (not record) because records make all fields final,
 * which makes partial-update logic awkward.
 */
@Getter
public class UpdateProfileRequest {

    // Getters
    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 20)
    private String phone;

}