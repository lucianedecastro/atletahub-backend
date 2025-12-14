package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.dto.match.DadosDetalhamentoMatch;
import br.com.atletahub.atletahub_backend.model.Match;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/matches")
public class MatchController {

    @Autowired
    private MatchService matchService;


    @GetMapping
    public ResponseEntity<List<DadosDetalhamentoMatch>> listarMeusMatches(
            @AuthenticationPrincipal Usuario usuarioLogado) {

        List<DadosDetalhamentoMatch> matches = matchService.listarMatchesDoUsuario(usuarioLogado.getIdUsuario());

        return ResponseEntity.ok(matches);
    }

}