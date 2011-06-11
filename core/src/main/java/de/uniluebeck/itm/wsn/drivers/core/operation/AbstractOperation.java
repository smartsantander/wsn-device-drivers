package de.uniluebeck.itm.wsn.drivers.core.operation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;

import de.uniluebeck.itm.wsn.drivers.core.State;
import de.uniluebeck.itm.wsn.drivers.core.async.AsyncCallback;
import de.uniluebeck.itm.wsn.drivers.core.event.StateChangedEvent;
import de.uniluebeck.itm.wsn.drivers.core.exception.TimeoutException;

/**
 * An abstract operation.
 * If no other timeout is set the operation will be canceled automatically after the <code>DEFAULT_TIMEOUT</code>.
 * The result of a timed out operation is null also when the operation completed at the same time.
 * 
 * @author Malte Legenhausen
 *
 * @param <T> The return type of the operation.
 */
public abstract class AbstractOperation<T> implements Operation<T> {
	
	/**
	 * Default timeout is set to 5 minutes.
	 */
	public static final long DEFAULT_TIMEOUT = 30000;
	
	/**
	 * Logger for this class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(Operation.class);
	
	/**
	 * Sub <code>Operation</code> that is currently running.
	 */
	private Operation<?> subOperation;
	
	/**
	 * Listeners for <code>OperationContainer</code> changed.
	 */
	private final List<OperationListener<T>> listeners = new ArrayList<OperationListener<T>>();
	
	/**
	 * The timeout after which the application will be canceled.
	 */
	private long timeout = DEFAULT_TIMEOUT;
	
	/**
	 * The callback that is called when the operation has finished, canceled or when an exception occured.
	 */
	private AsyncCallback<T> callback;
	
	/**
	 * The current state of the <code>Operation</code>.
	 */
	private State state = State.WAITING;
	
	/**
	 * Boolean thats stores if the operatio has to be canceled.
	 */
	private boolean canceled;
	
	private final TimeLimiter timeLimiter;
	
	/**
	 * Constructor.
	 */
	public AbstractOperation() {
		this(new SimpleTimeLimiter());
	}
	
	public AbstractOperation(TimeLimiter timeLimiter) {
		this.timeLimiter = timeLimiter;
	}
	
	@Override
	public void setAsyncCallback(final AsyncCallback<T> aCallback) {
		callback = aCallback;
	}
	
	@Override
	public T call() throws Exception {
		setState(State.RUNNING);
		
		callback.onExecute();
		T result = null;
		try {
			// Cancel execution if operation was canceled before operation changed to running.
			if (!canceled) {
				result = executeOperation();
			}
		} catch (UncheckedTimeoutException e) {
			setState(State.TIMEDOUT);
			LOG.error("Timeout reached during operation execution", e);
			TimeoutException timeoutException = new TimeoutException("Operation timeout " + timeout + "ms reached.");
			callback.onFailure(timeoutException);
			throw timeoutException;
		} catch (Exception e) {
			setState(State.EXCEPTED);
			LOG.error("Exception during operation execution", e);
			callback.onFailure(e);
			throw e;
		}	
		
		if (canceled) {
			setState(State.CANCELED);
			callback.onCancel();
			result = null;
		} else {
			setState(State.DONE);
			callback.onSuccess(result);
		}
		return result;
	}
	
	private T executeOperation() throws Exception {
		Operation<T> operation = timeLimiter.newProxy(this, Operation.class, timeout, TimeUnit.MILLISECONDS);
		final ProgressManager progressManager = new RootProgressManager(callback);
		progressManager.worked(0.0f);
		T result = operation.execute(progressManager);
		progressManager.done();
		return result;
	}
	
	/**
	 * Call this method when another <code>Operation</code> has to be executed while this <code>Operation</code>.
	 * 
	 * @param <R> The return type of the sub <code>Operation</code>.
	 * @param operation The sub <code>Operation</code> that has to be executed.
	 * @param progressManager The progress manager for observing the progress.
	 * @return The result of the sub <code>Operation</code>.
	 * @throws Exception Any exception throws be the operation.
	 */
	protected <R> R executeSubOperation(Operation<R> operation, ProgressManager progressManager) throws Exception {
		subOperation = operation;
		final R result = operation.execute(progressManager);
		progressManager.done();
		subOperation = null;
		return result;
	}
	
	/**
	 * Thread safe state change function.
	 * 
	 * @param newState The new State of this operation.
	 */
	private void setState(final State newState) {
		synchronized (state) {
			final State oldState = state;
			state = newState;
			fireStateChangedEvent(new StateChangedEvent<T>(this, oldState, newState));
		}
	}
	
	/**
	 * Notify all listeners that the state has changed.
	 * 
	 * @param event The state change event.
	 */
	private void fireStateChangedEvent(final StateChangedEvent<T> event) {
		String msg = "Operation state of {} changed from {} to {}";
		LOG.trace(msg, new Object[] {this.getClass().getName(), event.getOldState(), event.getNewState()});
		for (OperationListener<T> listener : listeners.toArray(new OperationListener[0])) {
			listener.onStateChanged(event);
		}
	}

	@Override
	public State getState() {
		return state;
	}
	
	/**
	 * Method will throw an <code>IllegalStateException</code> when 
	 * trying to change the timeout when the operation is in running state.
	 * 
	 * @param timeout The timeout of the operation.
	 */
	@Override
	public void setTimeout(final long timeout) {
		checkArgument(timeout >= 0, "Negativ timeout is not allowed");
		checkState(!State.RUNNING.equals(state), "Timeout can not be set when operation is in running state");
		this.timeout = timeout;
	}

	@Override
	public long getTimeout() {
		return timeout;
	}
	
	@Override
	public void addListener(final OperationListener<T> listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeOperationListener(final OperationListener<T> listener) {
		listeners.remove(listener);
	}

	@Override
	public void cancel() {
		if (subOperation != null) {
			subOperation.cancel();
		}
		canceled = true;
	}
	
	@Override
	public boolean isCanceled() {
		return canceled;
	}
}
