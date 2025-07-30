package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.dto.interesse.DadosCadastroInteresse;
import br.com.atletahub.atletahub_backend.enums.TipoInteresse;
import br.com.atletahub.atletahub_backend.model.Interesse;
import br.com.atletahub.atletahub_backend.model.Match;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.InteresseRepository;
import br.com.atletahub.atletahub_backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InteresseService {

    @Autowired
    private InteresseRepository interesseRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MatchService matchService;

    @Transactional
    public Interesse registrarInteresse(Long idOrigem, DadosCadastroInteresse dados) {
        Usuario origem = usuarioRepository.findById(idOrigem)
                .orElseThrow(() -> new RuntimeException("Usuário de origem não encontrado."));

        Usuario destino = usuarioRepository.findById(dados.idDestino())
                .orElseThrow(() -> new RuntimeException("Usuário de destino não encontrado."));


        if (origem.getIdUsuario().equals(destino.getIdUsuario())) {
            throw new IllegalArgumentException("Não é permitido curtir a si mesmo.");
        }


        if (interesseRepository.findByOrigem_IdUsuarioAndDestino_IdUsuario(origem.getIdUsuario(), destino.getIdUsuario()).isPresent()) {
            throw new IllegalArgumentException("Você já expressou interesse neste usuário.");
        }


        Interesse novoInteresse = new Interesse(origem, destino, dados.tipoInteresse());
        Interesse interesseSalvo = interesseRepository.save(novoInteresse);


        Match matchGerado = matchService.verificarEGerarMatch(origem, destino, dados.tipoInteresse());

        if (matchGerado != null) {
            System.out.println("Match gerado com sucesso! ID: " + matchGerado.getId());
        }

        return interesseSalvo;
    }



    public Interesse buscarInteressePorId(Long idInteresse) {
        return interesseRepository.findById(idInteresse)
                .orElseThrow(() -> new RuntimeException("Interesse não encontrado."));
    }


    public List<Interesse> listarInteressesEnviados(Long idOrigem) {
        return interesseRepository.findByOrigem_IdUsuario(idOrigem);
    }


    public List<Interesse> listarInteressesRecebidos(Long idDestino) {
        return interesseRepository.findByDestino_IdUsuario(idDestino);
    }
}