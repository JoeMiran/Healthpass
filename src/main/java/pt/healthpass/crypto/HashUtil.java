package pt.healthpass.crypto;

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
 * O algoritmo usado e o <b>SHA3-256</b>, o mesmo do projeto original.</p>
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
     * Calcula o hash SHA3-256 de um texto e devolve-o em Base64.
     *
     * <p>Este metodo sera implementado na Etapa 5 (Blockchain). Devera usar
     * {@code MessageDigest.getInstance("SHA3-256")} e codificar o resultado
     * com {@link java.util.Base64}.</p>
     *
     * @param texto texto de entrada (nao pode ser {@code null})
     * @return o hash em Base64
     */
    // TODO (Etapa 5): implementar com MessageDigest SHA3-256 + Base64.
    public static String hash(String texto) {
        throw new UnsupportedOperationException("A implementar na Etapa 5");
    }

    /**
     * Calcula o hash de um conjunto de bytes (util para assinar/conferir dados
     * binarios, como chaves publicas).
     *
     * @param dados bytes de entrada
     * @return o hash em Base64
     */
    // TODO (Etapa 5): variante de hash(byte[]) para dados binarios.
    public static String hash(byte[] dados) {
        throw new UnsupportedOperationException("A implementar na Etapa 5");
    }
}
