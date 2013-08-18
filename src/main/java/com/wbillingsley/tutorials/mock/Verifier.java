package com.wbillingsley.tutorials.mock;

/**
 * An encrypted verifier from a Dead Drop to prove that
 * synchronisation has taken place
 */
public class Verifier {

	private String result;
	
	public Verifier(String result) {
		this.result = result;
	}
	
	public String getResult() {
		return result;
	}
	
}
