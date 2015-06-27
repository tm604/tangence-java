package org.tangence.java;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hand-rolled attempt at a Future/PromisesA implementation.
 */
public class Future<T> {
	private static final Logger log = LoggerFactory.getLogger("Future");

	/**
	 * Represents the callback(s) that we should run when this Future
	 * is ready.
	 */
	public interface Task<T> {
		/** This will be called with the relevant Future when it's marked
		 * as ready - this will always be called regardless of Future status.
		 * @param f Future
		 * @throws TangenceException
		 */
		public void onReady(final Future<T> f) throws TangenceException;
		/**
		 * This is called if the Future is cancelled.
		 * @param f Future
		 * @throws TangenceException
		 */
		public void onCancel(final Future<T> f) throws TangenceException;
		/**
		 * This will be called if the Future is marked as done (successful)
		 * @param param Object[]
		 * @throws TangenceException
		 */
		public void onDone(final T param) throws TangenceException;
		/**
		 * This will be called on failure with the Throwable as a parameter.
		 * @param e Throwable
		 * @throws TangenceException
		 */
		public void onFail(final Throwable e) throws TangenceException;
	}

	/**
	 * Shortcut with everything set to no-op handler
	 */
	public static class DefaultTask<T> implements Task<T> {
		/**
		 * Method onReady.
		 * @param f Future
		 * @throws TangenceException
		 * @see org.tangence.java.Future$Task#onReady(Future)
		 */
		public void onReady(final Future f) throws TangenceException { }
		/**
		 * Method onCancel.
		 * @param f Future
		 * @throws TangenceException
		 * @see org.tangence.java.Future$Task#onCancel(Future)
		 */
		public void onCancel(final Future f) throws TangenceException { }
		/**
		 * Method onDone.
		 * @param param Object[]
		 * @throws TangenceException
		 * @see org.tangence.java.Future$Task#onDone(Object[])
		 */
		public void onDone(final T param) throws TangenceException { }
		/**
		 * Method onFail.
		 * @param e Throwable
		 * @throws TangenceException
		 * @see org.tangence.java.Future$Task#onFail(Throwable)
		 */
		public void onFail(final Throwable e) throws TangenceException { }
	}

	/**
	 * Used for then, else etc.
	 */
	public interface Sequence {
		/**
		 * Method run.
		 * @param f Future
		 * @return Future
		 */
		public Future run(final Future f);
	}

	private List<Task> tasks = new ArrayList<Task>();

	private T result;
	private Throwable failure = null;

	private boolean ready = false;
	private boolean cancelled = false;
	private boolean failed = false;

	/** Returns true when this Future is completed (actual
	 * status can be done/failed/cancelled)
	 * @return boolean
	 */
	public boolean isReady() { return ready; }

	/**
	 * Returns a Future representing the combination of sub-Futures.
	 * @param pending Future[]
	 * @return Future
	 */
	public static <T> Future<List<Object>> needsAll(final Future<?> ... pending) throws TangenceException {
		final Future<List<Object>> parent = new Future<List<Object>>();
		final Set<Future<?>> expected = new HashSet<Future<?>>();
		for(final Future<?> f : pending) {
			expected.add(f);
			f.add(new DefaultTask() {
				@Override
				public void onReady(final Future f2) throws TangenceException {
					expected.remove(f2);
					if(expected.isEmpty()) {
						final List<Object> rslt = new ArrayList<Object>();
						for(final Future r : pending) {
							rslt.add(r.get());
						}
						parent.done(rslt);
					}
				}
			});
		}
		return parent;
	}

	/**
	 * Method get.
	 * @return Object[]
	 */
	public T get() { return result; }

	/**
	 * Method add.
	 * @param t Task
	 * @return Future
	 */
	public Future add(final Task t) throws TangenceException {
		if(isReady()) {
			complete(t);
		} else {
			tasks.add(t);
		}
		return this;
	}

/*
	public Future then(final Future f) {
		final Future parent = this;
		tasks.add(new Task() {
			public void onCancel(final Future f) {
				f.cancel(parent);
			}
			public void onDone(final T param) {
				f.done(param);
			}
			public void onFail(final T param) {
				f.fail(param);
			}
		});
		return this;
	}

	public Future then(final Future f) {
		final Future parent = this;
		tasks.add(new Task() {
			public void onCancel(final Future f) {
				f.cancel(parent);
			}
			public void onDone(final T param) {
				f.done(param);
			}
			public void onFail(final T param) {
				f.fail(param);
			}
		});
		return this;
	}
*/
	/**
	 * Method done.
	 * @param param Object[]
	 * @return Future
	 * @throws TangenceException
	 */
	public Future done(final T param) throws TangenceException {
		if(ready) {
			throw new TangenceException("Cannot mark as ->done, this Future is ready: " + this.toString());
		}

		result = param;
		ready = true;

		log.debug(String.format("Processing %d tasks for %s", tasks.size(), this));
		for(final Task t : tasks) {
			complete(t);
		}
		return this;
	}

	private void complete(final Task t) throws TangenceException {
		log.debug(String.format("* Task %s", t));
		t.onReady(this);
		t.onDone(result);
		log.debug(String.format("* Task %s complete", t));
	}

	/**
	 * Method fail.
	 * @param e Throwable
	 * @return Future
	 * @throws TangenceException
	 */
	public Future fail(final Throwable e) throws TangenceException {
		if(ready) {
			throw new TangenceException("Cannot mark as ->fail, this Future is ready: " + this.toString() + " (exception " + e.getMessage() + ")");
		}
		failure = e;
		ready = true;
		failed = true;

		for(final Task t : tasks) {
			t.onReady(this);
			t.onFail(e);
		}
		return this;
	}

	/**
	 * Method cancel.
	 * @return Future
	 * @throws TangenceException
	 */
	public Future cancel() throws TangenceException {
		if(ready) {
			throw new TangenceException("Cannot mark as ->cancel, this Future is ready: " + this.toString());
		}
		ready = true;
		cancelled = true;

		for(final Task t : tasks) {
			t.onReady(this);
			t.onCancel(this);
		}
		return this;
	}

	public static class Transformation<T> {
		public T done(final T param) { return param; }
		public Throwable fail(final Throwable param) { return param; }
	}

	/**
	 * Given a source future, returns a new Future which will complete
	 * at the same time the source future does, but with a transformation
	 * applied to success and/or failure cases.
	 */
	public Future<T> transform(final Transformation<T> tx) throws TangenceException {
		final Future<T> target = new Future<T>();
		this.add(new Task<T>() {
			@Override
			public void onReady(final Future<T> f) throws TangenceException { }

			@Override
			public void onCancel(final Future<T> f) throws TangenceException {
				target.cancel();
			}

			@Override
			public void onDone(final T param) throws TangenceException {
				target.done(tx.done(param));
			}

			@Override
			public void onFail(final Throwable e) throws TangenceException {
				target.fail(tx.fail(e));
			}
		});
		return target;
	}
}

