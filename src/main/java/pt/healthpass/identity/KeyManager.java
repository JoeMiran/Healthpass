package pt.healthpass.identity;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import pt.healthpass.config.HealthPassConfig;

/**
 * Gestor de pares de chaves criptograficas.
 *
 * <p>Responsavel por gerar pares de chaves (publica + privada) e por as
 * carregar/guardar em ficheiro. A chave privada e protegida por password e
 * <b>nunca</b> deve ser enviada pela rede nem guardada na blockchain.</p>
 *
 * <p><b>Origem:</b> adaptado da geracao de chaves em
 * {@code utils.SecurityUtils} da TemplarCoin (RSA). O carregamento/gravacao
 * em ficheiro sera delegado em {@code storage.KeyStoreService}.</p>
 *
 * @author HealthPass
 */
public class KeyManager {

    /**
     * Gera um novo par de chaves.
     *
     * @param bits tamanho da chave (ex.: 3072)
     * @return o par de chaves gerado
     */
    public KeyPair gerarParDeChaves(int bits) {
        if (bits < 2048) throw new IllegalArgumentException("RSA requer pelo menos 2048 bits");
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(bits, new SecureRandom());
            return generator.generateKeyPair();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao gerar par de chaves RSA", e);
        }
    }

    /**
     * Gera um par de chaves com o tamanho por omissao definido na configuracao.
     *
     * @return o par de chaves gerado
     */
    public KeyPair gerarParDeChaves() {
        return gerarParDeChaves(HealthPassConfig.TAMANHO_CHAVE_RSA);
    }
}
