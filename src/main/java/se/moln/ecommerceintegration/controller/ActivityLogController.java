package se.moln.ecommerceintegration.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import se.moln.ecommerceintegration.model.ActivityLog;
import se.moln.ecommerceintegration.repository.ActivityLogRepository;
import java.util.List;

@RestController
public class ActivityLogController {

    private final ActivityLogRepository repo;

    public ActivityLogController(ActivityLogRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/users/history")
    @Operation(summary = "Get last 50 activity logs for the authenticated user")
    public List<ActivityLog> myHistory(@AuthenticationPrincipal Object principal) {
        String email = extractEmail(principal);
        return repo.findTop50ByUserEmailOrderByCreatedAtDesc(email);
    }

    private String extractEmail(Object principal) {
        if (principal instanceof UserDetails ud) return ud.getUsername();
        if (principal instanceof String s) return s;
        throw new IllegalStateException("Unsupported principal type: " + principal);
    }
}
