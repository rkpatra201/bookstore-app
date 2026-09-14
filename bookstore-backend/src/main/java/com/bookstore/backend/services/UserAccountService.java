package com.bookstore.backend.services;

import com.bookstore.backend.dtos.LoginRequest;
import com.bookstore.backend.dtos.LoginResponse;
import com.bookstore.backend.dtos.UserAccount;
import com.bookstore.backend.entities.UserAccountEntity;
import com.bookstore.backend.enums.UserAccountError;
import com.bookstore.backend.exceptions.UserAccountException;
import com.bookstore.backend.mappers.UserAccountMapper;
import com.bookstore.backend.repositories.UserAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final UserAccountMapper userAccountMapper = UserAccountMapper.INSTANCE;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public UserAccountService(UserAccountRepository userAccountRepository, JwtService jwtService) {
        this.userAccountRepository = userAccountRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public UserAccount createUserAccount(UserAccount userAccount) {
        validateUserAccount(userAccount);

        if (userAccountRepository.findByEmail(userAccount.getEmail()).isPresent()) {
            throw new UserAccountException(UserAccountError.EMAIL_ALREADY_EXISTS);
        }

        UserAccountEntity entity = userAccountMapper.toEntity(userAccount);
        entity.setUserId(UUID.randomUUID().toString());
        entity.setBlocked(false);

        // Encode password using BCrypt
        String encodedPassword = passwordEncoder.encode(userAccount.getPassword());
        entity.setPassword(encodedPassword);

        UserAccountEntity savedEntity = userAccountRepository.saveUserAccount(entity);
        return userAccountMapper.toDto(savedEntity);
    }

    public UserAccount findByUserId(String userId) {
        UserAccountEntity entity = userAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new UserAccountException(UserAccountError.USER_NOT_FOUND));
        return userAccountMapper.toDto(entity);
    }

    public UserAccount findByUserEmail(String email) {
        UserAccountEntity entity = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new UserAccountException(UserAccountError.USER_NOT_FOUND));
        return userAccountMapper.toDto(entity);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public LoginResult login(LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());

        validateLoginRequest(loginRequest);

        Optional<UserAccountEntity> userAccountOpt = userAccountRepository.findByEmail(loginRequest.getEmail());

        if (userAccountOpt.isEmpty()) {
            log.error("User account not found with email: {}", loginRequest.getEmail());
            throw new UserAccountException(UserAccountError.USER_NOT_FOUND_WITH_EMAIL, loginRequest.getEmail());
        }

        UserAccountEntity userAccount = userAccountOpt.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(), userAccount.getPassword())) {
            log.error("Login attempt failed for email: {} - Invalid password", loginRequest.getEmail());
            throw new UserAccountException(UserAccountError.LOGIN_FAILED);
        }

        String token = jwtService.generateToken(
                userAccount.getUserId(),
                userAccount.getEmail(),
                userAccount.getFirstName(),
                userAccount.getLastName()
        );
        log.info("Login successful for email: {} with userId: {}", loginRequest.getEmail(), userAccount.getUserId());

        LoginResponse loginResponse = LoginResponse.builder()
                .userId(userAccount.getUserId())
                .email(userAccount.getEmail())
                .firstName(userAccount.getFirstName())
                .lastName(userAccount.getLastName())
                .build();

        return new LoginResult(token, loginResponse);
    }

    @lombok.Value
    public static class LoginResult {
        String token;
        LoginResponse loginResponse;
    }

    private void validateLoginRequest(LoginRequest loginRequest) {
        if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
            throw new UserAccountException(UserAccountError.EMAIL_REQUIRED);
        }

        if (!EMAIL_PATTERN.matcher(loginRequest.getEmail()).matches()) {
            throw new UserAccountException(UserAccountError.INVALID_EMAIL_FORMAT);
        }

        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            throw new UserAccountException(UserAccountError.PASSWORD_REQUIRED);
        }
    }

    private void validateUserAccount(UserAccount userAccount) {
        if (userAccount.getEmail() == null || userAccount.getEmail().trim().isEmpty()) {
            throw new UserAccountException(UserAccountError.EMAIL_REQUIRED);
        }

        if (!EMAIL_PATTERN.matcher(userAccount.getEmail()).matches()) {
            throw new UserAccountException(UserAccountError.INVALID_EMAIL_FORMAT);
        }

        if (userAccount.getFirstName() == null || userAccount.getFirstName().trim().isEmpty()) {
            throw new UserAccountException(UserAccountError.FIRST_NAME_REQUIRED);
        }

        if (userAccount.getPassword() == null || userAccount.getPassword().trim().isEmpty()) {
            throw new UserAccountException(UserAccountError.PASSWORD_REQUIRED);
        }
    }
}
