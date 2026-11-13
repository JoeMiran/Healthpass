package pt.healthpass.identity;

import java.security.PublicKey;
import pt.healthpass.ledger.Blockchain;

/**
 * Servico de identidades.
 *
 * <p>Responsavel por criar, registar e consultar identidades na blockchain.
 * O registo de uma identidade gera uma transacao propria do tipo
 * {@code IDENTITY_REGISTERED} (ver {@code ledger.TransactionType}).</p>
 *
 * <p>Operacoes previstas:</p>
 * <ul>
 *   <li>Criar identidade (DID + papel + chave publica).</li>
 *   <li>Registar a identidade na blockchain.</li>
 *   <li>Consultar a identidade pelo DID.</li>
 *   <li>Verificar se um DID pertence a um emissor autorizado.</li>
 * </ul>
 *
 * <p><b>Origem:</b> evolucao de {@code digitalID.core.DigitalIDServices} da
 * TemplarCoin (que procurava a chave publica pelo nome).</p>
 *
 * @author HealthPass
 */
public class IdentityService {

    /** Referencia a blockchain onde as identidades ficam registadas. */
    private final Blockchain blockchain;

    /**
     * @param blockchain blockchain usada para registar/consultar identidades
     */
    public IdentityService(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    /**
     * Regista uma identidade na blockchain, criando uma transacao
     * {@code IDENTITY_REGISTERED}.
     *
     * @param identity identidade a registar
     */
    // TODO (Etapa 6): criar Transaction de IDENTITY_REGISTERED e submeter na cadeia.
    public void registar(Identity identity) {
        throw new UnsupportedOperationException("A implementar na Etapa 6");
    }

    /**
     * Procura uma identidade registada pelo seu DID.
     *
     * @param did identificador a procurar
     * @return a identidade encontrada, ou {@code null} se nao existir
     */
    // TODO (Etapa 6): percorrer a cadeia e reconstruir a Identity.
    public Identity procurar(DID did) {
        throw new UnsupportedOperationException("A implementar na Etapa 6");
    }

    /**
     * Devolve a chave publica associada a um DID.
     *
     * @param did identificador
     * @return a chave publica, ou {@code null} se nao existir
     */
    // TODO (Etapa 6): reutilizar a ideia de DigitalIDServices.getPublicKey.
    public PublicKey getChavePublica(DID did) {
        throw new UnsupportedOperationException("A implementar na Etapa 6");
    }

    /**
     * Verifica se um DID pertence a um emissor autorizado.
     *
     * @param did identificador a verificar
     * @return {@code true} se for um emissor autorizado
     */
    // TODO (Etapa 6): confirmar papel ISSUER e autorizacao registada.
    public boolean ehEmissorAutorizado(DID did) {
        throw new UnsupportedOperationException("A implementar na Etapa 6");
    }
}
