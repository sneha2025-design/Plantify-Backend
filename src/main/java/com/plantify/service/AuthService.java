package com.plantify.service;

import com.plantify.dto.*;
import com.plantify.entity.JwtToken;
import com.plantify.entity.Role;
import com.plantify.entity.User;
import com.plantify.exception.BadRequestException;
import com.plantify.exception.ResourceNotFoundException;
import com.plantify.exception.UnauthorizedException;
import com.plantify.repository.JwtTokenRepository;
import com.plantify.repository.UserRepository;
import com.plantify.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenRepository jwtTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Confirm password does not match password");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email address is already registered");
        }

        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new BadRequestException("Mobile number is already registered");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .username(request.getUsername())
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .build();

        userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        saveTokenForUser(user, token);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailOrMobileNumber(request.getEmailOrMobile(), request.getEmailOrMobile())
                .or(() -> userRepository.findByUsername(request.getEmailOrMobile()))
                .orElseThrow(() -> new UnauthorizedException("Invalid email/mobile number or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email/mobile number or password");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        saveTokenForUser(user, token);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public void logout(String tokenOrHeader) {
        if (org.springframework.util.StringUtils.hasText(tokenOrHeader)) {
            String token = tokenOrHeader.startsWith("Bearer ") ? tokenOrHeader.substring(7) : tokenOrHeader;
            jwtTokenRepository.findByToken(token).ifPresent(jwtTokenRepository::delete);
        }
        SecurityContextHolder.clearContext();
    }

    @Transactional
    public void logoutUser(Long userId) {
        if (userId != null) {
            try {
                jwtTokenRepository.deleteByUserUserId(userId);
            } catch (Exception ignored) {
            }
        }
        SecurityContextHolder.clearContext();
    }

    @Transactional
    public void logoutUser(User user) {
        if (user != null) {
            try {
                jwtTokenRepository.deleteByUser(user);
            } catch (Exception ignored) {
            }
        }
        SecurityContextHolder.clearContext();
    }

    public String generateOtp(ForgotPasswordRequest request) {
        User user = userRepository.findByEmailOrMobileNumber(request.getEmailOrMobile(), request.getEmailOrMobile())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with provided email/mobile"));
        
        // Mock OTP generation (fixed 6-digit OTP for testing convenience: 123456)
        return "123456";
    }

    public boolean verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmailOrMobileNumber(request.getEmailOrMobile(), request.getEmailOrMobile())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with provided email/mobile"));
        
        return "123456".equals(request.getOtp());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!"123456".equals(request.getOtp())) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Confirm password does not match new password");
        }

        User user = userRepository.findByEmailOrMobileNumber(request.getEmailOrMobile(), request.getEmailOrMobile())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with provided email/mobile"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Business rule: Password reset invalidates all previous sessions
        jwtTokenRepository.deleteByUser(user);
    }

    @Transactional
    public void changePassword(String currentEmail, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Confirm password does not match new password");
        }

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Authenticated user context not found"));
    }

    private void saveTokenForUser(User user, String tokenStr) {
        JwtToken token = JwtToken.builder()
                .user(user)
                .token(tokenStr)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
        jwtTokenRepository.save(token);
    }
}
