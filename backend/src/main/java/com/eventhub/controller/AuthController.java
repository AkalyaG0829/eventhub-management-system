package com.eventhub.controller;

import com.eventhub.dto.ApiResponse;
import com.eventhub.dto.AuthResponse;
import com.eventhub.dto.LoginRequest;
import com.eventhub.dto.RegisterRequest;
import com.eventhub.dto.UserProfileDto;
import com.eventhub.security.CustomUserDetails;
import com.eventhub.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse authResponse = authService.registerUser(registerRequest);
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser(@AuthenticationPrincipal CustomUserDetails currentUser) {
        UserProfileDto userProfile = authService.getCurrentUser(currentUser);
        return ResponseEntity.ok(userProfile);
    }
}
