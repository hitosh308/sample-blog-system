package com.example.blog.data;

import com.example.blog.model.UserAccount;
import com.example.blog.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void runCreatesAdminWhenNoneExists() throws Exception {
        when(userAccountRepository.countByRole(UserAccount.Role.ADMIN)).thenReturn(0L);
        when(passwordEncoder.encode("admin")).thenReturn("encoded-admin");

        dataInitializer.run();

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(captor.capture());
        UserAccount saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("admin");
        assertThat(saved.getPassword()).isEqualTo("encoded-admin");
        assertThat(saved.getRole()).isEqualTo(UserAccount.Role.ADMIN);
    }

    @Test
    void runDoesNothingWhenAdminExists() throws Exception {
        when(userAccountRepository.countByRole(UserAccount.Role.ADMIN)).thenReturn(1L);

        dataInitializer.run();

        verify(userAccountRepository, never()).save(any());
    }
}
