package com.smartmove.domain.entity;

import com.smartmove.domain.enums.City;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * User - Represents a SmartMove user
 *
 * Simple read-only user entity for rental operations.
 * Users are loaded from users.json and used only to reference
 * user information during vehicle rental management.
 *
 * No user management features needed - only basic information
 * for rental context and tracking.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private City homeCity;
    private int completedRentals;
    private Double averageRating;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
