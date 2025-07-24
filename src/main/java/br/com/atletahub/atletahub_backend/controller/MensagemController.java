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

@RestController // Marca a classe como um controlador REST
@RequestMapping("/mensagens") // Define o caminho base para todos os endpoints deste controlador
public class MensagemController {

    @Autowired
    private MensagemService mensagemService; // Injeta o serviço de mensagens

    @PostMapping // Mapeia requisições POST para /mensagens
    public ResponseEntity<DetalhesMensagemDTO> enviarMensagem(
            @RequestBody @Valid DadosEnvioMensagemDTO dados, // Recebe os dados da mensagem e valida
            UriComponentsBuilder uriBuilder // Para construir a URI de retorno no 201 Created
    ) {
        DetalhesMensagemDTO mensagemSalva = mensagemService.enviarMensagem(dados);

        // Constrói a URI para o recurso criado (boa prática para 201 Created)
        URI uri = uriBuilder.path("/mensagens/{id}").buildAndExpand(mensagemSalva.id()).toUri();

        return ResponseEntity.created(uri).body(mensagemSalva);
    }

    @GetMapping("/match/{idMatch}") // Mapeia requisições GET para /mensagens/match/{idMatch}
    public ResponseEntity<List<DetalhesMensagemDTO>> listarMensagensDoMatch(
            @PathVariable Long idMatch // Pega o ID do match da URL
    ) {
        List<DetalhesMensagemDTO> mensagens = mensagemService.listarMensagensDoMatch(idMatch);
        return ResponseEntity.ok(mensagens);
    }
}
