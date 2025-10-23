package com.example.blog.repository;

import com.example.blog.model.UserAccount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserAccountRepositoryTest {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    void findByUsernameReturnsMatchingAccount() {
        UserAccount account = createAccount("admin", UserAccount.Role.ADMIN);

        Optional<UserAccount> found = userAccountRepository.findByUsername("admin");
        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo(UserAccount.Role.ADMIN);
        assertThat(userAccountRepository.existsByUsername("admin")).isTrue();
        assertThat(userAccountRepository.existsByUsername("missing")).isFalse();
    }

    @Test
    void countByRoleReturnsNumberOfAccountsWithRole() {
        createAccount("admin1", UserAccount.Role.ADMIN);
        createAccount("admin2", UserAccount.Role.ADMIN);
        createAccount("editor", UserAccount.Role.EDITOR);

        long adminCount = userAccountRepository.countByRole(UserAccount.Role.ADMIN);
        long editorCount = userAccountRepository.countByRole(UserAccount.Role.EDITOR);

        assertThat(adminCount).isEqualTo(2);
        assertThat(editorCount).isEqualTo(1);
    }

    private UserAccount createAccount(String username, UserAccount.Role role) {
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPassword("password");
        account.setRole(role);
        return userAccountRepository.save(account);
    }
}
