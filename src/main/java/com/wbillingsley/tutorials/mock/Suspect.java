package com.wbillingsley.tutorials.mock;

/**
 * A spy, who may or may not be a double agent for the Ruritanian Secret Service! 
 */
public interface Suspect {

	/** 
	 * All suspects have a phone, so this never returns null.
	 */
	public SpyPhone getPhone();  
	
	/**
	 * All suspects have a name, so this never returns null.
	 */
	public String getName();
	
}
