package pt.healthpass.credential;

import pt.healthpass.identity.IdentityService;
import pt.healthpass.ledger.Blockchain;

/**
 * Verificador de credenciais.
 *
 * <p><b>Principio importante:</b> a verificacao tem varias partes
 * independentes. Nao basta uma delas passar. Apresentamos cada resultado
 * separadamente, para se perceber exatamente porque uma credencial e aceite ou
 * rejeitada:</p>
 * <ul>
 *   <li>Assinatura valida ou invalida.</li>
 *   <li>Hash correspondente ou divergente (integridade).</li>
 *   <li>Emissor autorizado ou desconhecido.</li>
 *   <li>Credencial ativa ou revogada (estado on-chain).</li>
 *   <li>Dentro ou fora do prazo de validade.</li>
 * </ul>
 *
 * <p>A credencial so e considerada <b>tecnicamente valida</b> quando todas as
 * verificacoes obrigatorias passam.</p>
 *
 * <p><b>AVISO:</b> isto e validacao <b>tecnica</b> (criptografica), nao
 * clinica. O sistema nao avalia se a vacinacao e clinicamente adequada.</p>
 *
 * <p><b>Origem:</b> novo.</p>
 *
 * @author HealthPass
 */
public class CredentialVerifier {

    private final Blockchain blockchain;
    private final IdentityService identityService;

    /**
     * @param blockchain cadeia para consultar o estado da credencial
     * @param identityService servico para confirmar o emissor
     */
    public CredentialVerifier(Blockchain blockchain, IdentityService identityService) {
        this.blockchain = blockchain;
        this.identityService = identityService;
    }

    /**
     * Resultado detalhado de uma verificacao.
     *
     * @param assinaturaValida assinatura criptografica valida
     * @param hashCorresponde hash da credencial coincide com o registado on-chain
     * @param emissorAutorizado o emissor e um emissor autorizado
     * @param ativa a credencial nao esta revogada
     * @param dentroValidade a credencial esta dentro do prazo
     */
    public record Resultado(boolean assinaturaValida, boolean hashCorresponde,
                            boolean emissorAutorizado, boolean ativa,
                            boolean dentroValidade) {

        /**
         * @return {@code true} se todas as verificacoes obrigatorias passaram.
         */
        public boolean tecnicamenteValida() {
            return assinaturaValida && hashCorresponde && emissorAutorizado
                    && ativa && dentroValidade;
        }
    }

    /**
     * Executa todas as verificacoes e devolve o resultado detalhado.
     *
     * @param credencial credencial a verificar
     * @return o resultado com cada verificacao separada
     */
    // TODO (Etapa 6): verificar assinatura, hash, emissor, estado e validade.
    public Resultado verificar(Credential credencial) {
        throw new UnsupportedOperationException("A implementar na Etapa 6");
    }
}
