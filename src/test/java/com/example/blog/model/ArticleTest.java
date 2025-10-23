package com.example.blog.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleTest {

    @Test
    void prePersistInitializesTimestampsAndPublishedAtWhenPublished() {
        Article article = new Article();
        article.setPublished(true);

        article.prePersist();

        assertThat(article.getCreatedAt()).isNotNull();
        assertThat(article.getUpdatedAt()).isNotNull();
        assertThat(article.getPublishedAt()).isEqualTo(article.getCreatedAt());
        assertThat(article.getUpdatedAt()).isEqualTo(article.getCreatedAt());
    }

    @Test
    void prePersistLeavesPublishedAtNullWhenUnpublished() {
        Article article = new Article();
        article.setPublished(false);

        article.prePersist();

        assertThat(article.getCreatedAt()).isNotNull();
        assertThat(article.getUpdatedAt()).isNotNull();
        assertThat(article.getPublishedAt()).isNull();
    }

    @Test
    void preUpdateSetsUpdatedAtAndPublishedAtWhenMissing() {
        Article article = new Article();
        article.setPublished(true);
        article.setPublishedAt(null);

        article.preUpdate();

        assertThat(article.getUpdatedAt()).isNotNull();
        assertThat(article.getPublishedAt()).isEqualTo(article.getUpdatedAt());
    }

    @Test
    void preUpdateDoesNotOverrideExistingPublishedAt() {
        Article article = new Article();
        article.setPublished(true);
        LocalDateTime previous = LocalDateTime.now().minusDays(1);
        article.setPublishedAt(previous);

        article.preUpdate();

        assertThat(article.getUpdatedAt()).isNotNull();
        assertThat(article.getPublishedAt()).isEqualTo(previous);
    }

    @Test
    void preUpdateDoesNotSetPublishedAtWhenUnpublished() {
        Article article = new Article();
        article.setPublished(false);
        article.setPublishedAt(null);

        article.preUpdate();

        assertThat(article.getUpdatedAt()).isNotNull();
        assertThat(article.getPublishedAt()).isNull();
    }
}
