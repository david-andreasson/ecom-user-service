package se.moln.ecommerceintegration.dto;

public record AuthResponse(String accessToken, String tokenType) {
    public AuthResponse(String token) { this(token, "Bearer"); }
}