package com.example.blog.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AccountFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validFormProducesNoViolations() {
        AccountForm form = new AccountForm();
        form.setUsername("user");
        form.setPassword("secret");
        form.setRole("ADMIN");

        Set<ConstraintViolation<AccountForm>> violations = validator.validate(form);
        assertThat(violations).isEmpty();
    }

    @Test
    void blankUsernameFailsValidation() {
        AccountForm form = new AccountForm();
        form.setUsername("   ");
        form.setRole("ADMIN");

        Set<ConstraintViolation<AccountForm>> violations = validator.validate(form);

        assertThat(violations)
                .anySatisfy(violation -> {
                    assertThat(violation.getPropertyPath().toString()).isEqualTo("username");
                    assertThat(violation.getMessage()).isEqualTo("ユーザー名を入力してください");
                });
    }

    @Test
    void blankRoleFailsValidation() {
        AccountForm form = new AccountForm();
        form.setUsername("user");
        form.setRole("   ");

        Set<ConstraintViolation<AccountForm>> violations = validator.validate(form);

        assertThat(violations)
                .anySatisfy(violation -> {
                    assertThat(violation.getPropertyPath().toString()).isEqualTo("role");
                    assertThat(violation.getMessage()).isEqualTo("ロールを選択してください");
                });
    }
}
