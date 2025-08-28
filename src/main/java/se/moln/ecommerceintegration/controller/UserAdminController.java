package se.moln.ecommerceintegration.controller;


import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import se.moln.ecommerceintegration.model.Role;
import se.moln.ecommerceintegration.model.User;
import se.moln.ecommerceintegration.repository.UserRepository;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // hela controllern kräver ADMIN
public class UserAdminController {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    // DTOs (enkla inline records – byt till dina egna om du vill)
    public record UserDto(UUID id, String email, String firstName, String lastName, Role role, Boolean isActive) {}
    public record CreateUserRequest(String email, String password, String firstName, String lastName, Role role) {}
    public record UpdateUserRequest(String firstName, String lastName, Boolean isActive) {}
    public record UpdateRoleRequest(Role role) {}

    private UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(), u.getRole(), u.getIsActive());
    }

    @GetMapping
    @Operation(summary = "List all users (ADMIN)")
    public List<UserDto> list() {
        return users.findAll().stream().map(this::toDto).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by ID (ADMIN)")
    public UserDto get(@PathVariable UUID id) {
        User u = users.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        return toDto(u);
    }

    @PostMapping
    @ResponseStatus(CREATED)
    @Operation(summary = "Create a new user (ADMIN)")
    public UserDto create(@Valid @RequestBody CreateUserRequest req) {
        if (req.email() == null || req.email().isBlank())
            throw new ResponseStatusException(BAD_REQUEST, "Email is required");
        if (users.existsByEmail(req.email()))
            throw new ResponseStatusException(CONFLICT, "Email already in use");

        User u = User.newUser(
                req.email(),
                passwordEncoder.encode(req.password()),
                req.firstName(),
                req.lastName()
        );
        u.setRole(req.role() == null ? Role.USER : req.role());
        u.setIsActive(true);

        return toDto(users.save(u));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update basic fields (ADMIN)")
    public UserDto update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest req) {
        User u = users.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        if (req.firstName() != null) u.setFirstName(req.firstName());
        if (req.lastName() != null)  u.setLastName(req.lastName());
        if (req.isActive() != null)  u.setIsActive(req.isActive());
        return toDto(users.save(u));
    }

    @PostMapping("/{id}/role")
    @Operation(summary = "Set role (ADMIN)")
    public UserDto setRole(@PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest req) {
        if (req.role() == null) throw new ResponseStatusException(BAD_REQUEST, "role is required");
        User u = users.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        u.setRole(req.role());
        return toDto(users.save(u));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    @Operation(summary = "Delete user (ADMIN)")
    public void delete(@PathVariable UUID id) {
        if (!users.existsById(id)) throw new ResponseStatusException(NOT_FOUND, "User not found");
        users.deleteById(id);
    }
}
