package pt.healthpass.identity;

import java.security.KeyPair;

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
    // TODO (Etapa 6): implementar com KeyPairGenerator ("RSA").
    public KeyPair gerarParDeChaves(int bits) {
        throw new UnsupportedOperationException("A implementar na Etapa 6");
    }

    /**
     * Gera um par de chaves com o tamanho por omissao definido na configuracao.
     *
     * @return o par de chaves gerado
     */
    // TODO (Etapa 6): usar HealthPassConfig para o tamanho por omissao.
    public KeyPair gerarParDeChaves() {
        throw new UnsupportedOperationException("A implementar na Etapa 6");
    }
}
