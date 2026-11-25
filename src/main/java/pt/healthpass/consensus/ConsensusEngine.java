package pt.healthpass.consensus;

import java.security.PrivateKey;
import java.util.List;
import java.util.Objects;
import pt.healthpass.ledger.Block;
import pt.healthpass.ledger.Blockchain;
import pt.healthpass.ledger.Transaction;
import pt.healthpass.identity.DID;

/**
 * Motor de consenso (Proof of Authority simplificado).
 *
 * <p><b>Porque PoA e nao PoW?</b><br>
 * A TemplarCoin usava Proof of Work (mineracao). O HealthPass e uma rede
 * <b>permissionada</b>: os validadores sao conhecidos e autorizados, pelo que
 * faz mais sentido um esquema tipo Proof of Authority, sem gasto energetico de
 * mineracao.</p>
 *
 * <p><b>Regras do consenso simplificado:</b></p>
 * <ol>
 *   <li>Um validador autorizado propoe um bloco ({@link BlockProposal}).</li>
 *   <li>Os restantes validadores recebem a proposta.</li>
 *   <li>Cada validador verifica estrutura, hash anterior, Merkle Root e
 *       formato/hash das transações incluídas.</li>
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
 * calculo do quorum, rejeicao de blocos invalidos e limites de disponibilidade
 * e concorrência. A verificação das regras de negócio e assinaturas das
 * transações continua nos serviços de identidade/credenciais.</p>
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
        this.registry = Objects.requireNonNull(registry, "registry obrigatório");
        if (quorum < 1 || quorum > registry.tamanho()) throw new IllegalArgumentException("quorum fora do tamanho do registo");
        this.quorum = quorum;
    }

    /**
     * Cria uma proposta de bloco a partir das transacoes pendentes.
     *
     * @param blockchain cadeia atual (para obter o bloco anterior)
     * @return a proposta de bloco
     */
    public BlockProposal propor(Blockchain blockchain) {
        Objects.requireNonNull(blockchain, "blockchain obrigatória");
        if (!blockchain.validarCadeia()) throw new IllegalStateException("Cadeia local inválida");
        DID proponente = proponenteParaAltura(blockchain.getAltura() + 1);
        Block bloco = blockchain.criarBlocoPendente(proponente.toString());
        return new BlockProposal(bloco, proponente);
    }

    /**
     * Verifica um bloco proposto (estrutura, encadeamento, Merkle Root,
     * transacoes e assinaturas).
     *
     * @param bloco bloco a verificar
     * @param blockchain cadeia atual
     * @return {@code true} se o bloco for valido
     */
    public boolean validarBloco(Block bloco, Blockchain blockchain) {
        if (bloco == null || blockchain == null || registry.tamanho() == 0
                || !blockchain.validarCadeia() || !bloco.isValid()) return false;
        Block ultimo = blockchain.getUltimoBloco();
        if (bloco.getHeight() != blockchain.getAltura() + 1
                || !ultimo.getHash().equals(bloco.getPreviousHash())) return false;
        DID proponente;
        try { proponente = DID.parse(bloco.getValidatorId()); }
        catch (RuntimeException e) { return false; }
        if (!registry.ehValidador(proponente)
                || !proponente.equals(proponenteParaAltura(bloco.getHeight()))) return false;
        List<Transaction> transacoes = bloco.getTransactions();
        if (transacoes == null) return false;
        try {
            for (Transaction tx : transacoes) {
                if (tx == null || tx.getType() == null || tx.getPayload() == null) return false;
                tx.calcularHash();
            }
        } catch (RuntimeException e) { return false; }
        return true;
    }

    /**
     * Avalia um certificado de consenso: verifica as assinaturas e se o numero
     * de aprovacoes atinge o quorum.
     *
     * @param certificado certificado a avaliar
     * @return {@code true} se o certificado atingir o quorum
     */
    public boolean temQuorum(ConsensusCertificate certificado) {
        return certificado != null && certificado.contarAprovacoes(registry) >= quorum;
    }

    /** Cria um voto apenas se este validador aceitar a proposta localmente. */
    public ValidatorVote votar(BlockProposal proposta, Blockchain blockchain,
                               DID validador, PrivateKey chavePrivada) {
        if (proposta == null || !validarProposta(proposta, blockchain)
                || !registry.ehValidador(validador)) {
            throw new IllegalArgumentException("Proposta inválida ou validador não autorizado");
        }
        return ValidatorVote.assinar(validador, proposta.getBloco().getHash(), true, chavePrivada);
    }

    /** Aceita e persiste o bloco somente após validar proposta e certificado. */
    public void aceitar(BlockProposal proposta, ConsensusCertificate certificado, Blockchain blockchain) {
        if (!validarProposta(proposta, blockchain) || certificado == null
                || !proposta.getBloco().getHash().equals(certificado.getHashBloco())
                || !temQuorum(certificado)) {
            throw new IllegalArgumentException("Proposta sem validação ou quorum suficiente");
        }
        blockchain.adicionarBloco(proposta.getBloco());
    }

    public boolean validarProposta(BlockProposal proposta, Blockchain blockchain) {
        return proposta != null && proposta.getProponente().equals(proponenteParaAltura(
                proposta.getBloco().getHeight()))
                && proposta.getProponente().toString().equals(proposta.getBloco().getValidatorId())
                && validarBloco(proposta.getBloco(), blockchain);
    }

    /** Seleção determinística por altura, sobre o registo ordenado por DID. */
    public DID proponenteParaAltura(int altura) {
        if (registry.tamanho() == 0 || altura < 1) throw new IllegalStateException("Altura/registo inválido");
        return registry.getValidadores().get((altura - 1) % registry.tamanho()).getDid();
    }

    /**
     * @return o quorum configurado.
     */
    public int getQuorum() {
        return quorum;
    }

    public ValidatorRegistry getRegistry() { return registry; }
}
