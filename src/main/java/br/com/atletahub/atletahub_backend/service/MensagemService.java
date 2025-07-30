package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.dto.mensagem.DadosEnvioMensagemDTO;
import br.com.atletahub.atletahub_backend.dto.mensagem.DetalhesMensagemDTO;
import br.com.atletahub.atletahub_backend.model.Mensagem;
import br.com.atletahub.atletahub_backend.model.Match;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.MensagemRepository;
import br.com.atletahub.atletahub_backend.repository.MatchRepository;
import br.com.atletahub.atletahub_backend.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MensagemService {

    @Autowired
    private MensagemRepository mensagemRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public DetalhesMensagemDTO enviarMensagem(DadosEnvioMensagemDTO dados) {

        Match match = matchRepository.findById(dados.idMatch())
                .orElseThrow(() -> new IllegalArgumentException("Match não encontrado com o ID: " + dados.idMatch()));


        Usuario remetente = usuarioRepository.findById(dados.idRemetente())
                .orElseThrow(() -> new IllegalArgumentException("Remetente (Usuário) não encontrado com o ID: " + dados.idRemetente()));


        if (!match.getUsuarioA().getIdUsuario().equals(remetente.getIdUsuario()) && !match.getUsuarioB().getIdUsuario().equals(remetente.getIdUsuario())) {
            throw new IllegalArgumentException("O remetente não é um participante deste match.");
        }


        Mensagem novaMensagem = new Mensagem(match, remetente, dados.texto());
        Mensagem mensagemSalva = mensagemRepository.save(novaMensagem);

        return new DetalhesMensagemDTO(mensagemSalva);
    }

    public List<DetalhesMensagemDTO> listarMensagensDoMatch(Long idMatch) {

        if (!matchRepository.existsById(idMatch)) {
            throw new IllegalArgumentException("Match não encontrado com o ID: " + idMatch);
        }


        List<Mensagem> mensagens = mensagemRepository.findByMatchIdOrderByDataEnvioAsc(idMatch);
        return mensagens.stream()
                .map(DetalhesMensagemDTO::new)
                .collect(Collectors.toList());
    }
}
