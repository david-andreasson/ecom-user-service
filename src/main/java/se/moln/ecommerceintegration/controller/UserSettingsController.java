package se.moln.ecommerceintegration.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import se.moln.ecommerceintegration.dto.UpdateSettingsRequest;
import se.moln.ecommerceintegration.dto.UserProfileResponse;
import se.moln.ecommerceintegration.model.User;
import se.moln.ecommerceintegration.service.UserService;

import static se.moln.ecommerceintegration.utils.PrincipalUtils.extractEmail;

@RestController
@RequiredArgsConstructor
public class UserSettingsController {

    private final UserService userService;

    @PutMapping("/me/settings")
    @Operation(
            summary = "Update own profile settings",
            description = "Send only the fields you want to update.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(
                            name = "Example",
                            value = """
                        {
                          "firstName": "Ismete",
                          "lastName": "Hani"
                        }
                        """
                    ))
            )
    )
    public UserProfileResponse updateSettings(
            @AuthenticationPrincipal Object principal,
            @Valid @org.springframework.web.bind.annotation.RequestBody UpdateSettingsRequest req
    ) {
        String email = extractEmail(principal);

        String first = req.firstName();
        if (first != null) {
            first = first.trim();
            if (first.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "firstName must not be blank");
        }

        String last = req.lastName();
        if (last != null) {
            last = last.trim();
            if (last.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lastName must not be blank");
        }

        User u = userService.updateSettings(email, first, last);
        return new UserProfileResponse(
                u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(),
                u.getRole().name(), Boolean.TRUE.equals(u.getIsActive())
        );
    }
}
