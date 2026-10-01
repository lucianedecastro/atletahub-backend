package br.com.atletahub.atletahub_backend.controller;

import br.com.atletahub.atletahub_backend.dto.usuario.DadosDataNascimento;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/conta")
public class ContaController {

    private final UsuarioService usuarioService;

    public ContaController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Para contas antigas: informa a data de nascimento (só maiores de 18; só uma vez).
     * Exige login (qualquer rota não listada no SecurityConfig já é "authenticated").
     */
    @PutMapping("/nascimento")
    public ResponseEntity<Map<String, String>> informarNascimento(
            @RequestBody @Valid DadosDataNascimento dados,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        usuarioService.informarDataNascimento(usuarioLogado.getIdUsuario(), dados.dataNascimento());
        return ResponseEntity.ok(Map.of("message", "Data de nascimento registrada."));
    }
}
