package ru.practicum.ewm.main;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//новая версия
@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Explore With Me is running on localhost:8080 V2";
    }
}
