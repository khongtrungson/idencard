package com.idencard.auth.server.web;

import com.idencard.auth.application.dto.UserDto;
import com.idencard.auth.application.dto.UserRegistrationCommand;
import com.idencard.auth.application.service.UserService;
import com.idencard.auth.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserRegistrationCommand command) {
        User registered = userService.register(
                command.username(),
                command.password(),
                command.email(),
                command.fullName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.fromDomain(registered));
    }

    @GetMapping("/user/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal Object principal) {
        if (principal instanceof Jwt jwt) {
            return ResponseEntity.ok(Map.of(
                    "subject", jwt.getSubject(),
                    "claims", jwt.getClaims()
            ));
        }
        return ResponseEntity.ok(Map.of("principal", principal.toString()));
    }
}
