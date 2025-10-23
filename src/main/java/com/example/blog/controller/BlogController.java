package com.example.blog.controller;

import com.example.blog.model.Article;
import com.example.blog.service.ArticleService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class BlogController {

    private final ArticleService articleService;

    public BlogController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("articles", articleService.findPublishedArticles());
        return "blog/index";
    }

    @GetMapping("/posts/{slug}")
    public String show(@PathVariable String slug, Model model) {
        Article article = articleService.findBySlug(slug)
                .filter(Article::isPublished)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("article", article);
        return "blog/article";
    }
}
