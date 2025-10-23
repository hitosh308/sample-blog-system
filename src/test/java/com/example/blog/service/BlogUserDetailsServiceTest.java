package com.example.blog.service;

import com.example.blog.model.UserAccount;
import com.example.blog.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogUserDetailsServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private BlogUserDetailsService blogUserDetailsService;

    @Test
    void loadUserByUsernameBuildsUserDetails() {
        UserAccount account = new UserAccount();
        account.setUsername("alice");
        account.setPassword("encoded");
        account.setRole(UserAccount.Role.ADMIN);

        when(userAccountRepository.findByUsername("alice")).thenReturn(Optional.of(account));

        UserDetails details = blogUserDetailsService.loadUserByUsername("alice");

        assertEquals("alice", details.getUsername());
        assertEquals("encoded", details.getPassword());
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsernameThrowsWhenUserMissing() {
        when(userAccountRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> blogUserDetailsService.loadUserByUsername("missing"));
    }
}
