package com.wbillingsley.tutorials.mock;

import java.util.Map;
import java.util.HashMap;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;


import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

public class TheKeyTest {

	static Scenario scenario = new Scenario();
	
	/* 
	 * Good morning, Mr Drake.  I trust you have read the mission briefing.
	 * 
	 * This is our rough plan of action.  You'll need to fill it in until
	 * all the tests succeed.
	 */
	
	
	/**
	 * First, I'm pleased to tell you that our engineering section has obtained 
	 * a Ruritanian dead drop device. We'd very much like to verify it works.
	 * Then we'll move on to catching those double agents. 
	 */
	@Test 
	public void deadDrop() {
		
		/*
		 * We've got as far as setting up the dead drop with a code.
		 * This should be it below.
		 */
		Code testCode = new Code();
		DeadDrop deadDrop = new DeadDrop(testCode);
		
		/*
		 * We'll need a fake spy phone to test it with. Let's get
		 * Mockito to make us one
		 */
		// SpyPhone p = ...
		SpyPhone p = mock(SpyPhone.class);
		
		/*
		 * Next, we want to try initiating a sync with the dead drop using the 
		 * right code, and check it did try to sync with our fake phone.
		 */
		// deadDrop.startSync( ...
		// verify( ...
		deadDrop.startSync(p, testCode);
		verify(p).syncData(deadDrop);
		
		/*
		 * We'd better reset the fake phone, or it's going to 
		 * remember the previous interaction.
		 */
		reset(p);
		
		/*
		 * Now let's make sure the dead drop isn't syncing with phones that
		 * give the wrong code.
		 * 
		 * Mockito tells me there's a verify call you can make that could be used
		 * to check a method's been called zero times.
		 */
		// when( ...
		// deadDrop.startSync( ...
		// verify( ...
		Code wrongCode = new Code();
		deadDrop.startSync(p, wrongCode);
		verify(p, times(0)).syncData(deadDrop);		
	}
	
	


	/**
	 * If that first test is working, our engineering department will be very happy.
	 * But that's not going to do us much good if those double agents sell us all out
	 * to Ruritania before we can do anything with it.
	 * 
	 * Let's catch them before they destroy us all!
	 * 
	 * I've outlined the plan below, but you're going to have to work out how
	 * to do each of the steps.
	 * 
	 * (The functions called by this test don't do anything yet. You'll need to
	 * change that!)
	 */
	@Test
	public void catchThem() {
		
		/*
		 * First, we should capture the codes for all the American phones.
		 * American phones are a bit careless -- they don't check the cryptographic
		 * verifier from the dead drop, so we should be able to get their codes 
		 * without spooking the agents.
		 */
		Map<SpyPhone, Code> phoneToCode = recordTheAmericanCodes();		
		System.out.println("Here's a map of the codes the American phones emit:");
		for (SpyPhone phone: phoneToCode.keySet()) {
			System.out.println(
				scenario.getOwnerOf(phone).getName() + 
				" has a phone emitting " +
				phoneToCode.get(phone)
			);
		}
		assertEquals(
				scenario.getAmericanSuspects().length, 
				phoneToCode.keySet().size(),
				"The map of phones to codes has the wrong size"
		);
		
		/*
		 * Next we're going to need to find out which of those codes works
		 * with the dead drop.
		 */
		Code theCode = findTheCode(phoneToCode);
		
		/*
		 * Then we're quietly going to arrest every American agent using one 
		 * of those phones.
		 */
		for (SpyPhone phone : phoneToCode.keySet()) {
			if (phoneToCode.get(phone).equals(theCode)) {
				Suspect crook = scenario.getOwnerOf(phone); 
				assertTrue(scenario.accuseAmerican(crook, theCode));				
			}
		}
		
		/*
		 * In Britain, we'll have to be more careful. The British phones
		 * check the crypto verifier from the dead drop, so what ever we
		 * do, we've got to make sure it thinks it's talking to a read
		 * Ruritanian dead drop.  And sorry, that one found earlier
		 * won't help us -- it has a different verifier.
		 */
		Map<SpyPhone, Verifier> pair = catchTheBritishCrook(theCode);
		for (SpyPhone phone : pair.keySet()) {
			Suspect crook = scenario.getOwnerOf(phone); 
			assertTrue(scenario.accuseBriton(crook, theCode, pair.get(phone)));				
		}
		
		/*
		 * Once you're done, let Hobbs know, so we can give you your usual fee.
		 */
		assertTrue(scenario.callWhenComplete());
	}
	
	/**
	 * Like I said -
	 * 
	 * First, we should capture the codes for all the American phones.
	 * American phones are a bit careless -- they don't check the cryptographic
	 * verifier from the dead drop, so we should be able to get their codes 
	 * without spooking the agents.	 
	 * 
	 * I think you'll need a fake dead drop from Mockito to do this. Oh, 
	 * Mockito left a piece of paper lying around that said:
	 * 
	 * when(d.startSync(any(SpyPhone.class), any(Code.class))).then(new Answer<Void>() {
	 * 
	 * Not sure if that helps you.
	 */
	private Map<SpyPhone, Code> recordTheAmericanCodes() {
		
		final Map<SpyPhone, Code> phoneToCode = new java.util.HashMap<SpyPhone, Code>();
		
		DeadDrop d = mock(DeadDrop.class);
				
		when(d.startSync(any(SpyPhone.class), any(Code.class))).then(new Answer<Void>() {			
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();				
				phoneToCode.put((SpyPhone)args[0], ((Code)args[1]));
				return null;
			}
		});
			
		for (Suspect s : scenario.getAmericanSuspects()) {
			s.getPhone().showDeadDrop(d);
		}
		
		return phoneToCode;
	}
	
	/**
	 * Play back the codes to the American dead drop, and find out which one syncs!
	 * We'll need a fake phone for this.
	 */
	private Code findTheCode(Map<SpyPhone, Code> phoneCodes) {
		SpyPhone p = mock(SpyPhone.class);
		DeadDrop d = scenario.getAmericanDeadDrop();
		when(p.syncData(d)).thenThrow(new RuntimeException("Gotcha"));
		
		for (SpyPhone phone: phoneCodes.keySet()) {
			try {
				d.startSync(p, phoneCodes.get(phone));
			} catch (RuntimeException ex) {
				if (ex.getMessage().equals("Gotcha")) {
					return phoneCodes.get(phone);
				}
			}
		}
		return null;
	}
	
	/**
	 * In Britain, we'll have to be more careful. The British phones
	 * check the crypto verifier from the dead drop, so what ever we
	 * do, we've got to make sure it thinks it's talking to a read
	 * Ruritanian dead drop. Otherwise it's going to spook the phone
	 * and our suspects will flee before we can arrest them.
	 * 
	 * And sorry, the dead drop we own has a different verifier
	 * than the Ruritanian ones, so we're going to need to use the real
	 * things.  (Scenario.getBritishDeadDrop())
	 *
	 * I think I heard one of our engineers muttering about these things
	 * being susceptible to a "man-in-the-middle" attack. Not sure if 
	 * that helps you. I guess if there's some way you could make one
	 * of Mockito's fakes behave as if it was the real thing?
	 */
	public Map<SpyPhone, Verifier> catchTheBritishCrook(final Code theCode) {
		/*
		 * Play back the codes to the American dead drop, and find out which one syncs!
		 * We'll need a fake phone for this.
		 */		
		final DeadDrop d = scenario.getBritishDeadDrop();
		DeadDrop middle = mock(DeadDrop.class);
		
		final Map<SpyPhone, Verifier> pair = new HashMap<SpyPhone, Verifier>();
		
		when(middle.startSync(any(SpyPhone.class), eq(theCode))).thenAnswer(new Answer<Verifier>() {

			@Override
			public Verifier answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				
				Code code = (Code)args[1];
				SpyPhone phone = (SpyPhone)args[0];
				Verifier thisV = d.startSync(phone, code);
				
				System.out.println("We've caught someone!");
				pair.put(phone, thisV);
				return thisV;
			}
			
		});
		
		for (Suspect s : scenario.getBritishSuspects()) {
			s.getPhone().showDeadDrop(middle);
		}
		
		return pair;
	}	
	
}
