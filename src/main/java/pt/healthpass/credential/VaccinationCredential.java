package pt.healthpass.credential;

/**
 * Credencial de vacinacao (dados <b>sinteticos</b>).
 *
 * <p>Primeiro caso de uso do HealthPass. Acrescenta a {@link Credential} os
 * campos especificos da vacinacao:</p>
 * <ul>
 *   <li>{@code vaccineCode} — codigo da vacina (ex.: "VACCINE-DEMO-A");</li>
 *   <li>{@code administrationDate} — data de administracao.</li>
 * </ul>
 *
 * <p><b>AVISO:</b> usar exclusivamente dados ficticios de demonstracao. Nunca
 * introduzir dados clinicos reais, nomes ou numeros de utente.</p>
 *
 * <p><b>Origem:</b> novo.</p>
 *
 * @author HealthPass
 */
public class VaccinationCredential extends Credential {

    /** Codigo da vacina de demonstracao. */
    private String vaccineCode;

    /** Data de administracao (formato ISO-8601, texto). */
    private String administrationDate;

    /** Construtor vazio para serializacao JSON. */
    public VaccinationCredential() {
        this.type = "VaccinationCredential";
    }

    public VaccinationCredential(String credentialId, String issuerDID, String holderDID,
                                 String issuedAt, String expiresAt, String vaccineCode,
                                 String administrationDate) {
        this();
        this.credentialId = credentialId;
        this.issuerDID = issuerDID;
        this.holderDID = holderDID;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.vaccineCode = vaccineCode;
        this.administrationDate = administrationDate;
    }

    /**
     * @return o codigo da vacina.
     */
    public String getVaccineCode() {
        return vaccineCode;
    }

    /**
     * @return a data de administracao.
     */
    public String getAdministrationDate() {
        return administrationDate;
    }

    public void setVaccineCode(String vaccineCode) { this.vaccineCode = vaccineCode; }
    public void setAdministrationDate(String administrationDate) { this.administrationDate = administrationDate; }
}
