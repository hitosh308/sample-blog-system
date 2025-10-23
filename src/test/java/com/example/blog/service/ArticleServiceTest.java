package com.example.blog.service;

import com.example.blog.dto.ArticleForm;
import com.example.blog.model.Article;
import com.example.blog.repository.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private ArticleService articleService;

    private ArticleForm baseForm;

    @BeforeEach
    void setUp() {
        baseForm = new ArticleForm();
        baseForm.setTitle("Café au Lait");
        baseForm.setSummary("Summary");
        baseForm.setContent("Content");
    }

    @Test
    void createArticleSetsNormalizedSlugAndPublishedAt() {
        baseForm.setPublished(true);
        when(articleRepository.findBySlug("cafe-au-lait")).thenReturn(Optional.empty());
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Article result = articleService.createArticle(baseForm);

        assertEquals("Café au Lait", result.getTitle());
        assertEquals("Summary", result.getSummary());
        assertEquals("Content", result.getContent());
        assertEquals("cafe-au-lait", result.getSlug());
        assertNotNull(result.getPublishedAt(), "Published article should have publishedAt set");
        assertThat(result.getPublishedAt()).isAfter(LocalDateTime.now().minusMinutes(1));
    }

    @Test
    void createArticleGeneratesUniqueSlugWhenDuplicateExists() {
        baseForm.setSlug("duplicate");
        baseForm.setPublished(false);
        Article existing = new Article();
        existing.setId(100L);

        when(articleRepository.findBySlug("duplicate")).thenReturn(Optional.of(existing));
        when(articleRepository.findBySlug("duplicate-1")).thenReturn(Optional.empty());
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Article result = articleService.createArticle(baseForm);

        assertEquals("duplicate-1", result.getSlug());
        assertNull(result.getPublishedAt());
        verify(articleRepository).save(result);
    }

    @Test
    void updateArticleAppliesChangesAndKeepsExistingSlug() {
        Article existing = new Article();
        existing.setId(5L);
        existing.setSlug("existing-slug");
        existing.setTitle("Old");
        existing.setSummary("Old summary");
        existing.setContent("Old content");
        existing.setPublished(false);
        existing.setPublishedAt(null);

        ArticleForm form = new ArticleForm();
        form.setTitle("Updated Title");
        form.setSummary("Updated Summary");
        form.setContent("Updated Content");
        form.setSlug("existing-slug");
        form.setPublished(true);

        when(articleRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(articleRepository.findBySlug("existing-slug")).thenReturn(Optional.of(existing));

        Article updated = articleService.updateArticle(5L, form);

        assertSame(existing, updated);
        assertEquals("Updated Title", existing.getTitle());
        assertEquals("Updated Summary", existing.getSummary());
        assertEquals("Updated Content", existing.getContent());
        assertEquals("existing-slug", existing.getSlug());
        assertNotNull(existing.getPublishedAt());
    }

    @Test
    void updateArticleUnpublishClearsPublishedAt() {
        Article existing = new Article();
        existing.setId(10L);
        existing.setSlug("keep-slug");
        existing.setTitle("Old");
        existing.setSummary("Old summary");
        existing.setContent("Old content");
        existing.setPublished(true);
        LocalDateTime previousPublishedAt = LocalDateTime.now().minusDays(2);
        existing.setPublishedAt(previousPublishedAt);

        ArticleForm form = new ArticleForm();
        form.setTitle("Keep Slug");
        form.setSummary("New summary");
        form.setContent("New content");
        form.setSlug("keep-slug");
        form.setPublished(false);

        when(articleRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(articleRepository.findBySlug("keep-slug")).thenReturn(Optional.of(existing));

        articleService.updateArticle(10L, form);

        assertEquals("keep-slug", existing.getSlug());
        assertNull(existing.getPublishedAt(), "Unpublished article should not have publishedAt");
        assertEquals("New summary", existing.getSummary());
        assertEquals("New content", existing.getContent());
    }

    @Test
    void deleteArticleDelegatesToRepository() {
        articleService.deleteArticle(42L);
        verify(articleRepository).deleteById(42L);
    }

    @Test
    void findersDelegateToRepository() {
        List<Article> articles = List.of(new Article());
        Optional<Article> optional = Optional.of(new Article());

        when(articleRepository.findAllByOrderByUpdatedAtDesc()).thenReturn(articles);
        when(articleRepository.findByPublishedTrueOrderByPublishedAtDesc()).thenReturn(articles);
        when(articleRepository.findById(7L)).thenReturn(optional);
        when(articleRepository.findBySlug("slug")).thenReturn(optional);

        assertSame(articles, articleService.findAllForAdmin());
        assertSame(articles, articleService.findPublishedArticles());
        assertSame(optional, articleService.findById(7L));
        assertSame(optional, articleService.findBySlug("slug"));
    }

    @Test
    void updateArticleThrowsWhenNotFound() {
        when(articleRepository.findById(1L)).thenReturn(Optional.empty());

        ArticleForm form = new ArticleForm();
        form.setTitle("Title");
        form.setSummary("Summary");
        form.setContent("Content");

        assertThrows(IllegalArgumentException.class, () -> articleService.updateArticle(1L, form));
    }
}
