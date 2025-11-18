package com.example.nordicelectronics.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/index.html";
    }

    @GetMapping("/swagger")
    public String admin() {
        return "redirect:/swagger-ui/index.html";
    }
}