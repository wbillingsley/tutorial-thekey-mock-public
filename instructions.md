# Instructions

This tutorial is providing you with two things

1. A project set up with JUnit, Mockito, the appropriate layout, and the 
   beginnings of some tests
2. A story and a puzzle, that is motivation to try out parts of Mockito in order 
   to solve it.

You may also find [Mockito's documentation](http://https://code.google.com/p/mockito/) useful.


## How to go about solving the puzzle

1. You're probably going to want to get the project into eclipse

        gradle eclipse
      
   Then open up Eclipse, and 
   `File` &rarr; `Import` &rarr; `Existing project into workspace`
   
2. The next thing you should do is *explore*.

   Run the tests. Initially they will fail.  
   
   Put a breakpoint on one of the lines where the phone syncs with the dead drop. 
   Step through it, and see how the phones respond.
   
   Right-click on one of the phone's methods. Have a look at its code to see what 
   happens.
   
   This is the sequence of calls that a phone makes with a deaddrop:
   
   ```
                          p:Phone                             d:Dead drop
                          -------                             -----------
   -- showDeadDrop(d) -->   |                                     |
                            | ----- startSync(p, code) ---------> |
                            |                                     |
                            | <--------- syncData(d) ------------ |
                            | . . . . . . SyncSuccess . . . . . > |
                            |                                     |
                            | <. . . . . Verifier . . . . . . . . |
    <. . . Verifier  . . . .|                               


   ```

3. Next try tackling the tests. Hints below.


### Verifying the dead drop works

You have a mock phone, and a real dead drop.

1. If you call `d.startSync(p, code)`, what do you want to verify the dead drop
   has called on the phone in response if the code was right?
   
2. What do you then want to verify the dead drop *didn't* call if the code was 
   wrong? 
   
   Hint: to verify that something wasn't called, verify that it was called zero 
   times.
   
   
### Capturing the American codes

Call `Scenario.getAmericanSuspects()` to get the suspects, and then for each suspect, you can call `suspect.getPhone()` to get their phone.

You now have the real phones, and a mock dead drop.

1. If we want to get a code out of a phone, the only way to do that is to call
   `phone.showDeadDrop(d)` The phone will obligingly send its code to the dead
   drop to try to start synchronising.  Fortunately we have a mock dead drop we
   could show them.
   
2. Our next challenge is to set the dead drop up so that it will record the 
   codes it receives.  Here's what we want to tell our mock dead drop: 
   
   > When d.startSync(phone, code) is called, put the phone and its code 
   > into the map.
   
   We want to do this for *any* phone and *any* code, so we're going to use
   Mockito's `any()` matchers.
   
   The phone and code are in the arguments. To get them, we're going to need to
   use Mockito's `thenAnswer` method.  
   
       when(
         d.startSync(any(SpyPhone.class), any(Code.class))
       ).thenAnswer(new Answer<Void>() {
       
         public Void answer(InvocationOnMock invocation) throws Throwable {
           Object args[] = invocation.getArguments();
           
           // The SpyPhone is in args[0]
           // The code is in args[1]
           // I'll leave you to work out the one line you need!
           
           return null;
         }
       
       }) 
       
   `startSync` is of return type `Void` to make it clear you shouldn't be 
   testing the return value -- it's always null.  It's `Void` rather than 
   `void` because you need to give `Answer<T>` a generic type.
   
   `Answer<Void>` is legal; `Answer<void>` is not.  As `Void` is the type of
   null, it just means you have to have a `return null` at the end.

3. Now the dead drop is set up to record phones, all you need to do is show
   it the American phones, and it'll duly record them for you.
   
   
   
### Finding the code

Now we have all the codes from the American phones.  But which one is the right one?  We still have a mock phone, and we can use it on the American dead drop.

How can we tell which is the right code? By seeing which one makes the American dead drop sync.

1. You can get the dead drop from `Scenario.getAmericanDeadDrop()`.

2. If we call `deadDrop.startSync(phone, code)`, the dead drop will call
   `phone.sync(deadDrop)` if the code was right.  So we want to set the mock 
   phone up to tell us when `phone.sync(deadDrop)` has been called.
   
   > when phone.sync(theCode), hit the alarms you've found it!
   
   While there are tidier ways of doing this, as this is a small contained
   test, you can get away with using an exception.
   
       when(phone.syncData(d)).thenThrow(new RuntimeException("gotcha"));       
       
   
3. We now have a phone that will throw an exception if the right code is used.
   We just have to loop through the phones, and catch the exception -- the code
   when the exception happens is the one.
   
   
### Finding the British suspects

Here's where it gets trickier.  The British phones check the verifier.  And the
verifier can only come from the real British dead drop.  So we have to catch 
the code using the real phones and the real dead drop.

The hint you were given was to do a man-in-the-middle attack.

1.  You can get the British dead drop with `Scenario.getBritishDeadDrop()`

2.  We're going to use a mock dead drop, but any time that dead drop is given 
    the right code, it's going to forward the request on to the real dead drop, 
    so that it can get the right verifier to return.
    
    We want to do this for any spy phone, but just the right code. (Spooking
    innocent phones doesn't matter -- they haven't done anything wrong.)
    
        when(
          middle.startSync(any(SpyPhone.class), theCode)
        ).thenAnswer(new Answer<Verifier>() {
        
         public Verifier answer(InvocationOnMock invocation) throws Throwable {
           Object args[] = invocation.getArguments();
           
           // The SpyPhone is in args[0]
           SpyPhone p = (SpyPhone)args[0];
           
           // I'll leave you to work out how to forward the call to the real
           // dead drop so you get the real verifier back, and how to record 
           // the phone and verifier as evidence
           
           // Return the real verifier so they don't get spooked
           return verifier;
         }        
        
        })
        
     Now the dead drop has been set up, you just need to show all the suspects'
     phones to it and it'll do its work. You can get the British suspects by 
     calling `Scenario.getBritishSuspects()` and you can get a suspect's phone 
     by calling `suspect.getPhone()`



### But one of them keeps getting away!

Don't worry, you've finished the tutorial. 

There's one British spy who is unreliable -- she *sometimes* gives the right 
code and *sometimes* gives the wrong code.  I included her to give you a last
little message about testing:

* It's quite hard to reliably catch that unreliable spy, because she doesn't 
behave predictably.

* It's even harder to reliably catch a bug that behaves unpredictably.

When you write unit tests, you want them to be repeatable and predictable. If
your tests sometimes fail and sometimes pass, because they don't have a 
predictable behaviour, then sometimes a bug is going to get through unnoticed.