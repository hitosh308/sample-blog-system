package com.example.blog.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validFormProducesNoViolations() {
        ArticleForm form = new ArticleForm();
        form.setTitle("Title");
        form.setSummary("Summary");
        form.setContent("Content");
        form.setSlug("slug");
        form.setPublished(true);

        Set<ConstraintViolation<ArticleForm>> violations = validator.validate(form);
        assertThat(violations).isEmpty();
    }

    @Test
    void blankTitleFailsValidation() {
        ArticleForm form = new ArticleForm();
        form.setTitle("   ");
        form.setContent("Content");

        Set<ConstraintViolation<ArticleForm>> violations = validator.validate(form);

        assertThat(violations)
                .anySatisfy(violation -> {
                    assertThat(violation.getPropertyPath().toString()).isEqualTo("title");
                    assertThat(violation.getMessage()).isEqualTo("タイトルを入力してください");
                });
    }

    @Test
    void blankContentFailsValidation() {
        ArticleForm form = new ArticleForm();
        form.setTitle("Title");
        form.setContent(" ");

        Set<ConstraintViolation<ArticleForm>> violations = validator.validate(form);

        assertThat(violations)
                .anySatisfy(violation -> {
                    assertThat(violation.getPropertyPath().toString()).isEqualTo("content");
                    assertThat(violation.getMessage()).isEqualTo("本文を入力してください");
                });
    }

    @Test
    void summaryLongerThan500CharactersFailsValidation() {
        ArticleForm form = new ArticleForm();
        form.setTitle("Title");
        form.setContent("Content");
        form.setSummary("a".repeat(501));

        Set<ConstraintViolation<ArticleForm>> violations = validator.validate(form);

        assertThat(violations)
                .anySatisfy(violation -> {
                    assertThat(violation.getPropertyPath().toString()).isEqualTo("summary");
                    assertThat(violation.getMessage()).isEqualTo("サマリーは500文字以内で入力してください");
                });
    }
}
