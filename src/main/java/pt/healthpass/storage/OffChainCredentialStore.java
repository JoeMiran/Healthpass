package pt.healthpass.storage;

import java.util.List;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pt.healthpass.config.HealthPassConfig;
import pt.healthpass.credential.VaccinationCredential;
import pt.healthpass.crypto.CanonicalSerializer;
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

    private final Path diretorio;

    public OffChainCredentialStore() {
        this(Path.of(HealthPassConfig.DIRETORIO_CREDENCIAIS));
    }

    public OffChainCredentialStore(Path diretorio) {
        this.diretorio = Objects.requireNonNull(diretorio, "diretorio obrigatorio");
    }

    /**
     * Guarda uma credencial localmente.
     *
     * @param credencial credencial a guardar
     */
    public void guardar(Credential credencial) {
        Objects.requireNonNull(credencial, "credencial obrigatoria");
        validarId(credencial.getCredentialId());
        if (!(credencial instanceof VaccinationCredential)) {
            throw new IllegalArgumentException("Tipo de credencial ainda nao suportado pelo armazenamento");
        }
        Path destino = ficheiro(credencial.getCredentialId());
        Path temporario = null;
        try {
            Files.createDirectories(diretorio);
            temporario = Files.createTempFile(diretorio, "credential-", ".tmp");
            Files.writeString(temporario, CanonicalSerializer.paraJson(credencial), StandardCharsets.UTF_8);
            try {
                Files.move(temporario, destino, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao guardar credencial off-chain", e);
        } finally {
            if (temporario != null) try { Files.deleteIfExists(temporario); } catch (IOException ignored) { }
        }
    }

    /**
     * Carrega uma credencial pelo seu identificador.
     *
     * @param credentialId identificador da credencial
     * @return a credencial, ou {@code null} se nao existir
     */
    public Credential carregar(String credentialId) {
        Path caminho = ficheiro(credentialId);
        if (!Files.exists(caminho)) return null;
        try {
            String json = Files.readString(caminho, StandardCharsets.UTF_8);
            JsonNode raiz = new ObjectMapper().readTree(json);
            if ("VaccinationCredential".equals(raiz.path("type").asText())) {
                return CanonicalSerializer.deJson(json, VaccinationCredential.class);
            }
            throw new IllegalArgumentException("Tipo de credencial persistida desconhecido");
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler credencial off-chain", e);
        }
    }

    /**
     * Lista as credenciais guardadas localmente.
     *
     * @return a lista de credenciais
     */
    public List<Credential> listar() {
        if (!Files.isDirectory(diretorio)) return List.of();
        try (var ficheiros = Files.list(diretorio)) {
            List<Credential> resultado = new ArrayList<>();
            for (Path caminho : ficheiros.filter(p -> p.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(Path::toString)).toList()) {
                String id = caminho.getFileName().toString().replaceFirst("\\.json$", "");
                Credential credencial = carregar(id);
                if (credencial != null) resultado.add(credencial);
            }
            return List.copyOf(resultado);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao listar credenciais off-chain", e);
        }
    }

    private Path ficheiro(String credentialId) {
        validarId(credentialId);
        return diretorio.resolve(credentialId + ".json");
    }

    private static void validarId(String id) {
        if (id == null || !id.matches("[A-Za-z0-9._-]{1,128}")) {
            throw new IllegalArgumentException("credentialId inválido para nome de ficheiro");
        }
    }
}
