package bgu.spl.a2;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Describes a monitor that supports the concept of versioning - its idea is
 * simple, the monitor has a version number which you can receive via the method
 * {@link #getVersion()} once you have a version number, you can call
 * {@link #await(int)} with this version number in order to wait until this
 * version number changes.
 *
 * you can also increment the version number by one using the {@link #inc()}
 * method.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 */
public class VersionMonitor {
	private AtomicInteger version = new AtomicInteger(0);
	private Object lock = new Object();
	private Object lock1 = new Object();

	public int getVersion() {
		return version.get();
	}
	/*
	 * Called when was a change in one of the queues to take from any queue.
	 * synchronized is used here to prevent two threads to change their state to awake at the same time.
	 */
	public void inc() {
		version.getAndIncrement();

		try {
			synchronized (lock1) {
				notifyAll();
			}

		} catch (Exception e) {

		}

	}
/*
 * Called when no actions to take from any queue.
 * synchronized is used here to prevent two threads to change their state to wait at the same time.
 */
	public void await(int version) throws InterruptedException {
		if (this.version.get() == version)
			try {
				synchronized (lock) {

					wait();
				}

			} catch (Exception e) {

			}

	}
}
