package abs.api;

import java.io.Serializable;
import java.net.URI;

/**
 * A reference encapsulate a serializable and comparable
 * {@linkplain URI uri} with a name. Since {@link URI} is a direct
 * dependency in this interface, all restrictions and requirements are
 * applied to any implementation.
 * 
 * @author Behrooz Nobakht
 * @since 1.0
 */
public interface Reference extends Serializable, Comparable<Reference> {

	/**
	 * The name of this reference.
	 * 
	 * @return the name of this reference
	 */
	URI name();

	/**
	 * Compares this reference to another reference using
	 * {@link #name()} of the reference.
	 * 
	 * @return the result of the comparison of {@code this} with the
	 *         provided reference. The same semantics as default
	 *         {@link Comparable} holds here.
	 */
	@Override
	default int compareTo(Reference o) {
		return name().compareTo(o.name());
	}

	/**
	 * @param name
	 * @return
	 */
	static Reference from(final String name) {
		return new Reference() {

			private static final long serialVersionUID = 1L;

			private final URI uri = URI.create(name);

			@Override
			public URI name() {
				return uri;
			}

			@Override
			public String toString() {
				return uri.toASCIIString();
			}

			@Override
			public int hashCode() {
				return uri.hashCode();
			}
		};
	}
}
