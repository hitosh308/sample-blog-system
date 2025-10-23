package com.example.blog.service;

import com.example.blog.dto.ArticleForm;
import com.example.blog.model.Article;
import com.example.blog.repository.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public List<Article> findAllForAdmin() {
        return articleRepository.findAllByOrderByUpdatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Article> findPublishedArticles() {
        return articleRepository.findByPublishedTrueOrderByPublishedAtDesc();
    }

    @Transactional(readOnly = true)
    public Optional<Article> findById(Long id) {
        return articleRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Article> findBySlug(String slug) {
        return articleRepository.findBySlug(slug);
    }

    public Article createArticle(ArticleForm form) {
        Article article = new Article();
        applyForm(form, article);
        return articleRepository.save(article);
    }

    public Article updateArticle(Long id, ArticleForm form) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("記事が見つかりません: " + id));
        applyForm(form, article);
        return article;
    }

    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }

    private void applyForm(ArticleForm form, Article article) {
        article.setTitle(form.getTitle());
        article.setSummary(form.getSummary());
        article.setContent(form.getContent());
        article.setPublished(form.isPublished());

        String slugCandidate = StringUtils.hasText(form.getSlug()) ? form.getSlug() : form.getTitle();
        String normalizedSlug = normalizeSlug(slugCandidate);
        String uniqueSlug = ensureUniqueSlug(normalizedSlug, article.getId());
        article.setSlug(uniqueSlug);

        if (article.isPublished()) {
            if (article.getPublishedAt() == null) {
                article.setPublishedAt(LocalDateTime.now());
            }
        } else {
            article.setPublishedAt(null);
        }
    }

    private String ensureUniqueSlug(String slug, Long currentId) {
        String base = slug;
        if (!StringUtils.hasText(base)) {
            base = UUID.randomUUID().toString();
        }
        String candidate = base;
        int counter = 1;
        while (true) {
            Optional<Article> existing = articleRepository.findBySlug(candidate);
            if (existing.isEmpty() || existing.get().getId().equals(currentId)) {
                return candidate;
            }
            candidate = base + "-" + counter++;
        }
    }

    private String normalizeSlug(String input) {
        if (!StringUtils.hasText(input)) {
            return UUID.randomUUID().toString();
        }
        String value = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        value = value.toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9]+", "-");
        value = value.replaceAll("^-+", "").replaceAll("-+$", "");
        if (!StringUtils.hasText(value)) {
            return UUID.randomUUID().toString();
        }
        return value;
    }
}
