package br.com.atletahub.atletahub_backend.service;

import br.com.atletahub.atletahub_backend.dto.mensagem.DadosEnvioMensagemDTO;
import br.com.atletahub.atletahub_backend.dto.mensagem.DetalhesMensagemDTO;
import br.com.atletahub.atletahub_backend.model.Mensagem;
import br.com.atletahub.atletahub_backend.model.Match;
import br.com.atletahub.atletahub_backend.model.Usuario;
import br.com.atletahub.atletahub_backend.repository.MensagemRepository;
import br.com.atletahub.atletahub_backend.repository.MatchRepository; // Certifique-se de ter este repositório
import br.com.atletahub.atletahub_backend.repository.UsuarioRepository; // Certifique-se de ter este repositório
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
        // 1. Validar Match
        Match match = matchRepository.findById(dados.idMatch())
                .orElseThrow(() -> new IllegalArgumentException("Match não encontrado com o ID: " + dados.idMatch()));

        // 2. Validar Remetente
        Usuario remetente = usuarioRepository.findById(dados.idRemetente())
                .orElseThrow(() -> new IllegalArgumentException("Remetente (Usuário) não encontrado com o ID: " + dados.idRemetente()));

        // 3. Verificar se o remetente é participante do match
        if (!match.getUsuarioA().getIdUsuario().equals(remetente.getIdUsuario()) && !match.getUsuarioB().getIdUsuario().equals(remetente.getIdUsuario())) {
            throw new IllegalArgumentException("O remetente não é um participante deste match.");
        }

        // 4. Criar e salvar a mensagem
        Mensagem novaMensagem = new Mensagem(match, remetente, dados.texto());
        Mensagem mensagemSalva = mensagemRepository.save(novaMensagem);

        return new DetalhesMensagemDTO(mensagemSalva);
    }

    public List<DetalhesMensagemDTO> listarMensagensDoMatch(Long idMatch) {
        // 1. Validar Match (opcional, dependendo da regra de negócio, mas é boa prática)
        if (!matchRepository.existsById(idMatch)) {
            throw new IllegalArgumentException("Match não encontrado com o ID: " + idMatch);
        }

        // 2. Buscar mensagens do match e converter para DTO
        List<Mensagem> mensagens = mensagemRepository.findByMatchIdOrderByDataEnvioAsc(idMatch);
        return mensagens.stream()
                .map(DetalhesMensagemDTO::new)
                .collect(Collectors.toList());
    }
}
