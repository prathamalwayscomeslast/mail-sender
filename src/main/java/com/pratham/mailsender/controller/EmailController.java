package com.pratham.mailsender.controller;

import com.pratham.mailsender.model.EmailConfig;
import com.pratham.mailsender.model.EmailRequest;
import com.pratham.mailsender.service.EmailService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @GetMapping("/")
    public String showConfigPage(Model model) {
        model.addAttribute("config", new EmailConfig());
        return "configure";
    }

    @PostMapping("/configure")
    public String saveConfig(@ModelAttribute EmailConfig config,
                             HttpSession session) {
        session.setAttribute("emailConfig", config);
        return "redirect:/send";
    }

    @GetMapping("/send")
    public String showSendPage(HttpSession session, Model model) {
        EmailConfig config = (EmailConfig) session.getAttribute("emailConfig");
        if (config == null) return "redirect:/";  // not configured yet
        model.addAttribute("emailRequest", new EmailRequest());
        model.addAttribute("fromEmail", config.getFromEmail());
        return "send";
    }

    @PostMapping("/send")
    public String sendEmail(@ModelAttribute EmailRequest emailRequest,
                            HttpSession session, Model model) {
        EmailConfig config = (EmailConfig) session.getAttribute("emailConfig");
        if (config == null) return "redirect:/";

        try {
            emailService.sendEmail(config, emailRequest);
            model.addAttribute("success", "Email sent successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to send email: " + e.getMessage());
        }

        model.addAttribute("fromEmail", config.getFromEmail());
        return "send";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/help")
    public String help() {
        return "help";
    }
}
