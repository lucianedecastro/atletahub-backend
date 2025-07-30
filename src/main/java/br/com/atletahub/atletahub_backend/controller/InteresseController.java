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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/interesses")
public class InteresseController {

    @Autowired
    private InteresseService interesseService;


    @PostMapping
    @Transactional
    public ResponseEntity<DadosDetalhamentoInteresse> registrarInteresse(
            @RequestBody @Valid DadosCadastroInteresse dados,
            @AuthenticationPrincipal Usuario usuarioLogado,
            UriComponentsBuilder uriBuilder) {



        Interesse interesse = interesseService.registrarInteresse(usuarioLogado.getIdUsuario(), dados);


        URI uri = uriBuilder.path("/interesses/{id}").buildAndExpand(interesse.getId()).toUri();


        return ResponseEntity.created(uri).body(new DadosDetalhamentoInteresse(interesse));
    }


    @GetMapping("/enviados")
    public ResponseEntity<List<DadosDetalhamentoInteresse>> listarInteressesEnviados(
            @AuthenticationPrincipal Usuario usuarioLogado) {


        List<Interesse> interesses = interesseService.listarInteressesEnviados(usuarioLogado.getIdUsuario());


        List<DadosDetalhamentoInteresse> dadosDetalhamento = interesses.stream()
                .map(DadosDetalhamentoInteresse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dadosDetalhamento);
    }


    @GetMapping("/recebidos")
    public ResponseEntity<List<DadosDetalhamentoInteresse>> listarInteressesRecebidos(
            @AuthenticationPrincipal Usuario usuarioLogado) {


        List<Interesse> interesses = interesseService.listarInteressesRecebidos(usuarioLogado.getIdUsuario());


        List<DadosDetalhamentoInteresse> dadosDetalhamento = interesses.stream()
                .map(DadosDetalhamentoInteresse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dadosDetalhamento);
    }
}