package com.novis.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novis.account.entity.Account;
import com.novis.account.repository.AccountRepository;
import com.novis.audit.service.AuditService;
import com.novis.auth.dto.LoginRequest;
import com.novis.auth.dto.MfaVerifyRequest;
import com.novis.auth.dto.RegisterRequest;
import com.novis.auth.dto.TokenResponse;
import com.novis.auth.entity.RefreshToken;
import com.novis.auth.entity.User;
import com.novis.auth.repository.RefreshTokenRepository;
import com.novis.auth.repository.UserRepository;
import com.novis.common.config.JwtService;
import com.novis.common.exception.DuplicateRequestException;
import com.novis.common.exception.ResourceNotFoundException;
import com.novis.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.context.annotation.Lazy;

@Service
public class AuthService implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);


    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;

    @Transactional
    public void register(RegisterRequest request, HttpServletRequest httpServletRequest) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateRequestException("Email already exists");
        }

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(User.Role.CUSTOMER)
                .build();
        user = userRepository.save(user);

        // Create first checking account
        String accountNumber = generateAccountNumber();
        Account account = Account.builder()
                .user(user)
                .accountNumber(accountNumber)
                .accountType(Account.AccountType.CHECKING)
                .build();
        accountRepository.save(account);

        auditService.log(user.getId(), "REGISTER", "User", user.getId(), "User registered", httpServletRequest);
    }

    @Transactional
    public TokenResponse login(LoginRequest request, HttpServletRequest httpServletRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        auditService.log(user.getId(), "LOGIN_ATTEMPT", "User", user.getId(), null, httpServletRequest);

        if (user.isMfaEnabled()) {
            String otp = String.format("%06d", new SecureRandom().nextInt(999999));
            user.setMfaSecret(passwordEncoder.encode(otp));
            userRepository.save(user);
            log.info("MFA OTP for {}: {}", user.getEmail(), otp); // Mock sending OTP
            String mfaSessionToken = jwtService.generateMfaSessionToken(user.getEmail());
            return new TokenResponse(null, null, true, mfaSessionToken);
        }

        return generateTokens(user, httpServletRequest);
    }

    @Transactional
    public TokenResponse verifyMfa(MfaVerifyRequest request, HttpServletRequest httpServletRequest) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getMfaSecret() == null || !passwordEncoder.matches(request.otp(), user.getMfaSecret())) {
            throw new UnauthorizedException("Invalid OTP");
        }

        user.setMfaSecret(null);
        userRepository.save(user);
        auditService.log(user.getId(), "MFA_VERIFIED", "User", user.getId(), null, httpServletRequest);

        return generateTokens(user, httpServletRequest);
    }

    @Transactional
    public TokenResponse refreshToken(String refreshTokenStr) {
        String email = jwtService.extractUsername(refreshTokenStr);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (jwtService.isTokenValid(refreshTokenStr, user)) {
            RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                    .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

            if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new UnauthorizedException("Invalid or expired refresh token");
            }

            String accessToken = jwtService.generateAccessToken(user, user.getId(), user.getRole().name());
            return new TokenResponse(accessToken, refreshTokenStr, false, null);
        }
        throw new UnauthorizedException("Invalid token");
    }

    @Transactional
    public void logout(String refreshTokenStr, HttpServletRequest request) {
        refreshTokenRepository.findByToken(refreshTokenStr).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            auditService.log(token.getUser().getId(), "LOGOUT", "User", token.getUser().getId(), null, request);
        });
    }

    private TokenResponse generateTokens(User user, HttpServletRequest request) {
        String accessToken = jwtService.generateAccessToken(user, user.getId(), user.getRole().name());
        String refreshTokenStr = jwtService.generateRefreshToken(user);

        refreshTokenRepository.deleteByUser(user);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenStr)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(refreshToken);

        auditService.log(user.getId(), "LOGIN_SUCCESS", "User", user.getId(), null, request);

        return new TokenResponse(accessToken, refreshTokenStr, false, null);
    }

    private String generateAccountNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public AuthService(UserRepository userRepository, AccountRepository accountRepository, RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder, JwtService jwtService, @Lazy AuthenticationManager authenticationManager, AuditService auditService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.auditService = auditService;
    }
}
