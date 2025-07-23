package com.example.landofchokolate.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/about")
public class AboutClientControler {

    @GetMapping
    public String about() {
        return "client/about/index";
    }
}
