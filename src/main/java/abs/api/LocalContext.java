package abs.api;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

/**
 * A local context provides a default implementation of
 * {@link abs.api.Context} using a default
 * {@link abs.api.Configuration} or a provided configuration that
 * utilizes {@link java.util.ServiceLoader} to provision instances
 * based on the configuration classes.
 *
 * @see Context
 * @see Configuration
 * @author Behrooz Nobakht
 * @since 1.0
 */
public class LocalContext implements Context {

	private final Configuration configuration;
	private Router router;
	private Opener opener;
	private Inbox inbox;
	private Notary notary;
	private ExecutorService executor;

	/**
	 * <p>
	 * Constructor for LocalContext.
	 * </p>
	 */
	public LocalContext() {
		this(Configuration.newConfiguration().build());
	}

	/**
	 * <p>
	 * Constructor for LocalContext.
	 * </p>
	 *
	 * @param configuration
	 *            a {@link abs.api.Configuration} object.
	 */
	public LocalContext(Configuration configuration) {
		this.configuration = configuration;
		try {
			initialize();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/** {@inheritDoc} */
	@PostConstruct
	@Override
	public void initialize() throws Exception {
		this.executor = Executors.newWorkStealingPool();

		ServiceLoader<Router> routerLoader = ServiceLoader.load(Router.class);
		for (Iterator<Router> it = routerLoader.iterator(); it.hasNext();) {
			Router router = it.next();
			if (router.getClass() == configuration.getRouter()
					|| configuration.getRouter().isAssignableFrom(router.getClass())) {
				this.router = router;
				break;
			}
		}
		if (this.router == null) {
			this.router = new LocalRouter(this);
		}

		ServiceLoader<Opener> openerLoader = ServiceLoader.load(Opener.class);
		for (Iterator<Opener> it = openerLoader.iterator(); it.hasNext();) {
			Opener opener = it.next();
			if (opener.getClass() == configuration.getOpener()
					|| configuration.getOpener().isAssignableFrom(opener.getClass())) {
				this.opener = opener;
				break;
			}
		}
		if (this.opener == null) {
			this.opener = new DefaultOpener();
		}

		ServiceLoader<Inbox> inboxlLoader = ServiceLoader.load(Inbox.class);
		for (Iterator<Inbox> it = inboxlLoader.iterator(); it.hasNext();) {
			Inbox inbox = it.next();
			if (inbox.getClass() == configuration.getInbox()
					|| configuration.getInbox().isAssignableFrom(inbox.getClass())) {
				this.inbox = inbox;
				break;
			}
		}
		if (this.inbox == null) {
			this.inbox = new DispatchInbox(this, executor);
		}

		ServiceLoader<Notary> notaryLoader = ServiceLoader.load(Notary.class);
		for (Iterator<Notary> it = notaryLoader.iterator(); it.hasNext();) {
			Notary notary = it.next();
			if (notary.getClass() == configuration.getNotary()
					|| configuration.getNotary().isAssignableFrom(notary.getClass())) {
				this.notary = notary;
				break;
			}
		}
		if (this.notary == null) {
			this.notary = new LocalNotary();
		}
	}

	/** {@inheritDoc} */
	@Override
	public Actor newReference(String name, Object object) {
		try {
			Actor ref = ContextActor.of(name, this);
			notary.add(ref, object);
			return ref;
		} catch (RuntimeException e) {
			throw e;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Notary notary() {
		return notary;
	}

	/** {@inheritDoc} */
	@Override
	public Router router() {
		return router;
	}

	/** {@inheritDoc} */
	@Override
	public Opener opener(Reference reference) {
		return opener;
	}

	/** {@inheritDoc} */
	@Override
	public Inbox inbox(Reference reference) {
		return inbox;
	}

	/** {@inheritDoc} */
	@Override
	public void stop() throws Exception {
		notary.stop();
		try {
			executor.shutdownNow();
		} catch (Exception e) {
		}
	}

}
