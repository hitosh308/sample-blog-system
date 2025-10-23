package com.example.blog.controller;

import com.example.blog.dto.AccountForm;
import com.example.blog.model.UserAccount;
import com.example.blog.service.UserAccountService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AdminAccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserAccountService userAccountService;

    @Test
    void listDisplaysAccountsAndRoles() throws Exception {
        UserAccount account = new UserAccount();
        account.setId(1L);
        account.setUsername("admin");
        account.setRole(UserAccount.Role.ADMIN);
        when(userAccountService.findAll()).thenReturn(List.of(account));

        mockMvc.perform(get("/admin/accounts"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/accounts/list"))
                .andExpect(model().attribute("accounts", hasSize(1)))
                .andExpect(model().attribute("roles", arrayContainingInAnyOrder(UserAccount.Role.values())));
    }

    @Test
    void newAccountProvidesDefaultForm() throws Exception {
        mockMvc.perform(get("/admin/accounts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/accounts/form"))
                .andExpect(model().attribute("accountForm", hasProperty("role", is(UserAccount.Role.EDITOR.name()))))
                .andExpect(model().attribute("formTitle", "新規アカウント"));
    }

    @Test
    void createValidationErrorsReturnForm() throws Exception {
        mockMvc.perform(post("/admin/accounts")
                        .param("username", "")
                        .param("role", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/accounts/form"))
                .andExpect(model().attributeHasFieldErrors("accountForm", "username", "role"));

        verify(userAccountService, never()).createAccount(any());
    }

    @Test
    void createSuccessfulRedirects() throws Exception {
        mockMvc.perform(post("/admin/accounts")
                        .param("username", "newuser")
                        .param("password", "secret")
                        .param("role", "ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/accounts"))
                .andExpect(flash().attribute("message", "アカウントを作成しました"));

        ArgumentCaptor<AccountForm> captor = ArgumentCaptor.forClass(AccountForm.class);
        verify(userAccountService).createAccount(captor.capture());
        AccountForm submitted = captor.getValue();
        assertThat(submitted.getUsername()).isEqualTo("newuser");
        assertThat(submitted.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void createHandlesServiceError() throws Exception {
        when(userAccountService.createAccount(any(AccountForm.class)))
                .thenThrow(new IllegalArgumentException("duplicate"));

        mockMvc.perform(post("/admin/accounts")
                        .param("username", "newuser")
                        .param("password", "secret")
                        .param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/accounts/form"))
                .andExpect(model().attributeHasErrors("accountForm"))
                .andExpect(model().attribute("formTitle", "新規アカウント"));
    }

    @Test
    void editPopulatesFormWhenAccountExists() throws Exception {
        UserAccount account = new UserAccount();
        account.setId(5L);
        account.setUsername("editor");
        account.setRole(UserAccount.Role.EDITOR);
        when(userAccountService.findById(5L)).thenReturn(Optional.of(account));

        mockMvc.perform(get("/admin/accounts/5/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/accounts/form"))
                .andExpect(model().attribute("accountForm", allOf(
                        hasProperty("id", is(5L)),
                        hasProperty("username", is("editor")),
                        hasProperty("role", is(UserAccount.Role.EDITOR.name()))
                )))
                .andExpect(model().attribute("formTitle", "アカウント編集"));
    }

    @Test
    void editRedirectsWhenAccountMissing() throws Exception {
        when(userAccountService.findById(8L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/accounts/8/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/accounts"))
                .andExpect(flash().attribute("error", "アカウントが見つかりません"));
    }

    @Test
    void updateValidationErrorsReturnForm() throws Exception {
        mockMvc.perform(post("/admin/accounts/3")
                        .param("username", "")
                        .param("role", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/accounts/form"))
                .andExpect(model().attributeHasFieldErrors("accountForm", "username", "role"));

        verify(userAccountService, never()).updateAccount(eq(3L), any());
    }

    @Test
    void updateSuccessfulRedirects() throws Exception {
        when(userAccountService.updateAccount(eq(3L), any(AccountForm.class)))
                .thenReturn(new UserAccount());

        mockMvc.perform(post("/admin/accounts/3")
                        .param("username", "editor")
                        .param("password", "")
                        .param("role", "EDITOR"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/accounts"))
                .andExpect(flash().attribute("message", "アカウントを更新しました"));

        verify(userAccountService).updateAccount(eq(3L), any(AccountForm.class));
    }

    @Test
    void updateHandlesServiceError() throws Exception {
        when(userAccountService.updateAccount(eq(4L), any(AccountForm.class)))
                .thenThrow(new IllegalArgumentException("error"));

        mockMvc.perform(post("/admin/accounts/4")
                        .param("username", "editor")
                        .param("password", "")
                        .param("role", "EDITOR"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/accounts/form"))
                .andExpect(model().attributeHasErrors("accountForm"))
                .andExpect(model().attribute("formTitle", "アカウント編集"));
    }

    @Test
    void deleteSuccessfulRedirects() throws Exception {
        mockMvc.perform(post("/admin/accounts/2/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/accounts"))
                .andExpect(flash().attribute("message", "アカウントを削除しました"));

        verify(userAccountService).deleteAccount(2L);
    }

    @Test
    void deleteHandlesServiceException() throws Exception {
        doThrow(new IllegalStateException("error")).when(userAccountService).deleteAccount(6L);

        mockMvc.perform(post("/admin/accounts/6/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/accounts"))
                .andExpect(flash().attribute("error", "error"));
    }
}
