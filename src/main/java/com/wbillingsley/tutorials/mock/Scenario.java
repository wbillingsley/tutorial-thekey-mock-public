package com.wbillingsley.tutorials.mock;

import java.util.HashSet;
import java.util.Set;

import com.wbillingsley.encrypt.Encrypt;

/**
 * The scenario you will be testing about.
 * Warning: the codes and verifiers change every time!
 */
public class Scenario {
	
	/**
	 * The secret code that this dead drop responds to
	 */
	private Code code = new Code();
	
	/**
	 * The verifier on real dead drops
	 */
	private Code verifier = new Code();
	
	/**
	 * Villains that have been caught
	 */
	private Set<Suspect> caught = new HashSet<Suspect>();
	
	/**
	 * The dead drop in Britain
	 */
	private DeadDrop britishDeadDrop = new DeadDrop(code);
	{
		britishDeadDrop.setVerifier(verifier);
	}
	
	/**
	 * The dead drop in America
	 */
	private DeadDrop americanDeadDrop = new DeadDrop(code);
	{
		americanDeadDrop.setVerifier(verifier);
	}
	


	private Suspect newSuspect(final String n, final SpyPhone p) {
		return new Suspect() {
		
			public String getName() {
				return n;
			}			
			
			public SpyPhone getPhone() {
				return p;
			}			
		};		
	}		
	
	
	/**
	 * The suspects in the British secret service 
	 */
	public Suspect[] getBritishSuspects() { 
		return brits.clone(); 
	}
	
	/**
	 * The suspects in the American secret service 
	 */
	public Suspect[] getAmericanSuspects() {
		return americans.clone();
	}
	
	/**
	 * The dead drop in America
	 */
	public DeadDrop getAmericanDeadDrop() {
		return americanDeadDrop;
	}
	
	/**
	 * The dead drop in Britain
	 */
	public DeadDrop getBritishDeadDrop() {
		return britishDeadDrop;
	}

	/**
	 * Who's phone is this?
	 */
	public Suspect getOwnerOf(String phoneSignature) {
		for (Suspect s : brits) {
			if (s.getPhone().getSignature().equals(phoneSignature)) {
				return s;
			}			
		}
		for (Suspect s : americans) {
			if (s.getPhone().getSignature().equals(phoneSignature)) {
				return s;
			}			
		}
		throw new IllegalArgumentException("No suspect carries that phone");
	}
	
	/**
	 * Who's phone is this?
	 */
	public Suspect getOwnerOf(SpyPhone p) {
		return getOwnerOf(p.getSignature());
	}

	/**
	 * Checks whether an accusation should be successful.
	 */
	private boolean accuseChecks(Suspect s, Code code) {
		if (!(s.getPhone() instanceof InternalSpyPhone)) {
			throw new IllegalArgumentException("No, that's not even a real phone in my pocket!");
		}
		
		InternalSpyPhone p = (InternalSpyPhone) s.getPhone();
		if (p.p <= 0) {
			throw new IllegalArgumentException("How dare you make unfounded accusations! (wrong suspect)");
		}
		
		if (!this.code.equals(code)) {
			throw new IllegalArgumentException("How dare you make unfounded accusations! (wrong code)");
		}
		
		if (p.getSpooked()) {
			throw new IllegalArgumentException("We'd love to accuse them... if only we could find them. They've fled! (You spooked them)");
		}
		
		return true;
	}
		
	/**
	 * Players should call this when they wish to accuse an American suspect
	 */
	public boolean accuseAmerican(Suspect s, Code code) {
		boolean b = accuseChecks(s, code);
		
		System.out.println(s.getName() + " says 'Ok, I give up. I'll come quietly.'");
		caught.add(s);
		return b;
	}
	
	/**
	 * Players should call this when they wish to accuse a British suspect
	 */
	public boolean accuseBriton(Suspect s, Code code, Verifier v) {
		String check = Encrypt.encrypt(this.code.getB64(), verifier.getB64());
		
		boolean b = accuseChecks(s, code);
		
		if (!check.equals(v.getResult())) {
			System.out.println("You spooked the suspect and they fled.  (wrong verifier)");
			System.out.println("Your verifier is " + v.getResult());
			System.out.println("The right verifier, this time, is " + check);
			throw new IllegalArgumentException("They're denying it. (wrong verifier)");
		}
		
		System.out.println(s.getName() + " says 'Ok, I give up. I'll come quietly.'");
		caught.add(s);
		return b;
	}	
	
	/**
	 * Players should call this when they have finished.
	 */
	public boolean callWhenComplete() {
		if (caught.size() >= 3) {
			System.out.println("Congratuations, Mr Drake, we've caught them all! What would we do without you!");
			return true;
		} else if (caught.size() == 2) {
			System.out.println("Well done, Mr Drake, we've caught two of them! But I have a sneaking suspicion someone might have got away.");
			return true;			
		} else if (caught.size() == 1) {
			System.out.println("We only caught one of them. That's really not good enough.");
			return false;			
		} else {
			System.out.println("We caught nobody! This is a disaster!");
			return false;			
		}
	}

	
	
	
	
	
	/*-------------------
	 *  SPOILERS AHEAD
	 *  DON'T READ BEYOND THIS POINT, OR IT'LL GIVE AWAY WHO THE VILLAINS ARE
	 *------------------*/
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * An internal SpyPhone class that can produce either
	 * the right code or the wrong code, so that it's not
	 * too easy to guess by who has which phone.
	 */
	private class InternalSpyPhone implements SpyPhone {		
		private String signature = new Code().getB64();
		
		private Code code = new Code();
		
		private double p;
		
		private boolean spookable;

		private InternalSpyPhone(double p, boolean spookable) {
			this.p = p;
		}
		
		@Override
		public String getSignature() {
			return this.signature;
		}
		
		@Override
		public SyncSuccess syncData(DeadDrop d) {
			return new SyncSuccess();
		}			

		@Override
		public Verifier showDeadDrop(DeadDrop d) {			
			if (Math.random() < p) {
				Verifier v = d.startSync(this, Scenario.this.code);		
				String check = Encrypt.encrypt(this.code.getB64(), verifier.getB64());
				if (spookable && (v == null || !v.getResult().equals(check))) {
					System.out.println("One of our suspects has been spooked");
					spooked = true;
				}
				return v;
			} else {
				Verifier v = d.startSync(this, this.code);		
				String check = Encrypt.encrypt(this.code.getB64(), "failed");
				if (spookable && (v == null || !v.getResult().equals(check))) {
					System.out.println("One of our suspects has been spooked");
					spooked = true;
				}
				return v;
			}
		}		
		
		private boolean spooked = false;
		
		public boolean getSpooked() {
			return spooked;
		}
	}		
	
	private Suspect[] brits = { 
			newSuspect("Algernon Moncrieff", new InternalSpyPhone(0d, true)),
			newSuspect("Bertram Wooster", new InternalSpyPhone(0d, true)),
			newSuspect("Cecily Cardew", new InternalSpyPhone(0.2d, true)),
			newSuspect("Dahlia Travers", new InternalSpyPhone(0d, true)),
			newSuspect("Edwin Worplestone",  new InternalSpyPhone(1.1d, true))			
	};
	
	private Suspect[] americans = { 
			newSuspect("Aline Hemmingway", new InternalSpyPhone(0d, false)),
			newSuspect("Bingo Little", new InternalSpyPhone(0d, false)),
			newSuspect("Cora Bellinger", new InternalSpyPhone(1.1d, false)),
			newSuspect("Dwight Stoker", new InternalSpyPhone(0d, false)),
			newSuspect("Enoch Simpson", new InternalSpyPhone(0d, false))			
	};	
	
}
