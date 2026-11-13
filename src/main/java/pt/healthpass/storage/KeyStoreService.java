package pt.healthpass.storage;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Servico de armazenamento de chaves.
 *
 * <p>Guarda e carrega chaves de ficheiro:</p>
 * <ul>
 *   <li>Chave publica em claro (ficheiro {@code .pub}).</li>
 *   <li>Chave privada <b>cifrada</b> com password (ficheiro {@code .priv}).</li>
 * </ul>
 *
 * <p><b>Regra de seguranca:</b> a chave privada nunca sai do dono, nunca e
 * enviada pela rede e nunca e guardada na blockchain. Os ficheiros {@code .priv}
 * estao no {@code .gitignore}.</p>
 *
 * <p><b>Origem:</b> adaptado das rotinas de cifra/IO de chaves de
 * {@code utils.SecurityUtils} da TemplarCoin (AES/GCM + PBKDF2-HMAC-SHA256).</p>
 *
 * @author HealthPass
 */
public class KeyStoreService {

    /**
     * Guarda uma chave publica num ficheiro {@code .pub}.
     *
     * @param chave chave publica
     * @param nomeUtilizador nome base do ficheiro
     */
    // TODO (Etapa 6): escrever chave publica (X.509) no ficheiro.
    public void guardarChavePublica(PublicKey chave, String nomeUtilizador) {
        throw new UnsupportedOperationException("A implementar na Etapa 6");
    }

    /**
     * Guarda uma chave privada num ficheiro {@code .priv}, cifrada com password.
     *
     * @param chave chave privada
     * @param nomeUtilizador nome base do ficheiro
     * @param password password de protecao
     */
    // TODO (Etapa 6): cifrar (AES/GCM + PBKDF2) e escrever no ficheiro.
    public void guardarChavePrivada(PrivateKey chave, String nomeUtilizador, String password) {
        throw new UnsupportedOperationException("A implementar na Etapa 6");
    }

    /**
     * Carrega uma chave publica de um ficheiro {@code .pub}.
     *
     * @param nomeUtilizador nome base do ficheiro
     * @return a chave publica
     */
    // TODO (Etapa 6): ler ficheiro e reconstruir a chave (X.509).
    public PublicKey carregarChavePublica(String nomeUtilizador) {
        throw new UnsupportedOperationException("A implementar na Etapa 6");
    }

    /**
     * Carrega e decifra uma chave privada de um ficheiro {@code .priv}.
     *
     * @param nomeUtilizador nome base do ficheiro
     * @param password password de protecao
     * @return a chave privada
     */
    // TODO (Etapa 6): ler, decifrar (AES/GCM) e reconstruir (PKCS#8).
    public PrivateKey carregarChavePrivada(String nomeUtilizador, String password) {
        throw new UnsupportedOperationException("A implementar na Etapa 6");
    }
}
