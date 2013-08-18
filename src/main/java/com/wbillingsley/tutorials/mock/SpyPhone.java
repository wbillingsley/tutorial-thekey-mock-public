package com.wbillingsley.tutorials.mock;

public interface SpyPhone {
	
	/**
	 * A unique signature that identifies this phone. 
	 */
	public String getSignature();
	
	/**
	 * Call this to "bring the phone in range" of a dead drop.
	 * The phone should then try to synchronise with the dead drop
	 * by calling DeadDrop.startSync 
	 */
	public Verifier showDeadDrop(DeadDrop d);
	
	/**
	 * Called by the dead drop to synchronise data.
	 */
	public SyncSuccess syncData(DeadDrop d);	

	/**
	 * British phones have an extra check in them, whereby they verify
	 * that they got a response from a Ruritanian dead drop. Calling
	 * this reveals whether or not the phone has been spooked.
	 * (Note that even innocent people's phones can be spooked.)
	 */
	public boolean getSpooked();

}
