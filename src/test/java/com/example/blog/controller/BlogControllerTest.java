package com.example.blog.controller;

import com.example.blog.model.Article;
import com.example.blog.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(BlogController.class)
@AutoConfigureMockMvc(addFilters = false)
class BlogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    @Test
    void indexDisplaysPublishedArticles() throws Exception {
        Article article = new Article();
        article.setPublished(true);
        when(articleService.findPublishedArticles()).thenReturn(List.of(article));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("blog/index"))
                .andExpect(model().attribute("articles", hasSize(1)));
    }

    @Test
    void showDisplaysRequestedArticle() throws Exception {
        Article article = new Article();
        article.setTitle("Title");
        article.setPublished(true);
        when(articleService.findBySlug("sample"))
                .thenReturn(Optional.of(article));

        mockMvc.perform(get("/posts/sample"))
                .andExpect(status().isOk())
                .andExpect(view().name("blog/article"))
                .andExpect(model().attribute("article", article));
    }

    @Test
    void showReturnsNotFoundWhenArticleMissing() throws Exception {
        when(articleService.findBySlug("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/posts/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void showReturnsNotFoundForUnpublishedArticle() throws Exception {
        Article article = new Article();
        article.setPublished(false);
        when(articleService.findBySlug("draft")).thenReturn(Optional.of(article));

        mockMvc.perform(get("/posts/draft"))
                .andExpect(status().isNotFound());
    }
}
