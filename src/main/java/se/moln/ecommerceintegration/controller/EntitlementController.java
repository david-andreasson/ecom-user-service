package se.moln.ecommerceintegration.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.moln.ecommerceintegration.model.User;
import se.moln.ecommerceintegration.repository.UserRepository;
import se.moln.ecommerceintegration.service.EntitlementService;
import se.moln.ecommerceintegration.service.JwtService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class EntitlementController {
    private final UserRepository users;
    private final EntitlementService entitlements;
    private final JwtService jwt;

    private UUID currentUserIdFromAuth(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) return null;
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        if (!jwt.isTokenValid(token)) return null;
        String email = jwt.extractEmail(token);
        if (email == null || email.isBlank()) return null;
        return users.findByEmail(email).map(User::getId).orElse(null);
    }

    @GetMapping("/api/users/me/entitlements")
    public ResponseEntity<?> getEntitlement(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
                                            @RequestParam String sku) {
        var uid = currentUserIdFromAuth(auth);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        return entitlements.find(uid, sku)
                .<ResponseEntity<?>>map(e -> ResponseEntity.ok(Map.of(
                        "sku", e.getSku(),
                        "remaining", e.getRemaining()
                )))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("sku", sku, "remaining", 0)));
    }

    // Alias without "/api/users" prefix to be resilient to proxy path rewriting
    @GetMapping("/me/entitlements")
    public ResponseEntity<?> getEntitlementAlias(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
                                                 @RequestParam String sku) {
        return getEntitlement(auth, sku);
    }

    @PostMapping("/api/users/me/entitlements/consume")
    public ResponseEntity<?> consume(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
                                     @RequestBody Map<String, Object> body) {
        var uid = currentUserIdFromAuth(auth);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        String sku = (String) body.getOrDefault("sku", "");
        int count = ((Number) body.getOrDefault("count", 1)).intValue();
        boolean ok = entitlements.consume(uid, sku, count);
        if (!ok) return ResponseEntity.status(409).body(Map.of("message", "Insufficient entitlement"));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // Alias without prefix
    @PostMapping("/me/entitlements/consume")
    public ResponseEntity<?> consumeAlias(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
                                          @RequestBody Map<String, Object> body) {
        return consume(auth, body);
    }

    // Mock payment: issues entitlement remaining=1 for the current user
    @PostMapping("/api/users/me/checkout/mock-pay")
    public ResponseEntity<?> mockPay(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
                                     @RequestBody Map<String, Object> body) {
        var uid = currentUserIdFromAuth(auth);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        String sku = (String) body.getOrDefault("sku", "HOROSCOPE_PDF");
        entitlements.issue(uid, sku, 1, null);
        return ResponseEntity.ok(Map.of("status", "PAID"));
    }

    // Alias without prefix
    @PostMapping("/me/checkout/mock-pay")
    public ResponseEntity<?> mockPayAlias(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth,
                                          @RequestBody Map<String, Object> body) {
        return mockPay(auth, body);
    }
}
