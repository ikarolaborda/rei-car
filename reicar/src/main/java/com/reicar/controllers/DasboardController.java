package com.reicar.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class DasboardController {

    @GetMapping("/dashboard")
    public String getViewDash(){
        return "screens/dashboard";
    }
}
