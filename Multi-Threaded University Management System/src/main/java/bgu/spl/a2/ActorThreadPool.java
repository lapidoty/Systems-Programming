package bgu.spl.a2;


import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * represents an actor thread pool - to understand what this class does please
 * refer to your assignment.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 */
public class ActorThreadPool {
	private volatile ConcurrentHashMap<String, PrivateState> actorsPrivateStates; // Note: Holds all the actors private
																					// states
	private volatile ConcurrentHashMap<String, ActionQueue> actorsQueues; // Note: Holds all the actors queues
	private volatile LinkedList<String> actorsIds;// Note: Holds all the actors ids
	private Vector<Thread> threadsPool; // Note: Holds all the threads
	private VersionMonitor vMonitor; // Note: Holds the number of current version
	private boolean run;

	/**
	 * creates a {@link ActorThreadPool} which has nthreads. Note, threads should
	 * not get started until calling to the {@link #start()} method.
	 *
	 * Implementors note: you may not add other constructors to this class nor you
	 * allowed to add any other parameter to this constructor - changing this may
	 * cause automatic tests to fail..
	 *
	 * @param nthreads
	 *            the number of threads that should be started by this thread pool
	 */
	public ActorThreadPool(int nthreads) {
		threadsPool = new Vector<Thread>(nthreads);
		actorsPrivateStates = new ConcurrentHashMap<String, PrivateState>();
		actorsIds = new LinkedList<String>();
		actorsQueues = new ConcurrentHashMap<String, ActionQueue>();
		vMonitor = new VersionMonitor();
		run = true;
		// Note: Creates new threads
		for (int i = 0; i < nthreads; i++) {
			Thread toAdd = new Thread(getProssecor());
			threadsPool.add(toAdd);
		}
	}

	public Map<String, PrivateState> getActors() {
		return actorsPrivateStates;
	}

	/**
	 * getter for actor's private state
	 * 
	 * @param actorId
	 *            actor's id
	 * @return actor's private state
	 */
	public PrivateState getPrivateState(String actorId) {
		return actorsPrivateStates.get(actorId);
	}

	/**
	 * Looping while searching not blocked queue
	 * 
	 * @return the runnable of the threads
	 */
	private Runnable getProssecor() {
		Runnable runnable = () -> {
			while (run) {

				findActionToFetch();

			}
		};
		return runnable;
	}
	/*
	 * Looping all the queues and try to fetch an action if not find an action to
	 * fetch - using await : start waiting until version number changes
	 */

	protected void findActionToFetch() {
		AtomicBoolean found = new AtomicBoolean(false);

		if (actorsIds.size() > 0) {

			for (int i = 0; i < actorsIds.size(); i++) {

				if (actorsQueues != null) {

					ActionQueue queue = actorsQueues.get(actorsIds.get(i));

					if (queue.size() > 0) {

						queue.fetchAction();

						found.compareAndSet(false, true);

					}

				}
			}
			if (!found.get()) {
				try {
					vMonitor.await(vMonitor.getVersion());
				} catch (InterruptedException e) {
				}

			}

		}

	}

	/**
	 * submits an action into an actor to be executed by a thread belongs to this
	 * thread pool
	 *
	 * @param action
	 *            the action to execute
	 * @param actorId
	 *            corresponding actor's id
	 * @param actorState
	 *            actor's private state (actor's information)
	 */
	public void submit(Action<?> action, String actorId, PrivateState actorState) {

		ActionQueue toAddQueue = new ActionQueue(actorId);

		// Adding the queue to the map, and the action to the queue
		if (actorsQueues.putIfAbsent(actorId, toAddQueue) == null) {
			toAddQueue.add(action);
		} else
			actorsQueues.get(actorId).add(action);
		vMonitor.inc();

		// Adding the PrivateState to the map, and the record to the PrivateState

		actorsPrivateStates.putIfAbsent(actorId, actorState);

		// Adding the actor ID to the list if not exist
		if (!actorsIds.contains(actorId)) {
			actorsIds.add(actorId);
		}

	}

	/**
	 * closes the thread pool - this method interrupts all the threads and waits for
	 * them to stop - it is returns *only* when there are no live threads in the
	 * queue.
	 *
	 * after calling this method - one should not use the queue anymore.
	 *
	 * @throws InterruptedException
	 *             if the thread that shut down the threads is interrupted
	 */
	public void shutdown() throws InterruptedException {
		run = false;
		for (int i = 0; i < threadsPool.size(); i++) {
			threadsPool.get(i).join();
		}

	}

	/**
	 * start the threads belongs to this thread pool
	 */
	public void start() {
		for (int i = 0; i < threadsPool.size(); i++) {
			threadsPool.get(i).start();
		}
	}
/*
 * Release a queue of specified actor.
 */
	protected void setQueueForFree(String actorId) {

		actorsQueues.get(actorId).setQueueForFree();

	}

	/**
	 * New protected class, a Queue of actions of some actor, this class **avoid**
	 * sync as expected. IMPORTANT: this class is *not* any kind of actor class, it
	 * extends Linkedlist, and just hold the actor holder id.
	 * 
	 */
	protected class ActionQueue extends ConcurrentLinkedQueue<Action<?>> {
		private String actorHolder; // Note: The holder actor
		private AtomicBoolean blocked; // Note: If the queue is blocked, meaning other thread fetching
		private Action<?> action = new Action<String>() {

			@Override
			protected void start() {

			}
		};

		protected ActionQueue(String actorHolder) {
			this.actorHolder = actorHolder;
			blocked = new AtomicBoolean(false);

		}

		/**
		 * Try to fetch action if the queue is not blocked, meaning other thread is
		 * fetching. We are using atomic boolean to avoid unnecessary sync.
		 * 
		 * @return if fetching succeeded
		 */
		protected Boolean fetchAction() {

			if (this.blocked.compareAndSet(false, true)) {

				action = poll();
				if (action != null) {

					action.handle(ActorThreadPool.this, actorHolder, actorsPrivateStates.get(actorHolder));

					this.blocked.set(false);
					vMonitor.inc();
					return true;
				}
				this.blocked.set(false);
			}
			return false;
		}

		protected void setQueueForFree() {
			this.blocked.set(false);
		}

	}

}
