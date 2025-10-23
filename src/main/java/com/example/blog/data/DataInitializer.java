package com.example.blog.data;

import com.example.blog.model.UserAccount;
import com.example.blog.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userAccountRepository.countByRole(UserAccount.Role.ADMIN) == 0) {
            UserAccount admin = new UserAccount();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole(UserAccount.Role.ADMIN);
            userAccountRepository.save(admin);
            log.info("管理者アカウントを作成しました (ユーザー名: admin, パスワード: admin)");
        }
    }
}
