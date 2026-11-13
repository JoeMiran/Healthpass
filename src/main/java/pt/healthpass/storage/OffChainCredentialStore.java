package pt.healthpass.storage;

import java.util.List;
import pt.healthpass.credential.Credential;

/**
 * Armazenamento local (off-chain) de credenciais.
 *
 * <p>Guarda a <b>credencial completa</b> e os respetivos dados sinteticos
 * (vacina, data de administracao, etc.). Isto e essencial para a separacao
 * on-chain / off-chain:</p>
 * <ul>
 *   <li><b>On-chain:</b> apenas o id tecnico, DIDs, hash, estado e timestamp.</li>
 *   <li><b>Off-chain (aqui):</b> a credencial completa com os dados de
 *       apresentacao.</li>
 * </ul>
 *
 * <p><b>AVISO:</b> nunca colocar dados clinicos completos, nomes ou numeros de
 * utente na blockchain. O hash nao e, por si so, anonimizacao de dados
 * pessoais.</p>
 *
 * <p><b>Origem:</b> novo.</p>
 *
 * @author HealthPass
 */
public class OffChainCredentialStore {

    /**
     * Guarda uma credencial localmente.
     *
     * @param credencial credencial a guardar
     */
    // TODO (Etapa 6): persistir a credencial (ex.: ficheiro JSON por credencial).
    public void guardar(Credential credencial) {
        throw new UnsupportedOperationException("A implementar na Etapa 6");
    }

    /**
     * Carrega uma credencial pelo seu identificador.
     *
     * @param credentialId identificador da credencial
     * @return a credencial, ou {@code null} se nao existir
     */
    // TODO (Etapa 6): ler a credencial do armazenamento local.
    public Credential carregar(String credentialId) {
        throw new UnsupportedOperationException("A implementar na Etapa 6");
    }

    /**
     * Lista as credenciais guardadas localmente.
     *
     * @return a lista de credenciais
     */
    // TODO (Etapa 6): listar as credenciais guardadas.
    public List<Credential> listar() {
        throw new UnsupportedOperationException("A implementar na Etapa 6");
    }
}
