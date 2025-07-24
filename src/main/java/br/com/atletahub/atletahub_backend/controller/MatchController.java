package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.dto.match.DadosDetalhamentoMatch;
import br.com.atletahub.atletahub_backend.model.Match;
import br.com.atletahub.atletahub_backend.model.Usuario; // Para AuthenticationPrincipal
import br.com.atletahub.atletahub_backend.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Importar AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/matches") // Endpoint base para matches
public class MatchController {

    @Autowired
    private MatchService matchService;

    // Endpoint para listar todos os matches do usuário logado
    @GetMapping
    public ResponseEntity<List<DadosDetalhamentoMatch>> listarMeusMatches(
            @AuthenticationPrincipal Usuario usuarioLogado) {


        List<Match> matches = matchService.listarMatchesDoUsuario(usuarioLogado.getIdUsuario());

        // Converte a lista de entidades para uma lista de DTOs de detalhamento
        List<DadosDetalhamentoMatch> dadosDetalhamento = matches.stream()
                .map(DadosDetalhamentoMatch::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dadosDetalhamento);
    }

}