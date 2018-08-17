package bgu.spl.a2;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.PUBLIC_MEMBER;

import com.sun.glass.ui.MenuItem.Callback;

import junit.framework.Assert;

public class PromiseTest {

	@Test
	public void testGet() {
		try {
			Promise<Integer> p = new Promise<>();
			try {
				p.get();
				Assert.fail();
			} catch (IllegalStateException ex) {
				p.resolve(5);
				int x = p.get();
				assertEquals(x, 5);
			} catch (Exception ex) {
				Assert.fail();
			}
		} catch (Exception ex) {
			Assert.fail();
		}
	}

	@Test
	public void testIsResolved() {
		try {
			Promise<Integer> p = new Promise<>();
			p.resolve(5);
			Boolean x = p.isResolved();
			assertEquals(x, true);
		} catch (Exception ex) {
			Assert.fail();
		}

		try {
			Promise<Integer> p = new Promise<>();
			Boolean x = p.isResolved();
			assertEquals(x, false);
		} catch (Exception ex) {
			Assert.fail();
		}

	}

	@Test
	public void testResolve() {
		try {
		// Tests if value changed after resolved already
		try {
			Promise<Integer> p = new Promise<>();
			p.resolve(5);
			try {
				p.resolve(6);
				Assert.fail();
			} catch (IllegalStateException ex) {
				int x = p.get();
				assertEquals(x, 5);
			} catch (Exception ex) {
				Assert.fail();
			}
		} catch (Exception ex) {
			Assert.fail();
		}

		// Tests if all callback were called
		Promise<Integer> p = new Promise<>();

		class callTest implements callback {

			private Boolean wasCalled = false;

			@Override
			public void call() {
				wasCalled = true;

			}

			public Boolean wasCalled() {
				return wasCalled;
			}

		}
		callTest[] arr = new callTest[5];
		for (int i = 0; i < 5; i++) {
			callTest call = new callTest();
			p.subscribe(call);
			arr[i] = call;
		}

		p.resolve(5);

		for (int i = 0; i < 5; i++) {
			try {
				assertEquals(arr[i].wasCalled, true);
			} catch (Exception ex) {
				Assert.fail();
			}
		}
		}
		catch (Exception ex) {
			Assert.fail();
		}
	}


	@Test
	public void testSubscribe() {
		try {
		//Tests if the callback were subscribed
		Promise<Integer> p1 = new Promise<>();

		class callTest implements callback {

			private Boolean wasCalled = false;

			@Override
			public void call() {
				wasCalled = true;

			}

			public Boolean wasCalled() {
				return wasCalled;
			}

		}
		callTest[] arr = new callTest[5];
		for (int i = 0; i < 5; i++) {
			callTest call = new callTest();
			p1.subscribe(call);
			arr[i] = call;
		}

		p1.resolve(5);

		for (int i = 0; i < 5; i++) {
			try {
				assertEquals(arr[i].wasCalled, true);
			} catch (Exception ex) {
				Assert.fail();
			}
		}
		//Tests if a callback called immediately if resolved already
		
		Promise<Integer> p2 = new Promise<>();
		callTest call1 = new callTest();
		callTest call2 = new callTest();
			try {				
				p2.subscribe(call1);
				Boolean x = call1.wasCalled();
				assertEquals(x, false);
			} catch (Exception ex) {
				Assert.fail();
			}
			p2.resolve(5);
			try {				
				p2.subscribe(call2);
				Boolean x = call2.wasCalled();
				assertEquals(x, true);
			} catch (Exception ex) {
				Assert.fail();
			}
		}
		catch (Exception ex) {
			Assert.fail();
		}
		
	
}
}
