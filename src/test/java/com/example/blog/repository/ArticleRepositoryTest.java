package com.example.blog.repository;

import com.example.blog.model.Article;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findBySlugReturnsMatchingArticle() {
        Article article = createArticle("Title", "unique-slug", true, LocalDateTime.now());

        Optional<Article> found = articleRepository.findBySlug("unique-slug");
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Title");
        assertThat(articleRepository.existsBySlug("unique-slug")).isTrue();
        assertThat(articleRepository.existsBySlug("missing")).isFalse();
    }

    @Test
    void findByPublishedTrueOrderByPublishedAtDescReturnsOrderedPublishedArticles() {
        LocalDateTime now = LocalDateTime.now();
        Article newer = createArticle("Newer", "newer", true, now);
        Article older = createArticle("Older", "older", true, now.minusDays(1));
        createArticle("Draft", "draft", false, null);

        List<Article> articles = articleRepository.findByPublishedTrueOrderByPublishedAtDesc();

        assertThat(articles).extracting(Article::getSlug)
                .containsExactly(newer.getSlug(), older.getSlug());
    }

    @Test
    void findAllByOrderByUpdatedAtDescReturnsArticlesSortedByUpdatedAt() {
        LocalDateTime now = LocalDateTime.now();
        Article first = createArticle("First", "first", true, now.minusDays(2));
        Article second = createArticle("Second", "second", true, now.minusDays(3));

        setUpdatedAt(first.getId(), now.minusHours(2));
        setUpdatedAt(second.getId(), now.minusHours(1));
        entityManager.clear();

        List<Article> articles = articleRepository.findAllByOrderByUpdatedAtDesc();

        assertThat(articles).extracting(Article::getSlug)
                .containsExactly(second.getSlug(), first.getSlug());
    }

    private Article createArticle(String title, String slug, boolean published, LocalDateTime publishedAt) {
        Article article = new Article();
        article.setTitle(title);
        article.setSlug(slug);
        article.setSummary("Summary");
        article.setContent("Content");
        article.setPublished(published);
        article.setPublishedAt(publishedAt);
        return articleRepository.save(article);
    }

    private void setUpdatedAt(Long id, LocalDateTime updatedAt) {
        entityManager.getEntityManager()
                .createQuery("update Article a set a.updatedAt = :updatedAt where a.id = :id")
                .setParameter("updatedAt", updatedAt)
                .setParameter("id", id)
                .executeUpdate();
    }
}
