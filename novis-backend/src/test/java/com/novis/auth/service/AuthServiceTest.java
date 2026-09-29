package com.novis.auth.service;

import com.novis.account.repository.AccountRepository;
import com.novis.audit.service.AuditService;
import com.novis.auth.dto.LoginRequest;
import com.novis.auth.dto.RegisterRequest;
import com.novis.auth.entity.User;
import com.novis.auth.repository.RefreshTokenRepository;
import com.novis.auth.repository.UserRepository;
import com.novis.common.config.JwtService;
import com.novis.common.exception.DuplicateRequestException;
import com.novis.common.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private AuditService auditService;
    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister_success() {
        RegisterRequest req = new RegisterRequest("John Doe", "john@test.com", "password");
        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hash");
        
        User savedUser = User.builder().id(1L).email("john@test.com").build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        authService.register(req, httpServletRequest);

        verify(userRepository).save(any(User.class));
        verify(accountRepository).save(any());
        verify(auditService).log(eq(1L), eq("REGISTER"), eq("User"), eq(1L), anyString(), eq(httpServletRequest));
    }

    @Test
    void testRegister_duplicateEmail_throws() {
        RegisterRequest req = new RegisterRequest("John Doe", "john@test.com", "password");
        when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

        assertThrows(DuplicateRequestException.class, () -> authService.register(req, httpServletRequest));
    }

    @Test
    void testLogin_invalidPassword_throws() {
        LoginRequest req = new LoginRequest("john@test.com", "wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(req, httpServletRequest));
    }
}
