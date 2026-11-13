package pt.healthpass.consensus;

import pt.healthpass.ledger.Block;
import pt.healthpass.ledger.Blockchain;

/**
 * Motor de consenso (Proof of Authority simplificado).
 *
 * <p><b>Porque PoA e nao PoW?</b><br>
 * A TemplarCoin usava Proof of Work (mineracao). O HealthPass e uma rede
 * <b>permissionada</b>: os validadores sao conhecidos e autorizados, pelo que
 * faz mais sentido um esquema tipo Proof of Authority, sem gasto energetico de
 * mineracao.</p>
 *
 * <p><b>Regras do consenso (a implementar na Etapa 7):</b></p>
 * <ol>
 *   <li>Um validador autorizado propoe um bloco ({@link BlockProposal}).</li>
 *   <li>Os restantes validadores recebem a proposta.</li>
 *   <li>Cada validador verifica estrutura, hash anterior, Merkle Root,
 *       transacoes e assinaturas.</li>
 *   <li>Cada validador autorizado emite um voto assinado ({@link ValidatorVote}).</li>
 *   <li>O bloco e aceite quando obtiver o quorum configurado.</li>
 *   <li>Os nos verificam as aprovacoes antes de integrar o bloco.</li>
 * </ol>
 *
 * <p><b>Quorum:</b> para 3 validadores, maioria estrita = 2 votos em 3.</p>
 *
 * <p><b>Honestidade sobre garantias:</b> este e um mecanismo <b>simplificado</b>
 * para fins academicos. Nao oferece tolerancia bizantina nem garantias de
 * producao. Os casos a documentar sao: escolha do proponente, recolha de votos,
 * calculo do quorum, rejeicao de blocos invalidos, indisponibilidade de um
 * validador e tratamento de blocos concorrentes.</p>
 *
 * <p><b>Origem:</b> novo (substitui o PoW de {@code core.MinerPOW} da
 * TemplarCoin).</p>
 *
 * @author HealthPass
 */
public class ConsensusEngine {

    private final ValidatorRegistry registry;
    private final int quorum;

    /**
     * @param registry registo de validadores autorizados
     * @param quorum numero de votos necessarios para aprovar um bloco
     */
    public ConsensusEngine(ValidatorRegistry registry, int quorum) {
        this.registry = registry;
        this.quorum = quorum;
    }

    /**
     * Cria uma proposta de bloco a partir das transacoes pendentes.
     *
     * @param blockchain cadeia atual (para obter o bloco anterior)
     * @return a proposta de bloco
     */
    // TODO (Etapa 7): construir Block com pendentes + Merkle Root + previousHash.
    public BlockProposal propor(Blockchain blockchain) {
        throw new UnsupportedOperationException("A implementar na Etapa 7");
    }

    /**
     * Verifica um bloco proposto (estrutura, encadeamento, Merkle Root,
     * transacoes e assinaturas).
     *
     * @param bloco bloco a verificar
     * @param blockchain cadeia atual
     * @return {@code true} se o bloco for valido
     */
    // TODO (Etapa 7): validar todos os aspetos do bloco.
    public boolean validarBloco(Block bloco, Blockchain blockchain) {
        throw new UnsupportedOperationException("A implementar na Etapa 7");
    }

    /**
     * Avalia um certificado de consenso: verifica as assinaturas e se o numero
     * de aprovacoes atinge o quorum.
     *
     * @param certificado certificado a avaliar
     * @return {@code true} se o certificado atingir o quorum
     */
    // TODO (Etapa 7): validar votos e comparar com o quorum.
    public boolean temQuorum(ConsensusCertificate certificado) {
        throw new UnsupportedOperationException("A implementar na Etapa 7");
    }

    /**
     * @return o quorum configurado.
     */
    public int getQuorum() {
        return quorum;
    }
}
