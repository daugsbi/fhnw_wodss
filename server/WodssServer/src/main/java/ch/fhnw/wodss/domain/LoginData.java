package ch.fhnw.wodss.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.hibernate.annotations.Type;

@Entity
public class LoginData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Lob
	private byte[] password;
	@Lob
	private byte[] salt;
	@Type(type = "org.hibernate.type.NumericBooleanType")
	private boolean validated;
	private String validationCode;
	private String resetCode;
	private Date resetExpiration;

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the password
	 */
	public byte[] getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(byte[] password) {
		this.password = password;
	}

	/**
	 * @return the salt
	 */
	public byte[] getSalt() {
		return salt;
	}

	/**
	 * @param salt
	 *            the salt to set
	 */
	public void setSalt(byte[] salt) {
		this.salt = salt;
	}

	/**
	 * @return the validated
	 */
	public boolean isValidated() {
		return validated;
	}

	/**
	 * @param validated
	 *            the validated to set
	 */
	public void setValidated(boolean validated) {
		this.validated = validated;
	}

	/**
	 * @return the validationCode
	 */
	public String getValidationCode() {
		return validationCode;
	}

	/**
	 * @param validationCode
	 *            the validationCode to set
	 */
	public void setValidationCode(String validationCode) {
		this.validationCode = validationCode;
	}

	/**
	 * @return the resetCode
	 */
	public String getResetCode() {
		return resetCode;
	}

	/**
	 * @param resetCode
	 *            the resetCode to set
	 */
	public void setResetCode(String resetCode) {
		this.resetCode = resetCode;
	}

	/**
	 * @return the resetExpiration
	 */
	public Date getResetExpiration() {
		return resetExpiration;
	}

	/**
	 * @param resetExpiration the resetExpiration to set
	 */
	public void setResetExpiration(Date resetExpiration) {
		this.resetExpiration = resetExpiration;
	}
}
