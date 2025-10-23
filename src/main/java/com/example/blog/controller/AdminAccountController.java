package com.example.blog.controller;

import com.example.blog.dto.AccountForm;
import com.example.blog.model.UserAccount;
import com.example.blog.service.UserAccountService;
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
public class AdminAccountController {

    private final UserAccountService userAccountService;

    public AdminAccountController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @ModelAttribute("roles")
    public UserAccount.Role[] roles() {
        return UserAccount.Role.values();
    }

    @GetMapping("/admin/accounts")
    public String list(Model model) {
        model.addAttribute("accounts", userAccountService.findAll());
        return "admin/accounts/list";
    }

    @GetMapping("/admin/accounts/new")
    public String newAccount(Model model) {
        AccountForm form = new AccountForm();
        form.setRole(UserAccount.Role.EDITOR.name());
        model.addAttribute("accountForm", form);
        model.addAttribute("formTitle", "新規アカウント");
        return "admin/accounts/form";
    }

    @PostMapping("/admin/accounts")
    public String create(@Valid @ModelAttribute("accountForm") AccountForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formTitle", "新規アカウント");
            return "admin/accounts/form";
        }
        try {
            userAccountService.createAccount(form);
            redirectAttributes.addFlashAttribute("message", "アカウントを作成しました");
            return "redirect:/admin/accounts";
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("error", ex.getMessage());
            model.addAttribute("formTitle", "新規アカウント");
            return "admin/accounts/form";
        }
    }

    @GetMapping("/admin/accounts/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        UserAccount account = userAccountService.findById(id).orElse(null);
        if (account == null) {
            redirectAttributes.addFlashAttribute("error", "アカウントが見つかりません");
            return "redirect:/admin/accounts";
        }
        AccountForm form = new AccountForm();
        form.setId(account.getId());
        form.setUsername(account.getUsername());
        form.setRole(account.getRole().name());
        model.addAttribute("accountForm", form);
        model.addAttribute("formTitle", "アカウント編集");
        return "admin/accounts/form";
    }

    @PostMapping("/admin/accounts/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("accountForm") AccountForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formTitle", "アカウント編集");
            return "admin/accounts/form";
        }
        try {
            userAccountService.updateAccount(id, form);
            redirectAttributes.addFlashAttribute("message", "アカウントを更新しました");
            return "redirect:/admin/accounts";
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("error", ex.getMessage());
            model.addAttribute("formTitle", "アカウント編集");
            return "admin/accounts/form";
        }
    }

    @PostMapping("/admin/accounts/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userAccountService.deleteAccount(id);
            redirectAttributes.addFlashAttribute("message", "アカウントを削除しました");
        } catch (IllegalStateException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/accounts";
    }
}
