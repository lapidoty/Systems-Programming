package bgu.spl.a2;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

public class VersionMonitorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetVersion() {
		try {
		VersionMonitor vm = new VersionMonitor();
		int version = vm.getVersion();
		// Test to get first version
		assertEquals(version, 0);

		vm.inc();

		// Test to get second version
		assertEquals(version, 1);
		}catch (Exception ex) {
			Assert.fail();
		}

	}

	@Test
	public void testInc() {
		try {
		VersionMonitor vm = new VersionMonitor();
		int version = vm.getVersion();
		Boolean[] startedToWait= {false};
		// Test to before increase
		assertEquals(version, 0);

		vm.inc();

		// Test to without increase
		assertEquals(version, 1);

		Boolean[] wasStoped = { true };
		Runnable task = () -> {
			try {
				startedToWait[0]=true;
				vm.await(version);
			} catch (InterruptedException e) {
			}
			wasStoped[0] = false;
		};
		Thread thread = new Thread(task);
		thread.start();
		Thread.sleep(1000);
		// Test if the thread waits
		assertEquals(wasStoped, true);

		vm.inc();
		// Test if the thread does not wait anymore
		assertEquals(wasStoped, false);
		}
		catch (Exception ex) {
			Assert.fail();
		}

	}

	@Test
	public void testAwait() {
		try {
		VersionMonitor vm = new VersionMonitor();
		int version = vm.getVersion();
		Boolean[] wasStoped = { true };
		Runnable task = () -> {
			try {
				vm.await(version);
			} catch (InterruptedException e) {
			}
			wasStoped[0] = false;
		};
		Thread thread = new Thread(task);
		thread.start();
		Thread.sleep(1000);
		// Test if the thread waits
		assertEquals(wasStoped, true);

		vm.inc();
		// Test if the thread does not wait anymore
		assertEquals(wasStoped, false);

	
	}
	catch (Exception ex) {
		Assert.fail();
	}
	}
}
