package br.com.atletahub.atletahub_backend.dto.perfil;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

// Os limites de tamanho seguem as colunas do banco (V1), para dar erro 400 claro em vez de 500.
public record DadosAtualizacaoPerfilAtleta(
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,
        @Email(message = "Email inválido")
        @Size(max = 100, message = "O email deve ter no máximo 100 caracteres")
        String email,
        @Min(value = 0, message = "Idade inválida")
        @Max(value = 120, message = "Idade inválida")
        Integer idade,
        @Size(max = 100, message = "A modalidade deve ter no máximo 100 caracteres")
        String modalidade,
        @Size(max = 100, message = "A posição deve ter no máximo 100 caracteres")
        String posicao,
        // Altura em cm (aceita também metros, ex.: 1.75, e converte).
        @DecimalMin(value = "0.50", message = "Altura inválida")
        @DecimalMax(value = "300.00", message = "Altura inválida")
        @Digits(integer = 3, fraction = 2, message = "Altura inválida")
        BigDecimal altura,
        @DecimalMin(value = "1.00", message = "Peso inválido")
        @DecimalMax(value = "500.00", message = "Peso inválido")
        @Digits(integer = 3, fraction = 2, message = "Peso inválido")
        BigDecimal peso,
        @Past(message = "A data de nascimento deve estar no passado")
        LocalDate dataNascimento,
        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
        String telefoneContato,
        @Size(max = 255, message = "As observações devem ter no máximo 255 caracteres")
        String observacoes,
        @Size(max = 255, message = "O link do mídia kit deve ter no máximo 255 caracteres")
        String midiakitUrl,
        @Size(max = 5000, message = "Competições e títulos: texto muito longo")
        String competicoesTitulos,
        @Size(max = 255, message = "Redes sociais deve ter no máximo 255 caracteres")
        String redesSocial,
        @Size(max = 5000, message = "Histórico: texto muito longo")
        String historico
) {}
