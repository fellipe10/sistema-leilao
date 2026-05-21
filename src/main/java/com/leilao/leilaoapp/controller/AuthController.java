package com.leilao.leilaoapp.controller;

import com.leilao.leilaoapp.dto.RegisterDTO;
import com.leilao.leilaoapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/";
        }
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String registerForm(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/";
        }
        model.addAttribute("registerDTO", new RegisterDTO());
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerDTO") RegisterDTO dto,
                           BindingResult bindingResult,
                           Model model) {
        
        // Erros de validação dos campos
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        
        // Senha != confirmPassword
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            model.addAttribute("erroGlobal", "As senhas não coincidem.");
            return "auth/register";
        }
        
        // Email já existe
        try {
            userService.registerUser(dto);
        } catch (IllegalArgumentException e) {
            model.addAttribute("erroGlobal", e.getMessage());
            return "auth/register";
        }
        
        return "redirect:/login?registered=true";
    }
}