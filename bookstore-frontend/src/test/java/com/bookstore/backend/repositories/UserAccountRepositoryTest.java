package com.bookstore.backend.repositories;

import com.bookstore.backend.entities.UserAccountEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ActiveProfiles("test")
@Import(UserAccountRepository.class)
class UserAccountRepositoryTest {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    void shouldSaveUserAccount() {
        UserAccountEntity userAccount = UserAccountEntity.builder()
                .userId(UUID.randomUUID().toString())
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("hashedPassword123")
                .blocked(false)
                .build();

        UserAccountEntity savedAccount = userAccountRepository.saveUserAccount(userAccount);

        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.getUserId()).isEqualTo(userAccount.getUserId());
        assertThat(savedAccount.getEmail()).isEqualTo(userAccount.getEmail());
        assertThat(savedAccount.getFirstName()).isEqualTo(userAccount.getFirstName());
        assertThat(savedAccount.getLastName()).isEqualTo(userAccount.getLastName());
        assertThat(savedAccount.getBlocked()).isFalse();
        assertThat(savedAccount.getCreatedAt()).isNotNull();
        assertThat(savedAccount.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindUserByUserId() {
        UserAccountEntity userAccount = UserAccountEntity.builder()
                .userId(UUID.randomUUID().toString())
                .email("findtest@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .password("hashedPassword456")
                .blocked(false)
                .build();

        userAccountRepository.saveUserAccount(userAccount);

        Optional<UserAccountEntity> foundAccount = userAccountRepository.findByUserId(userAccount.getUserId());

        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getUserId()).isEqualTo(userAccount.getUserId());
        assertThat(foundAccount.get().getEmail()).isEqualTo(userAccount.getEmail());
    }

    @Test
    void shouldReturnEmptyWhenUserIdNotFound() {
        Optional<UserAccountEntity> foundAccount = userAccountRepository.findByUserId("non-existent-id");

        assertThat(foundAccount).isEmpty();
    }

    @Test
    void shouldFindUserByEmail() {
        UserAccountEntity userAccount = UserAccountEntity.builder()
                .userId(UUID.randomUUID().toString())
                .email("emailtest@example.com")
                .firstName("Bob")
                .lastName("Johnson")
                .password("hashedPassword789")
                .blocked(false)
                .build();

        userAccountRepository.saveUserAccount(userAccount);

        Optional<UserAccountEntity> foundAccount = userAccountRepository.findByEmail(userAccount.getEmail());

        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getEmail()).isEqualTo(userAccount.getEmail());
        assertThat(foundAccount.get().getUserId()).isEqualTo(userAccount.getUserId());
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        Optional<UserAccountEntity> foundAccount = userAccountRepository.findByEmail("nonexistent@example.com");

        assertThat(foundAccount).isEmpty();
    }

    @Test
    void shouldEnforceUniqueEmailConstraint() {
        String email = "duplicate@example.com";

        UserAccountEntity userAccount1 = UserAccountEntity.builder()
                .userId(UUID.randomUUID().toString())
                .email(email)
                .firstName("User")
                .lastName("One")
                .password("password1")
                .blocked(false)
                .build();

        userAccountRepository.saveUserAccount(userAccount1);

        UserAccountEntity userAccount2 = UserAccountEntity.builder()
                .userId(UUID.randomUUID().toString())
                .email(email)
                .firstName("User")
                .lastName("Two")
                .password("password2")
                .blocked(false)
                .build();

        try {
            userAccountRepository.saveUserAccount(userAccount2);
            assertThat(true).as("Should have thrown exception for duplicate email").isFalse();
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }

    @Test
    void shouldSaveAndRetrieveBCryptEncodedPassword() {
        String bcryptPassword = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

        UserAccountEntity userAccount = UserAccountEntity.builder()
                .userId(UUID.randomUUID().toString())
                .email("bcrypt@example.com")
                .firstName("BCrypt")
                .lastName("User")
                .password(bcryptPassword)
                .blocked(false)
                .build();

        UserAccountEntity savedAccount = userAccountRepository.saveUserAccount(userAccount);

        assertThat(savedAccount.getPassword()).isNotNull();
        assertThat(savedAccount.getPassword()).isEqualTo(bcryptPassword);
        assertThat(savedAccount.getPassword()).startsWith("$2a$");

        Optional<UserAccountEntity> retrievedAccount = userAccountRepository.findByUserId(userAccount.getUserId());

        assertThat(retrievedAccount).isPresent();
        assertThat(retrievedAccount.get().getPassword()).isEqualTo(bcryptPassword);
    }
}
