package se.moln.ecommerceintegration.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import se.moln.ecommerceintegration.dto.UserProfileResponse;
import se.moln.ecommerceintegration.model.User;
import se.moln.ecommerceintegration.repository.UserRepository;

@RestController
@RequiredArgsConstructor
public class ProfileController {
    private final UserRepository repo;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public UserProfileResponse me(@AuthenticationPrincipal Object principal){
        String email = extractEmail(principal);
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        return new UserProfileResponse(
                u.getId(),
                u.getEmail(),
                u.getFirstName(),
                u.getLastName(),
                u.getRole().name(),
                Boolean.TRUE.equals(u.getIsActive())
        );
    }

    private String extractEmail(Object principal) {
        if (principal instanceof UserDetails ud) return ud.getUsername(); // username = email
        if (principal instanceof String s) return s; // filtret satte principal som email-string
        throw new IllegalStateException("Unsupported principal type: " + principal);
    }
















}
