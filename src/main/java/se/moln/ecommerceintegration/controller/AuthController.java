package se.moln.ecommerceintegration.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
    @Operation(summary = "Register a new user")
    public AuthResponse register(
            @RequestBody(
                    description = "User registration payload",
                    required = true,
                    content = @Content(examples = @ExampleObject(
                            name = "Example",
                            value = """
                                    {
                                      "email": "user@example.com",
                                      "password": "Password123!",
                                      "firstName": "King",
                                      "lastName": "Kong"
                                    }
                                    """
                    ))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody RegisterRequest req
    ) {
        User user = userService.register(req.email(), req.password(), req.firstName(), req.lastName());
        return new AuthResponse(jwt.createAccessToken(user.getId(), user.getEmail(), user.getRole().name()));
    }

    @PostMapping("/login")
    @Operation(summary = "Log in with email and password")
    public AuthResponse login(
            @RequestBody(
                    description = "User credentials",
                    required = true,
                    content = @Content(examples = @ExampleObject(
                            name = "Example",
                            value = """
                                    {
                                      "email": "user@example.com",
                                      "password": "Password123!"
                                    }
                                    """
                    ))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody LoginRequest req
    ) {
        User user = users.findByEmail(req.email()).orElseThrow(() -> new IllegalStateException("Invalid credentials"));
        var enc = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        if (!enc.matches(req.password(), user.getPasswordHash())) throw new IllegalStateException("Invalid credentials");
        return new AuthResponse(jwt.createAccessToken(user.getId(), user.getEmail(), user.getRole().name()));
    }
}