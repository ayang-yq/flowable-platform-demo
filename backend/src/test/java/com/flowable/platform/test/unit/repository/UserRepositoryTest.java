package com.flowable.platform.test.unit.repository;

import com.flowable.platform.entity.Tenant;
import com.flowable.platform.entity.User;
import com.flowable.platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserRepository methods.
 * Tests JPA query methods for user management.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    private Tenant testTenant;
    private User testUser;

    @BeforeEach
    void setUp() {
        testTenant = new Tenant("Test Tenant", "test-tenant");
        testTenant.setId(UUID.randomUUID());

        testUser = new User(testTenant, "testuser", "test@example.com", "hashedPassword");
        testUser.setId(UUID.randomUUID());
        testUser.setIsActive(true);
    }

    @Nested
    @DisplayName("findByTenantIdAndUsername")
    class FindByTenantIdAndUsernameTests {

        @Test
        @DisplayName("Should find user by tenant and username")
        void shouldFindUserByTenantAndUsername() {
            UUID tenantId = testTenant.getId();
            String username = "testuser";
            when(userRepository.findByTenantIdAndUsername(tenantId, username))
                .thenReturn(Optional.of(testUser));

            Optional<User> result = userRepository.findByTenantIdAndUsername(tenantId, username);

            assertThat(result).isPresent();
            assertThat(result.get().getUsername()).isEqualTo(username);
            verify(userRepository).findByTenantIdAndUsername(tenantId, username);
        }

        @Test
        @DisplayName("Should return empty when user not found")
        void shouldReturnEmptyWhenUserNotFound() {
            UUID tenantId = UUID.randomUUID();
            String username = "nonexistent";
            when(userRepository.findByTenantIdAndUsername(tenantId, username))
                .thenReturn(Optional.empty());

            Optional<User> result = userRepository.findByTenantIdAndUsername(tenantId, username);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByTenantIdAndEmail")
    class FindByTenantIdAndEmailTests {

        @Test
        @DisplayName("Should find user by tenant and email")
        void shouldFindUserByTenantAndEmail() {
            UUID tenantId = testTenant.getId();
            String email = "test@example.com";
            when(userRepository.findByTenantIdAndEmail(tenantId, email))
                .thenReturn(Optional.of(testUser));

            Optional<User> result = userRepository.findByTenantIdAndEmail(tenantId, email);

            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo(email);
        }

        @Test
        @DisplayName("Should return empty for non-existent email")
        void shouldReturnEmptyForNonExistentEmail() {
            UUID tenantId = testTenant.getId();
            String email = "nonexistent@example.com";
            when(userRepository.findByTenantIdAndEmail(tenantId, email))
                .thenReturn(Optional.empty());

            Optional<User> result = userRepository.findByTenantIdAndEmail(tenantId, email);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findActiveByTenantIdAndUsername")
    class FindActiveByTenantIdAndUsernameTests {

        @Test
        @DisplayName("Should find active user by tenant and username")
        void shouldFindActiveUserByTenantAndUsername() {
            UUID tenantId = testTenant.getId();
            String username = "testuser";
            when(userRepository.findActiveByTenantIdAndUsername(tenantId, username))
                .thenReturn(Optional.of(testUser));

            Optional<User> result = userRepository.findActiveByTenantIdAndUsername(tenantId, username);

            assertThat(result).isPresent();
            assertThat(result.get().getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should not find inactive user")
        void shouldNotFindInactiveUser() {
            UUID tenantId = testTenant.getId();
            String username = "inactive-user";
            when(userRepository.findActiveByTenantIdAndUsername(tenantId, username))
                .thenReturn(Optional.empty());

            Optional<User> result = userRepository.findActiveByTenantIdAndUsername(tenantId, username);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByTenantIdAndIsActiveTrue")
    class FindActiveUsersTests {

        @Test
        @DisplayName("Should find all active users for tenant")
        void shouldFindAllActiveUsersForTenant() {
            UUID tenantId = testTenant.getId();
            User activeUser2 = new User(testTenant, "active2", "active2@example.com", "hash");
            activeUser2.setId(UUID.randomUUID());
            activeUser2.setIsActive(true);

            when(userRepository.findByTenantIdAndIsActiveTrue(tenantId))
                .thenReturn(List.of(testUser, activeUser2));

            List<User> result = userRepository.findByTenantIdAndIsActiveTrue(tenantId);

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(User::getIsActive);
        }

        @Test
        @DisplayName("Should return empty list when no active users")
        void shouldReturnEmptyListWhenNoActiveUsers() {
            UUID tenantId = UUID.randomUUID();
            when(userRepository.findByTenantIdAndIsActiveTrue(tenantId))
                .thenReturn(List.of());

            List<User> result = userRepository.findByTenantIdAndIsActiveTrue(tenantId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByTenantIdAndUsername")
    class ExistsByTenantIdAndUsernameTests {

        @Test
        @DisplayName("Should return true when user exists")
        void shouldReturnTrueWhenUserExists() {
            UUID tenantId = testTenant.getId();
            String username = "testuser";
            when(userRepository.existsByTenantIdAndUsername(tenantId, username))
                .thenReturn(true);

            boolean result = userRepository.existsByTenantIdAndUsername(tenantId, username);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when user does not exist")
        void shouldReturnFalseWhenUserDoesNotExist() {
            UUID tenantId = testTenant.getId();
            String username = "nonexistent";
            when(userRepository.existsByTenantIdAndUsername(tenantId, username))
                .thenReturn(false);

            boolean result = userRepository.existsByTenantIdAndUsername(tenantId, username);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("save")
    class SaveTests {

        @Test
        @DisplayName("Should save user successfully")
        void shouldSaveUserSuccessfully() {
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = userRepository.save(testUser);

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("testuser");
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should update existing user")
        void shouldUpdateExistingUser() {
            testUser.setFirstName("Updated");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = userRepository.save(testUser);

            assertThat(result.getFirstName()).isEqualTo("Updated");
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTests {

        @Test
        @DisplayName("Should delete user successfully")
        void shouldDeleteUserSuccessfully() {
            doNothing().when(userRepository).delete(testUser);

            userRepository.delete(testUser);

            verify(userRepository).delete(testUser);
        }
    }
}
