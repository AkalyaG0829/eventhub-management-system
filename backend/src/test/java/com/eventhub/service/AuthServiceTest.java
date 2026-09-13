package com.eventhub.service;

import com.eventhub.dto.AuthResponse;
import com.eventhub.dto.LoginRequest;
import com.eventhub.dto.RegisterRequest;
import com.eventhub.exception.BadRequestException;
import com.eventhub.model.Role;
import com.eventhub.model.User;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.CustomUserDetails;
import com.eventhub.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private CustomUserDetails customUserDetails;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("encoded_password")
                .role(Role.PARTICIPANT)
                .build();

        customUserDetails = new CustomUserDetails(1L, "test@example.com", "encoded_password", true, Collections.emptyList());
        
        authentication = mock(Authentication.class);
    }

    @Test
    void registerUser_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt_token");

        AuthResponse response = authService.registerUser(request);

        assertNotNull(response);
        assertEquals("jwt_token", response.getAccessToken());
        assertEquals(testUser.getEmail(), response.getUser().getEmail());
        assertEquals(Role.PARTICIPANT, response.getUser().getRole());
        
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_EmailAlreadyExists_ThrowsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.registerUser(request));
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticateUser_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt_token");

        AuthResponse response = authService.authenticateUser(request);

        assertNotNull(response);
        assertEquals("jwt_token", response.getAccessToken());
        assertEquals(testUser.getEmail(), response.getUser().getEmail());
    }
}
