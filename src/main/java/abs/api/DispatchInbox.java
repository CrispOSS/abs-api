package abs.api;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * A dispatch inbox maintains an in-memory mapping of separate inboxes
 * for each recipient. It delegates posting of an envelope to the
 * proper inbox and creates them on-the-fly.
 *
 * @author Behrooz Nobakht
 * @since 1.0
 */
public class DispatchInbox extends AbstractInbox {

	private final ConcurrentMap<Reference, Inbox> inboxes;
	private final ExecutorService executor;

	/**
	 * <p>
	 * Constructor for DispatchInbox.
	 * </p>
	 *
	 * @param context
	 *            a {@link abs.api.Context} object.
	 * @param executor
	 *            a {@link java.util.concurrent.ExecutorService}
	 *            object.
	 */
	public DispatchInbox(Context context, ExecutorService executor) {
		super(context);
		this.executor = executor;
		this.inboxes = new ConcurrentHashMap<>(8192);
	}

	@Override
	public <V> Future<V> post(Envelope envelope, Object receiver) {
		Inbox inbox = getInbox(envelope.to());
		inbox.post(envelope, receiver);
		return envelope.response();
	}

	/**
	 * <p>
	 * getInbox.
	 * </p>
	 *
	 * @param owner
	 *            a {@link abs.api.Reference} object.
	 * @return a {@link abs.api.Inbox} object.
	 */
	protected Inbox getInbox(Reference owner) {
		Inbox inbox = inboxes.get(owner);
		if (inbox != null) {
			return inbox;
		}
		inbox = new QueueInbox(context, getExecutor());
		inboxes.put(owner, inbox);
		return inbox;
	}

	/**
	 * <p>
	 * Getter for the field <code>executor</code>.
	 * </p>
	 *
	 * @return a {@link java.util.concurrent.ExecutorService} object.
	 */
	protected ExecutorService getExecutor() {
		return executor;
	}

}
