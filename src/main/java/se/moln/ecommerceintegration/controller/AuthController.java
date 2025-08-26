package se.moln.ecommerceintegration.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import se.moln.ecommerceintegration.dto.AuthResponse;
import se.moln.ecommerceintegration.dto.LoginRequest;
import se.moln.ecommerceintegration.dto.RegisterRequest;
import se.moln.ecommerceintegration.model.User;
import se.moln.ecommerceintegration.repository.UserRepository;
import se.moln.ecommerceintegration.service.JwtService;
import se.moln.ecommerceintegration.service.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepository users;
    private final JwtService jwt;

    public AuthController(UserService userService, UserRepository users, JwtService jwt) {
        this.userService = userService; this.users = users; this.jwt = jwt;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        User user = userService.register(req.email(), req.password(), req.firstName(), req.lastName());
        return new AuthResponse(jwt.createAccessToken(user.getId(), user.getEmail(), user.getRole()));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        User user = users.findByEmail(req.email()).orElseThrow(() -> new IllegalStateException("Invalid credentials"));
        var enc = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        if (!enc.matches(req.password(), user.getPasswordHash())) throw new IllegalStateException("Invalid credentials");
        return new AuthResponse(jwt.createAccessToken(user.getId(), user.getEmail(), user.getRole()));
    }
}