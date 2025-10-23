package com.example.blog.controller;

import com.example.blog.dto.ArticleForm;
import com.example.blog.model.Article;
import com.example.blog.service.ArticleService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminArticleController {

    private final ArticleService articleService;

    public AdminArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/admin/articles")
    public String list(Model model) {
        model.addAttribute("articles", articleService.findAllForAdmin());
        return "admin/articles/list";
    }

    @GetMapping("/admin/articles/new")
    public String newArticle(Model model) {
        model.addAttribute("articleForm", new ArticleForm());
        model.addAttribute("formTitle", "新規記事");
        return "admin/articles/form";
    }

    @PostMapping("/admin/articles")
    public String create(@Valid @ModelAttribute("articleForm") ArticleForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formTitle", "新規記事");
            return "admin/articles/form";
        }
        articleService.createArticle(form);
        redirectAttributes.addFlashAttribute("message", "記事を作成しました");
        return "redirect:/admin/articles";
    }

    @GetMapping("/admin/articles/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Article article = articleService.findById(id)
                .orElse(null);
        if (article == null) {
            redirectAttributes.addFlashAttribute("error", "記事が見つかりません");
            return "redirect:/admin/articles";
        }
        model.addAttribute("articleForm", toForm(article));
        model.addAttribute("formTitle", "記事編集");
        return "admin/articles/form";
    }

    @PostMapping("/admin/articles/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("articleForm") ArticleForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formTitle", "記事編集");
            return "admin/articles/form";
        }
        try {
            articleService.updateArticle(id, form);
            redirectAttributes.addFlashAttribute("message", "記事を更新しました");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/articles";
    }

    @PostMapping("/admin/articles/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            articleService.deleteArticle(id);
            redirectAttributes.addFlashAttribute("message", "記事を削除しました");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "削除に失敗しました");
        }
        return "redirect:/admin/articles";
    }

    private ArticleForm toForm(Article article) {
        ArticleForm form = new ArticleForm();
        form.setId(article.getId());
        form.setTitle(article.getTitle());
        form.setSummary(article.getSummary());
        form.setSlug(article.getSlug());
        form.setContent(article.getContent());
        form.setPublished(article.isPublished());
        return form;
    }
}
