package abs.api;

/**
 * @author Behrooz Nobakht
 * @since 1.0
 */
public interface Factory {

	/**
	 * @param fqcn
	 * @param ctorArguments
	 * @return
	 */
	Object create(String fqcn, String... ctorArguments);

	/**
	 * @param clazz
	 * @return
	 */
	boolean supports(Class<?> clazz);

}
