package com.example.bookbot.controller;

import com.example.bookbot.dto.response.RootResponseDto;
import com.example.bookbot.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/telegram")
public class BookController {
    private final BookService service;

    @GetMapping
    public RootResponseDto response() {
        return service.sendInfo();
    }
}
