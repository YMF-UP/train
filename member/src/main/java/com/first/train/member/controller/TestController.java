package com.first.train.member.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class TestController {
    @GetMapping("/hello")
    public String hello()
    {
        return "Hello World";
    }

}
