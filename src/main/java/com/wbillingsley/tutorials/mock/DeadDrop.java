package com.wbillingsley.tutorials.mock;

import com.wbillingsley.encrypt.Encrypt;

/**
 * A Ruritanian dead drop, that tries to synchronise data with spy phones.
 */
public class DeadDrop {
	
	private Code code;
	
	private Code verifier = new Code();
	
	public DeadDrop(Code code) {
		this.code = code;
	}
	
	public void setVerifier(Code v) {
		this.verifier = v;
	}
	
	/**
	 * Attempts to synchronise data with a phone.
	 * If the code is correct, the dead drop will synchronise.
	 * If it isn't, it won't.
	 * The dead drop returns a cryptographic verifier, allowing
	 * the spy phone to check it was talking to a real dead drop
	 * (though rumour has it that only the cautious British spy
	 * phones actually check this.)
	 */
	public Verifier startSync(SpyPhone p, Code code) {
		if (this.code.equals(code)) {
			p.syncData(this);
			return new Verifier(Encrypt.encrypt(code.getB64(), verifier.getB64()));
		}
		return new Verifier(Encrypt.encrypt(code.getB64(), "failed"));		
	}
	
}
