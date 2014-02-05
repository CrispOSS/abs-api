package abs.api;

import java.net.URI;

/**
 * An extension of actor reference that allows an instance of
 * {@link Actor} to bind to a specific {@link Context}.
 * 
 * @see Context
 * @see EnvelopeContext
 * 
 * @author Behrooz Nobakht
 * @since 1.0
 */
interface ContextActor extends Actor {

	/**
	 * Binds this actor reference to the provided context.
	 *
	 * @see Context
	 * @see EnvelopeContext
	 * @param context
	 *            the context that should be bound for this actor
	 *            reference
	 */
	void bind(Context context);

	/**
	 * Creates an instance with the specific name and the provided
	 * context. No validation or checks are performed by this method
	 * in the creation.
	 *
	 * @param name
	 *            the name of the actor reference
	 * @param context
	 *            the initial bound context
	 * @return an instance of {@link abs.api.ContextActor}
	 */
	static ContextActor of(String name, Context context) {
		return new LocalContextActorReference(name, context);
	}

	/**
	 * An implementation of {@link ContextActor} that is suitable for
	 * a local context.
	 */
	static class LocalContextActorReference implements ContextActor {

		private static final long serialVersionUID = 5903306592703776997L;

		private final URI uri;
		private transient Context context;

		public LocalContextActorReference(String name, Context context) {
			final String u = name.startsWith("/") ? name : "/" + name;
			this.uri = name.startsWith(NS) ? URI.create(u) : URI.create(NS + u);
			this.context = context;
		}

		@Override
		public URI name() {
			return uri;
		}

		@Override
		public Context context() {
			return context;
		}

		@Override
		public final void bind(Context context) {
			this.context = context;
		}

		@Override
		public int hashCode() {
			return name().hashCode();
		}

	}

}
