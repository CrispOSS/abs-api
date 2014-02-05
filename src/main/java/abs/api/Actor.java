package abs.api;

import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * An actor reference is a reference that exposes a set of methods to
 * send messages to another actor reference. There are a number of
 * ways that a message would have meaning as an executable entity in
 * this implementation:
 * <ul>
 * <li>a method invocation exposed by
 * {@link #invoke(Actor, String, Object...)}
 * <li>an instance of {@link Runnable} or {@link Callable} exposed by
 * {@link #ask(Actor, Object)}
 * <li>the recipient of the message is an instance of {@link Behavior}
 * which leads to running {@link Behavior#respond(Object)}
 * </ul>
 * 
 * <p>
 * Every actor reference is registered with an instance of
 * {@link Context}. A gathers different layers of the actor system to
 * be used by any actor reference such as routing or executing
 * messages.
 * 
 * <p>
 * This interface in this version exposes methods as {@code ask} which
 * allows to capture the result of the message into a future value.
 * However, to have the model of {@code tell} in actor (fire and
 * forget), simply the result of the message can be ignored.
 * 
 * @see Reference
 * @see MethodReference
 * 
 * @author Behrooz Nobakht
 * @since 1.0
 */
public interface Actor extends Reference, Comparable<Reference> {

	/**
	 * The prefix for all actor references created in a context.
	 */
	String NS = "abs://";

	/**
	 * NOBODY refers to any recipient that is not recognized by its
	 * {@link #name()} in the system.
	 */
	Actor NOBODY = new Actor() {
		private static final long serialVersionUID = 6203481166223651274L;

		private final URI name = URI.create(NS + "NOBODY");

		@Override
		public URI name() {
			return name;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj == this) {
				return true;
			}
			if (obj instanceof Reference == false) {
				return false;
			}
			return name.equals(((Reference) obj).name());
		};
	};

	/**
	 * A default implementation of {@link Reference#name()} that uses
	 * {@link #NOBODY}.
	 * 
	 * @return the nobody name by default.
	 */
	@Override
	default URI name() {
		return NOBODY.name();
	}

	/**
	 * Provides the context of this actor reference. By default, it
	 * uses the context from {@link SystemContext} which is expected
	 * to be initialized in the beginning of an application using this
	 * API.
	 * 
	 * @see SystemContext
	 * @return the context to which this actor reference is registered
	 *         with.
	 */
	default Context context() {
		return SystemContext.context;
	}

	/**
	 * Sends a general message to a recipient and captures the result
	 * into an instance of {@link Future}.
	 * 
	 * @see Context
	 * @see Router
	 * 
	 * @param <V>
	 *            the type of the result expected from the future
	 *            value
	 * @param to
	 *            the receiver of the message
	 * @param message
	 *            the message to be sent to the receiver
	 * @return a future value to capture the result of processing the
	 *         message. The future value may throw exception is
	 *         {@link Future#get()} is used as a result of either
	 *         failure in processing the message or actually the
	 *         processing of the message decided to fail the message
	 *         result. The user of the future value may inspect into
	 *         causes of the exception to identify the reasons.
	 */
	default <V> Future<V> ask(Actor to, Object message) {
		final Actor receiver = NOBODY.equals(to) ? (Actor) context().reference(to) : to;
		final Envelope envelope = new SimpleEnvelope((Reference) this, receiver, message);
		context().router().route(envelope);
		return envelope.response();
	}

	/**
	 * A convenient method to ask a method invocation from this actor
	 * reference. Delegates to
	 * {@link #invoke(Actor, String, Object...)} with {@code this}
	 * parameter.
	 * 
	 * @param <V>
	 *            the type of the result expected from the future
	 *            value
	 * @param method
	 *            the name of the method to be invoked
	 * @param args
	 *            the actual parameters of the method
	 * @return a future value to capture the result (see
	 *         {@link #ask(Actor, Object)})
	 */
	default <V> Future<V> ask(String method, Object... args) {
		return invoke(this, method, args);
	}

	/**
	 * Sends a message to the recipient to invoke a specific method
	 * with specific arguments expected in the receiver object.
	 * Delegates to {@link #ask(Actor, Object)} with an instance of
	 * {@link MethodReference} by default.
	 * 
	 * @param <V>
	 *            the type of the result expected from the future
	 *            value
	 * @param to
	 *            the receiver of the message to invoke the method
	 * @param method
	 *            the name of the method to be invoked
	 * @param args
	 *            the actual parameters of method to execute
	 * @return a future value to capture the result (see
	 *         {@link #ask(Actor, Object)})
	 */
	default <V> Future<V> invoke(Actor to, String method, Object... args) {
		final MethodReference message = MethodReference.of(to, method, args);
		return ask(to, message);
	}

	/**
	 * Provides the sender of the <i>current</i> message that is being
	 * invoked/processed by the receiver object.
	 * 
	 * @see Context
	 * @see ContextActor
	 * @see EnvelopeContext
	 * 
	 * @return the sender of the current message or {@link #NOBODY} if
	 *         there is no sender for this message
	 */
	default Actor sender() {
		try {
			if (!NOBODY.equals(this)) {
				Context context = context();
				if (context != null && context instanceof EnvelopeContext) {
					return ((EnvelopeContext) context).sender();
				}
			} else {
				Reference ref = context().reference(this);
				if (ref instanceof ContextActor) {
					ContextActor caref = (ContextActor) ref;
					Context context = caref.context();
					if (context != null || context instanceof EnvelopeContext) {
						return ((EnvelopeContext) context).sender();
					}
				}
			}
		} catch (Exception e) {
			// Ignore
		}
		return NOBODY;
	}

	/**
	 * The implementation is not different from
	 * {@link Reference#compareTo(Reference)}.
	 * 
	 * @param o
	 *            the reference to compare to
	 * @return the default semantics specified by {@link Comparable}
	 */
	@Override
	default int compareTo(Reference o) {
		return name().compareTo(o.name());
	}

}