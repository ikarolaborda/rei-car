package com.reicar.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ro")
public class ServiceOrderController {

    @GetMapping("/register")
    public String  getViewForm(){
        return "screens/service-order-form";
    }
}
