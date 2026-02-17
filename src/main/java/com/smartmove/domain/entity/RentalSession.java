package com.smartmove.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartmove.domain.enums.City;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Represents a vehicle rental session.
 *
 * <p>This session stores the full {@link User} object for easier access to user details during rental operations.
 * For persistence (JSON), only the {@code userId} value is stored. Jackson maps the JSON {@code userId} field
 * through {@link #getUserId()} and {@link #setUserId(String)}.</p>
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>User id: {@code rental.getUser().getUserId()}</li>
 *   <li>User info: {@code rental.getUser().getFirstName()}, etc.</li>
 * </ul>
 */



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalSession {

	private String rentalId;
	private String vehicleId;

	private User user;

	private City city;

	private Instant startTime;
	private Instant endTime;

	private GPSLocation startLocation;
	private GPSLocation endLocation;

	private BigDecimal totalCostAmount;
	private String costCurrency;

	private boolean completed;

	@Builder.Default
	private List<Map<String, Object>> additionalCharges = new ArrayList<>();

	public RentalSession(String rentalId, String vehicleId, User user, City city, GPSLocation startLocation) {
		this.rentalId = rentalId;
		this.vehicleId = vehicleId;
		this.user = user;
		this.city = city;
		this.startLocation = startLocation;
		this.startTime = Instant.now();
		this.additionalCharges = new ArrayList<>();
		this.completed = false;
	}
	
	/**
	 * JSON persistence proxy: stores only userId in JSON.
	 */


	@JsonProperty("userId")
	public String getUserId() {
		return user != null ? user.getUserId() : null;
	}

	@JsonProperty("userId")
	public void setUserId(String userId) {
		if (this.user == null) {
			this.user = new User();
		}
		this.user.setUserId(userId);
	}

	public void endRental(GPSLocation endLocation) {
		this.endLocation = endLocation;
		this.endTime = Instant.now();
		this.completed = true;
	}

	public void addCharge(String chargeType, double amount) {
		Map<String, Object> charge = new HashMap<>();
		charge.put("type", chargeType);
		charge.put("amount", amount);
		charge.put("timestamp", Instant.now());
		additionalCharges.add(charge);
	}

	public long getDurationMinutes() {
		return Duration.between(endTime, startTime).toMinutes();
	}

	public Double getTotalAdditionalCharges() {
		return additionalCharges.stream().mapToDouble(charge -> ((Number) charge.get("amount")).doubleValue()).sum();
	}
}