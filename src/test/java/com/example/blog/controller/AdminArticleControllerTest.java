package com.example.blog.controller;

import com.example.blog.dto.ArticleForm;
import com.example.blog.model.Article;
import com.example.blog.service.ArticleService;
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

@WebMvcTest(AdminArticleController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    @Test
    void listDisplaysArticles() throws Exception {
        Article article = new Article();
        article.setId(1L);
        article.setTitle("Sample");
        when(articleService.findAllForAdmin()).thenReturn(List.of(article));

        mockMvc.perform(get("/admin/articles"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/articles/list"))
                .andExpect(model().attribute("articles", hasSize(1)));
    }

    @Test
    void newArticleProvidesEmptyForm() throws Exception {
        mockMvc.perform(get("/admin/articles/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/articles/form"))
                .andExpect(model().attributeExists("articleForm"))
                .andExpect(model().attribute("formTitle", "新規記事"));
    }

    @Test
    void createWithValidationErrorsReturnsForm() throws Exception {
        mockMvc.perform(post("/admin/articles")
                        .param("title", "")
                        .param("summary", "")
                        .param("content", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/articles/form"))
                .andExpect(model().attributeHasFieldErrors("articleForm", "title", "content"));

        verify(articleService, never()).createArticle(any());
    }

    @Test
    void createValidArticleRedirects() throws Exception {
        mockMvc.perform(post("/admin/articles")
                        .param("title", "Title")
                        .param("summary", "Summary")
                        .param("slug", "")
                        .param("content", "Content")
                        .param("published", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/articles"))
                .andExpect(flash().attribute("message", "記事を作成しました"));

        ArgumentCaptor<ArticleForm> captor = ArgumentCaptor.forClass(ArticleForm.class);
        verify(articleService).createArticle(captor.capture());
        ArticleForm saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("Title");
        assertThat(saved.isPublished()).isTrue();
    }

    @Test
    void editPopulatesFormWhenArticleExists() throws Exception {
        Article article = new Article();
        article.setId(10L);
        article.setTitle("Title");
        article.setSummary("Summary");
        article.setSlug("slug");
        article.setContent("Content");
        article.setPublished(true);
        when(articleService.findById(10L)).thenReturn(Optional.of(article));

        mockMvc.perform(get("/admin/articles/10/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/articles/form"))
                .andExpect(model().attribute("articleForm", allOf(
                        hasProperty("id", is(10L)),
                        hasProperty("title", is("Title")),
                        hasProperty("summary", is("Summary")),
                        hasProperty("slug", is("slug")),
                        hasProperty("content", is("Content")),
                        hasProperty("published", is(true))
                )))
                .andExpect(model().attribute("formTitle", "記事編集"));
    }

    @Test
    void editRedirectsWhenArticleMissing() throws Exception {
        when(articleService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/articles/99/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/articles"))
                .andExpect(flash().attribute("error", "記事が見つかりません"));
    }

    @Test
    void updateWithValidationErrorsReturnsForm() throws Exception {
        mockMvc.perform(post("/admin/articles/5")
                        .param("title", "")
                        .param("content", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/articles/form"))
                .andExpect(model().attributeHasFieldErrors("articleForm", "title", "content"));

        verify(articleService, never()).updateArticle(eq(5L), any());
    }

    @Test
    void updateSuccessfulRedirectsWithMessage() throws Exception {
        when(articleService.updateArticle(eq(5L), any(ArticleForm.class))).thenReturn(new Article());

        mockMvc.perform(post("/admin/articles/5")
                        .param("title", "Updated")
                        .param("summary", "Summary")
                        .param("slug", "slug")
                        .param("content", "Content")
                        .param("published", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/articles"))
                .andExpect(flash().attribute("message", "記事を更新しました"));

        verify(articleService).updateArticle(eq(5L), any(ArticleForm.class));
    }

    @Test
    void updateHandlesServiceException() throws Exception {
        when(articleService.updateArticle(eq(7L), any(ArticleForm.class)))
                .thenThrow(new IllegalArgumentException("エラー"));

        mockMvc.perform(post("/admin/articles/7")
                        .param("title", "Updated")
                        .param("summary", "Summary")
                        .param("slug", "slug")
                        .param("content", "Content")
                        .param("published", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/articles"))
                .andExpect(flash().attribute("error", "エラー"));
    }

    @Test
    void deleteSuccessfulRedirectsWithMessage() throws Exception {
        mockMvc.perform(post("/admin/articles/3/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/articles"))
                .andExpect(flash().attribute("message", "記事を削除しました"));

        verify(articleService).deleteArticle(3L);
    }

    @Test
    void deleteHandlesFailure() throws Exception {
        doThrow(new RuntimeException("fail")).when(articleService).deleteArticle(4L);

        mockMvc.perform(post("/admin/articles/4/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/articles"))
                .andExpect(flash().attribute("error", "削除に失敗しました"));
    }
}
