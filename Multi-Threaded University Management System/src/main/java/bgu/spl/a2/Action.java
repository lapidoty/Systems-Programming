package bgu.spl.a2;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.annotations.Expose;

/**
 * an abstract class that represents an action that may be executed using the
 * {@link ActorThreadPool}
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add to this class can
 * only be private!!!
 *
 * @param <R>
 *            the action result type
 */
public abstract class Action<R> {
	protected ActorThreadPool pool;
	protected String actorId;
	protected PrivateState actorState;
	@Expose
	private String name;
	protected volatile AtomicInteger count;
	protected volatile Promise<R> promise;
	protected AtomicBoolean resumed;
	private callback call;
	protected AtomicBoolean addToRecord;

	/**
	 * start handling the action - note that this method is protected, a thread
	 * cannot call it directly.
	 */
	protected abstract void start();

	/**
	 *
	 * start/continue handling the action
	 *
	 * this method should be called in order to start this action or continue its
	 * execution in the case where it has been already started.
	 *
	 * IMPORTANT: this method is package protected, i.e., only classes inside the
	 * same package can access it - you should *not* change it to
	 * public/private/protected
	 *
	 */
	/* package */ final void handle(ActorThreadPool pool, String actorId, PrivateState actorState) {
		this.pool = pool;
		this.actorState = actorState;
		this.actorId = actorId;

		if (resumed == null) {
			this.addToRecord = new AtomicBoolean(true);
			actorState.addRecord(getActionName());
			resumed = new AtomicBoolean(false);
		}
		if (count == null) {
			count = new AtomicInteger(0);
		}

		if (!resumed.get()) {

			start();

		} else {

			call.call();
		}
	}

	/**
	 * add a callback to be executed once *all* the given actions results are
	 * resolved
	 * 
	 * Implementors note: make sure that the callback is running only once when all
	 * the given actions completed.
	 *
	 * @param actions
	 * @param callback
	 *            the callback to execute once all the results are resolved
	 */
	protected final void then(Collection<? extends Action<?>> actions, callback callback) {
		resumed.set(true);
		addToRecord.set(false);
		this.call = callback;

		pool.setQueueForFree(actorId);

		while (count.get() != 0) {

			pool.findActionToFetch();

		}

		if (count.get() == 0 && resumed.get())
			pool.submit(this, actorId, actorState);

	}

	/**
	 * resolve the internal result - should be called by the action derivative once
	 * it is done.
	 *
	 * @param result
	 *            - the action calculated result
	 */
	protected final void complete(R result) {
		if (promise == null) {
			promise = new Promise<R>();
		}
		promise.resolve(result);

	}

	/**
	 * @return action's promise (result)
	 */
	public final Promise<R> getResult() {
		if (promise == null) {
			promise = new Promise<R>();
			return promise;
		} else
			return promise;
	}

	/**
	 * send an action to an other actor
	 * 
	 * @param action
	 *            the action
	 * @param actorId
	 *            actor's id
	 * @param actorState
	 *            actor's private state (actor's information)
	 * 
	 * @return promise that will hold the result of the sent action
	 */
	public Promise<?> sendMessage(Action<?> action, String actorId, PrivateState actorState) {

		action.getResult().subscribe(() -> {

			count.getAndDecrement();
			resumed.set(true);

		});

		count.getAndIncrement();
		pool.submit(action, actorId, actorState);
		return action.getResult();
	}

	/**
	 * set action's name
	 * 
	 * @param actionName
	 */
	public void setActionName(String actionName) {
		this.name = actionName;

	}

	/**
	 * @return action's name
	 */
	public String getActionName() {
		return name;
	}

}