package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.dto.mensagem.DadosCriacaoMensagemTraducaoDTO;
import br.com.atletahub.atletahub_backend.dto.mensagem.DetalhesMensagemTraducaoDTO;
import br.com.atletahub.atletahub_backend.service.MensagemTraducaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/mensagens/traducoes")
public class MensagemTraducaoController {

    @Autowired
    private MensagemTraducaoService mensagemTraducaoService;

    @PostMapping
    public ResponseEntity<DetalhesMensagemTraducaoDTO> criarTraducao(
            @RequestBody @Valid DadosCriacaoMensagemTraducaoDTO dados,
            UriComponentsBuilder uriBuilder
    ) {

        DetalhesMensagemTraducaoDTO traducao =
                mensagemTraducaoService.traduzirMensagem(dados);

        URI uri = uriBuilder
                .path("/mensagens/traducoes/{id}")
                .buildAndExpand(traducao.id())
                .toUri();

        return ResponseEntity.created(uri).body(traducao);
    }
}
