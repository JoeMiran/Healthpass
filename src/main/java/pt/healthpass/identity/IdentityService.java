package pt.healthpass.identity;

import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Set;
import pt.healthpass.config.HealthPassConfig;
import pt.healthpass.crypto.CanonicalSerializer;
import pt.healthpass.crypto.SignatureService;
import pt.healthpass.ledger.Transaction;
import pt.healthpass.ledger.TransactionType;
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
    private final Set<String> emissoresConfiaveis;

    /**
     * @param blockchain blockchain usada para registar/consultar identidades
     */
    public IdentityService(Blockchain blockchain) {
        this(blockchain, HealthPassConfig.EMISSORES_DEMO_AUTORIZADOS);
    }

    /** Permite configurar a lista de confiança no arranque/testes. */
    public IdentityService(Blockchain blockchain, Set<String> emissoresConfiaveis) {
        this.blockchain = blockchain;
        this.emissoresConfiaveis = Set.copyOf(Objects.requireNonNull(emissoresConfiaveis));
    }

    /**
     * Regista uma identidade na blockchain, criando uma transacao
     * {@code IDENTITY_REGISTERED}.
     *
     * @param identity identidade a registar
     */
    public void registar(Identity identity) {
        throw new IllegalArgumentException("O registo exige a chave privada do titular; use registar(identity, chavePrivada)");
    }

    /** Regista a identidade com uma prova de posse da chave privada. */
    public void registar(Identity identity, PrivateKey chavePrivada) {
        Objects.requireNonNull(identity, "identity obrigatoria");
        Objects.requireNonNull(chavePrivada, "chave privada obrigatoria");
        DID did = identity.getDid();
        if (procurar(did) != null) throw new IllegalArgumentException("DID ja registado: " + did);
        IdentityPayload payload = payload(identity);
        byte[] bytes = CanonicalSerializer.paraJson(payload).getBytes(StandardCharsets.UTF_8);
        String assinatura = Base64.getEncoder().encodeToString(SignatureService.assinar(bytes, chavePrivada));
        if (!SignatureService.verificar(bytes, Base64.getDecoder().decode(assinatura), identity.getPublicKey())) {
            throw new IllegalArgumentException("A chave privada nao corresponde a chave publica da identidade");
        }
        blockchain.adicionarTransacao(new Transaction(TransactionType.IDENTITY_REGISTERED,
                CanonicalSerializer.paraJson(payload), assinatura));
    }

    /**
     * Procura uma identidade registada pelo seu DID.
     *
     * @param did identificador a procurar
     * @return a identidade encontrada, ou {@code null} se nao existir
     */
    public Identity procurar(DID did) {
        if (did == null || blockchain.getCadeia().isEmpty()) return null;
        Identity encontrada = null;
        for (var bloco : blockchain.getCadeia()) {
            for (Transaction tx : bloco.getTransactions()) {
                if (tx.getType() != TransactionType.IDENTITY_REGISTERED) continue;
                try {
                    IdentityPayload payload = CanonicalSerializer.deJson(tx.getPayload(), IdentityPayload.class);
                    if (!did.toString().equals(payload.did())) continue;
                    PublicKey chave = decodePublicKey(payload.publicKey());
                    byte[] bytes = CanonicalSerializer.paraJson(payload).getBytes(StandardCharsets.UTF_8);
                    if (!SignatureService.verificar(bytes, Base64.getDecoder().decode(tx.getSignature()), chave)) continue;
                    encontrada = new Identity(DID.parse(payload.did()), IdentityRole.valueOf(payload.role()), chave);
                } catch (RuntimeException e) {
                    // Registos malformados ou assinaturas inválidas não criam uma identidade confiável.
                }
            }
        }
        return encontrada;
    }

    /**
     * Devolve a chave publica associada a um DID.
     *
     * @param did identificador
     * @return a chave publica, ou {@code null} se nao existir
     */
    public PublicKey getChavePublica(DID did) {
        Identity identity = procurar(did);
        return identity == null ? null : identity.getPublicKey();
    }

    /**
     * Verifica se um DID pertence a um emissor autorizado.
     *
     * @param did identificador a verificar
     * @return {@code true} se for um emissor autorizado
     */
    public boolean ehEmissorAutorizado(DID did) {
        Identity identity = procurar(did);
        return identity != null && identity.getRole() == IdentityRole.ISSUER
                && emissoresConfiaveis.contains(identity.getDid().toString());
    }

    private static IdentityPayload payload(Identity identity) {
        return new IdentityPayload(identity.getDid().toString(), identity.getRole().name(),
                Base64.getEncoder().encodeToString(identity.getPublicKey().getEncoded()));
    }

    private static PublicKey decodePublicKey(String encoded) {
        try {
            byte[] bytes = Base64.getDecoder().decode(encoded);
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
        } catch (Exception e) {
            throw new IllegalArgumentException("Chave pública RSA inválida", e);
        }
    }

    private record IdentityPayload(String did, String role, String publicKey) { }
}
