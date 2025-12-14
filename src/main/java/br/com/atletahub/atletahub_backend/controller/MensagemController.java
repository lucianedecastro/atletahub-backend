package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.dto.mensagem.DadosEnvioMensagemDTO;
import br.com.atletahub.atletahub_backend.dto.mensagem.DetalhesMensagemDTO;
import br.com.atletahub.atletahub_backend.service.MensagemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/mensagens")
public class MensagemController {

    @Autowired
    private MensagemService mensagemService;

    @PostMapping
    public ResponseEntity<DetalhesMensagemDTO> enviarMensagem(
            @RequestBody @Valid DadosEnvioMensagemDTO dados,
            UriComponentsBuilder uriBuilder
    ) {
        DetalhesMensagemDTO mensagemSalva = mensagemService.enviarMensagem(dados);


        URI uri = uriBuilder.path("/mensagens/{id}").buildAndExpand(mensagemSalva.id()).toUri();

        return ResponseEntity.created(uri).body(mensagemSalva);
    }

    @GetMapping("/match/{idMatch}")
    public ResponseEntity<List<DetalhesMensagemDTO>> listarMensagensDoMatch(
            @PathVariable Long idMatch
    ) {
        List<DetalhesMensagemDTO> mensagens = mensagemService.listarMensagensDoMatch(idMatch);
        return ResponseEntity.ok(mensagens);
    }
}
