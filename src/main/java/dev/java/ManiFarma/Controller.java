package dev.java.ManiFarma;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class Controller {


    @GetMapping("Introdução")
    public String introducao(){
        return "salve tropa";
    }


}
