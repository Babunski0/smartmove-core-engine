package com.smartmove.domain.entity;

import com.smartmove.domain.enums.City;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jniyi
 * @project smartmove-core-engine - 2026
 * @created 16.02.2026
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String userId;
    private String fullName;
    private String email;
    private String phoneNumber;

    private City homeCity;
    private boolean active;

    private Double accountBalance;

    // Convenience constructor (bez active; default true)
    public User(String userId, String fullName, String email, String phoneNumber, City homeCity, Double accountBalance) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.homeCity = homeCity;
        this.accountBalance = accountBalance;
        this.active = true;
    }
}
