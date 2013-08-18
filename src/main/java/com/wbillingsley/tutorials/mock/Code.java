package com.wbillingsley.tutorials.mock;

import com.wbillingsley.encrypt.Encrypt;

/**
 * A code, emitted by a spy phone
 */
public class Code {

	private String code = Encrypt.genSaltB64();
	
	/**
	 * Get the code as a Base64 encoded string
	 */
	public String getB64() {
		return code;
	}

	/** 
	 * Get the code in a printable form
	 */
	public String toString() {
		return "code(" + code + ")";
	}
}
