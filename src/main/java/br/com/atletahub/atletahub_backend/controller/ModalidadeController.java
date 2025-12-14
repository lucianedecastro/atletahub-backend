package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.enums.Modalidade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/modalidades")
public class ModalidadeController {

    @GetMapping
    public Modalidade[] getModalidades() {
        return Modalidade.values();
    }
}
