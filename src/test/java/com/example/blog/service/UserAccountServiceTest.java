package com.example.blog.service;

import com.example.blog.dto.AccountForm;
import com.example.blog.model.UserAccount;
import com.example.blog.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserAccountService userAccountService;

    private AccountForm form;

    @BeforeEach
    void setUp() {
        form = new AccountForm();
        form.setUsername("user");
        form.setPassword("secret");
        form.setRole("ADMIN");
    }

    @Test
    void findAllReturnsUsersSortedByUsernameIgnoringCase() {
        UserAccount a = new UserAccount();
        a.setUsername("zeta");
        UserAccount b = new UserAccount();
        b.setUsername("Alpha");
        UserAccount c = new UserAccount();
        c.setUsername("beta");

        when(userAccountRepository.findAll()).thenReturn(List.of(a, b, c));

        List<UserAccount> result = userAccountService.findAll();

        assertEquals(List.of(b, c, a), result);
    }

    @Test
    void createAccountThrowsWhenUsernameExists() {
        when(userAccountRepository.existsByUsername("user")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userAccountService.createAccount(form));
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void createAccountThrowsWhenPasswordBlank() {
        when(userAccountRepository.existsByUsername("user")).thenReturn(false);
        form.setPassword("   ");

        assertThrows(IllegalArgumentException.class, () -> userAccountService.createAccount(form));
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void createAccountEncodesPasswordAndSaves() {
        when(userAccountRepository.existsByUsername("user")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserAccount account = userAccountService.createAccount(form);

        assertEquals("user", account.getUsername());
        assertEquals("encoded", account.getPassword());
        assertEquals(UserAccount.Role.ADMIN, account.getRole());
        verify(userAccountRepository).save(account);
    }

    @Test
    void updateAccountThrowsWhenNotFound() {
        when(userAccountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userAccountService.updateAccount(1L, form));
    }

    @Test
    void updateAccountThrowsWhenUsernameTakenByAnotherUser() {
        UserAccount existing = new UserAccount();
        existing.setId(1L);
        existing.setUsername("original");
        existing.setPassword("encoded");
        existing.setRole(UserAccount.Role.ADMIN);

        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userAccountRepository.existsByUsername("user")).thenReturn(true);

        form.setUsername("user");

        assertThrows(IllegalArgumentException.class, () -> userAccountService.updateAccount(1L, form));
    }

    @Test
    void updateAccountUpdatesFieldsAndKeepsPasswordWhenBlank() {
        UserAccount existing = new UserAccount();
        existing.setId(1L);
        existing.setUsername("user");
        existing.setPassword("encoded");
        existing.setRole(UserAccount.Role.ADMIN);

        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(existing));

        form.setPassword("  ");
        form.setRole("EDITOR");

        UserAccount updated = userAccountService.updateAccount(1L, form);

        assertSame(existing, updated);
        assertEquals("user", existing.getUsername());
        assertEquals("encoded", existing.getPassword());
        assertEquals(UserAccount.Role.EDITOR, existing.getRole());
    }

    @Test
    void updateAccountEncodesPasswordWhenProvided() {
        UserAccount existing = new UserAccount();
        existing.setId(1L);
        existing.setUsername("user");
        existing.setPassword("old");
        existing.setRole(UserAccount.Role.EDITOR);

        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newpass")).thenReturn("encoded-new");

        form.setPassword("newpass");
        form.setRole("ADMIN");

        userAccountService.updateAccount(1L, form);

        assertEquals("encoded-new", existing.getPassword());
        assertEquals(UserAccount.Role.ADMIN, existing.getRole());
    }

    @Test
    void deleteAccountThrowsWhenLastAdmin() {
        UserAccount existing = new UserAccount();
        existing.setId(2L);
        existing.setRole(UserAccount.Role.ADMIN);

        when(userAccountRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(userAccountRepository.countByRole(UserAccount.Role.ADMIN)).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> userAccountService.deleteAccount(2L));
        verify(userAccountRepository, never()).delete(any());
    }

    @Test
    void deleteAccountDeletesWhenMoreAdminsRemain() {
        UserAccount existing = new UserAccount();
        existing.setId(3L);
        existing.setRole(UserAccount.Role.ADMIN);

        when(userAccountRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(userAccountRepository.countByRole(UserAccount.Role.ADMIN)).thenReturn(2L);

        userAccountService.deleteAccount(3L);

        verify(userAccountRepository).delete(existing);
    }

    @Test
    void deleteAccountDeletesNonAdmin() {
        UserAccount existing = new UserAccount();
        existing.setId(4L);
        existing.setRole(UserAccount.Role.EDITOR);

        when(userAccountRepository.findById(4L)).thenReturn(Optional.of(existing));

        userAccountService.deleteAccount(4L);

        verify(userAccountRepository).delete(existing);
    }

    @Test
    void findByIdDelegatesToRepository() {
        Optional<UserAccount> optional = Optional.of(new UserAccount());
        when(userAccountRepository.findById(9L)).thenReturn(optional);

        assertSame(optional, userAccountService.findById(9L));
    }
}
