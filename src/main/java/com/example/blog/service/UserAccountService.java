package com.example.blog.service;

import com.example.blog.dto.AccountForm;
import com.example.blog.model.UserAccount;
import com.example.blog.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserAccount> findAll() {
        return userAccountRepository.findAll().stream()
                .sorted((a, b) -> a.getUsername().compareToIgnoreCase(b.getUsername()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<UserAccount> findById(Long id) {
        return userAccountRepository.findById(id);
    }

    public UserAccount createAccount(AccountForm form) {
        if (userAccountRepository.existsByUsername(form.getUsername())) {
            throw new IllegalArgumentException("すでに存在するユーザー名です");
        }
        if (!StringUtils.hasText(form.getPassword())) {
            throw new IllegalArgumentException("パスワードを入力してください");
        }
        UserAccount account = new UserAccount();
        applyForm(form, account, true);
        return userAccountRepository.save(account);
    }

    public UserAccount updateAccount(Long id, AccountForm form) {
        UserAccount account = userAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("アカウントが見つかりません: " + id));
        if (!account.getUsername().equals(form.getUsername())
                && userAccountRepository.existsByUsername(form.getUsername())) {
            throw new IllegalArgumentException("すでに存在するユーザー名です");
        }
        applyForm(form, account, false);
        return account;
    }

    public void deleteAccount(Long id) {
        UserAccount account = userAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("アカウントが見つかりません: " + id));
        if (account.getRole() == UserAccount.Role.ADMIN
                && userAccountRepository.countByRole(UserAccount.Role.ADMIN) <= 1) {
            throw new IllegalStateException("最後の管理者アカウントは削除できません");
        }
        userAccountRepository.delete(account);
    }

    private void applyForm(AccountForm form, UserAccount account, boolean creating) {
        account.setUsername(form.getUsername());
        account.setRole(UserAccount.Role.valueOf(form.getRole()));
        if (StringUtils.hasText(form.getPassword())) {
            account.setPassword(passwordEncoder.encode(form.getPassword()));
        } else if (creating) {
            throw new IllegalArgumentException("パスワードを入力してください");
        }
    }
}
