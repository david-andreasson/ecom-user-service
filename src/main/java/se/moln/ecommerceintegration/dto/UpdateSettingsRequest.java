package se.moln.ecommerceintegration.dto;
import jakarta.validation.constraints.Size;

public record UpdateSettingsRequest(
        @Size(min = 1, max = 100, message = "FirstName must be 1-100 chars")
        String firstName,
        @Size(min = 1, max = 100, message = "LastName must be 1-100 chars")
        String lastName
) {}
