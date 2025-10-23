package com.example.blog.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserAccountTest {

    @Test
    void defaultRoleIsEditor() {
        UserAccount account = new UserAccount();
        assertThat(account.getRole()).isEqualTo(UserAccount.Role.EDITOR);
    }

    @Test
    void settersAndGettersWork() {
        UserAccount account = new UserAccount();
        account.setId(5L);
        account.setUsername("user");
        account.setPassword("secret");
        account.setRole(UserAccount.Role.ADMIN);

        assertThat(account.getId()).isEqualTo(5L);
        assertThat(account.getUsername()).isEqualTo("user");
        assertThat(account.getPassword()).isEqualTo("secret");
        assertThat(account.getRole()).isEqualTo(UserAccount.Role.ADMIN);
    }
}
