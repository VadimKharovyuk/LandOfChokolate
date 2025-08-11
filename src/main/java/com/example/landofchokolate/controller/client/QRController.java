package com.example.landofchokolate.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/qr")
public class QRController {


    @GetMapping
    public String home() {
        return "qr";
    }
}
