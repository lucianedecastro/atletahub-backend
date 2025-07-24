package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.dto.interesse.DadosCadastroInteresse;
import br.com.atletahub.atletahub_backend.dto.interesse.DadosDetalhamentoInteresse;
import br.com.atletahub.atletahub_backend.model.Interesse;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.service.InteresseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors; // Para usar stream().map().collect()

@RestController
@RequestMapping("/interesses") // Endpoint base para interesses
public class InteresseController {

    @Autowired
    private InteresseService interesseService;

    // Endpoint para registrar um novo interesse (curtir/super curtir)
    @PostMapping
    @Transactional // Garante que a operação seja transacional
    public ResponseEntity<DadosDetalhamentoInteresse> registrarInteresse(
            @RequestBody @Valid DadosCadastroInteresse dados,
            @AuthenticationPrincipal Usuario usuarioLogado, // Obtém o usuário logado do token
            UriComponentsBuilder uriBuilder) { // Para construir a URI de retorno

        // Chama o serviço para registrar o interesse, passando o ID do usuário logado como origem

        Interesse interesse = interesseService.registrarInteresse(usuarioLogado.getIdUsuario(), dados);

        // Constrói a URI para o novo recurso criado (boa prática RESTful)
        URI uri = uriBuilder.path("/interesses/{id}").buildAndExpand(interesse.getId()).toUri();

        // Retorna a resposta com status 201 Created e o detalhamento do interesse
        return ResponseEntity.created(uri).body(new DadosDetalhamentoInteresse(interesse));
    }

    // Endpoint para listar todos os interesses que o usuário logado enviou
    @GetMapping("/enviados")
    public ResponseEntity<List<DadosDetalhamentoInteresse>> listarInteressesEnviados(
            @AuthenticationPrincipal Usuario usuarioLogado) {


        List<Interesse> interesses = interesseService.listarInteressesEnviados(usuarioLogado.getIdUsuario());

        // Converte a lista de entidades para uma lista de DTOs de detalhamento
        List<DadosDetalhamentoInteresse> dadosDetalhamento = interesses.stream()
                .map(DadosDetalhamentoInteresse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dadosDetalhamento);
    }

    // Endpoint para listar todos os interesses que o usuário logado RECEBEU
    @GetMapping("/recebidos")
    public ResponseEntity<List<DadosDetalhamentoInteresse>> listarInteressesRecebidos(
            @AuthenticationPrincipal Usuario usuarioLogado) {


        List<Interesse> interesses = interesseService.listarInteressesRecebidos(usuarioLogado.getIdUsuario());

        // Converte a lista de entidades para uma lista de DTOs de detalhamento
        List<DadosDetalhamentoInteresse> dadosDetalhamento = interesses.stream()
                .map(DadosDetalhamentoInteresse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dadosDetalhamento);
    }
}