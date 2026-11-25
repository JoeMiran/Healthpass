package pt.healthpass.storage;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyFactory;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Objects;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import pt.healthpass.crypto.CanonicalSerializer;

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

    private static final int ITERACOES_PBKDF2 = 600_000;
    private static final int TAMANHO_SALT = 16;
    private static final int TAMANHO_IV = 12;
    private final Path diretorio;

    public KeyStoreService() { this(Path.of("data/demo/keys")); }

    public KeyStoreService(Path diretorio) {
        this.diretorio = Objects.requireNonNull(diretorio, "diretorio obrigatorio");
    }

    /**
     * Guarda uma chave publica num ficheiro {@code .pub}.
     *
     * @param chave chave publica
     * @param nomeUtilizador nome base do ficheiro
     */
    public void guardarChavePublica(PublicKey chave, String nomeUtilizador) {
        Objects.requireNonNull(chave, "chave publica obrigatoria");
        escrever(caminho(nomeUtilizador, ".pub"), Base64.getEncoder().encodeToString(chave.getEncoded()));
    }

    /**
     * Guarda uma chave privada num ficheiro {@code .priv}, cifrada com password.
     *
     * @param chave chave privada
     * @param nomeUtilizador nome base do ficheiro
     * @param password password de protecao
     */
    public void guardarChavePrivada(PrivateKey chave, String nomeUtilizador, String password) {
        Objects.requireNonNull(chave, "chave privada obrigatoria");
        validarPassword(password);
        byte[] salt = new byte[TAMANHO_SALT];
        byte[] iv = new byte[TAMANHO_IV];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        random.nextBytes(iv);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, derivarChave(password, salt), new GCMParameterSpec(128, iv));
            byte[] cifrado = cipher.doFinal(chave.getEncoded());
            String json = CanonicalSerializer.paraJson(new Envelope(
                    Base64.getEncoder().encodeToString(salt), Base64.getEncoder().encodeToString(iv),
                    Base64.getEncoder().encodeToString(cifrado)));
            escrever(caminho(nomeUtilizador, ".priv"), json);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao cifrar chave privada", e);
        }
    }

    /**
     * Carrega uma chave publica de um ficheiro {@code .pub}.
     *
     * @param nomeUtilizador nome base do ficheiro
     * @return a chave publica
     */
    public PublicKey carregarChavePublica(String nomeUtilizador) {
        try {
            byte[] encoded = Base64.getDecoder().decode(Files.readString(caminho(nomeUtilizador, ".pub"), StandardCharsets.UTF_8));
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao carregar chave pública", e);
        }
    }

    /**
     * Carrega e decifra uma chave privada de um ficheiro {@code .priv}.
     *
     * @param nomeUtilizador nome base do ficheiro
     * @param password password de protecao
     * @return a chave privada
     */
    public PrivateKey carregarChavePrivada(String nomeUtilizador, String password) {
        validarPassword(password);
        try {
            String json = Files.readString(caminho(nomeUtilizador, ".priv"), StandardCharsets.UTF_8);
            Envelope envelope = CanonicalSerializer.deJson(json, Envelope.class);
            byte[] salt = Base64.getDecoder().decode(envelope.salt());
            byte[] iv = Base64.getDecoder().decode(envelope.iv());
            byte[] cifrado = Base64.getDecoder().decode(envelope.ciphertext());
            if (salt.length != TAMANHO_SALT || iv.length != TAMANHO_IV) {
                throw new IllegalArgumentException("Envelope de chave privada inválido");
            }
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, derivarChave(password, salt), new GCMParameterSpec(128, iv));
            byte[] claro = cipher.doFinal(cifrado);
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(claro));
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao carregar chave privada (password incorreta ou ficheiro inválido)", e);
        }
    }

    private Path caminho(String nome, String extensao) {
        if (nome == null || !nome.matches("[A-Za-z0-9._-]{1,128}")) {
            throw new IllegalArgumentException("Nome de chave inválido");
        }
        return diretorio.resolve(nome + extensao);
    }

    private void escrever(Path caminho, String conteudo) {
        try {
            Files.createDirectories(diretorio);
            Files.writeString(caminho, conteudo, StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Falha ao guardar chave", e);
        }
    }

    private static SecretKeySpec derivarChave(String password, byte[] salt) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERACOES_PBKDF2, 256);
        try {
            return new SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded(), "AES");
        } finally {
            spec.clearPassword();
        }
    }

    private static void validarPassword(String password) {
        if (password == null || password.isEmpty()) throw new IllegalArgumentException("Password obrigatória");
    }

    private record Envelope(String salt, String iv, String ciphertext) { }
}
