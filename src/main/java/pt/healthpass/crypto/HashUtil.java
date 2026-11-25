package pt.healthpass.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Utilitario de calculo de hash.
 *
 * <p><b>O que e um hash e para que serve?</b><br>
 * Um hash criptografico transforma um texto de qualquer tamanho numa "impressao
 * digital" de tamanho fixo. As duas propriedades que nos interessam sao:</p>
 * <ul>
 *   <li><b>Determinismo:</b> o mesmo texto produz sempre o mesmo hash.</li>
 *   <li><b>Irreversibilidade:</b> nao e viavel reconstruir o texto a partir do hash.</li>
 * </ul>
 *
 * <p>No HealthPass o hash e usado para: encadear blocos (cada bloco guarda o
 * hash do anterior), calcular a Merkle Root das transacoes e criar a impressao
 * digital de uma credencial (on-chain) sem revelar os dados completos.</p>
 *
 * <p><b>Origem:</b> adaptado de {@code utils.SecurityUtils.getHash} da TemplarCoin.
 * O algoritmo definido para o HealthPass e <b>SHA-256</b>.</p>
 *
 * <p><b>Nota:</b> nao implementar o algoritmo manualmente. A plataforma Java
 * ja fornece {@link java.security.MessageDigest}.</p>
 *
 * @author HealthPass
 */
public final class HashUtil {

    /** Classe utilitaria: nao deve ser instanciada. */
    private HashUtil() {
        // Construtor privado para impedir a criacao de objetos desta classe.
    }

    /**
     * Calcula SHA-256 de texto UTF-8 e devolve 64 caracteres hexadecimais
     * minúsculos.
     *
     * <p>O texto é convertido em bytes UTF-8 antes do cálculo.</p>
     * @param texto texto de entrada (nao pode ser {@code null})
     * @return hash hexadecimal
     */
    public static String hash(String texto) {
        Objects.requireNonNull(texto, "texto nao pode ser null");
        return hash(texto.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * Calcula o hash de um conjunto de bytes (util para assinar/conferir dados
     * binarios, como chaves publicas).
     *
     * @param dados bytes de entrada
     * @return hash hexadecimal
     */
    public static String hash(byte[] dados) {
        Objects.requireNonNull(dados, "dados nao podem ser null");
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(dados));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 nao esta disponivel nesta JVM", e);
        }
    }
}
