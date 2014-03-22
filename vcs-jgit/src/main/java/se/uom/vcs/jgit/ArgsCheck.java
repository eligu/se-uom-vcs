/**
 * 
 */
package se.uom.vcs.jgit;

import java.text.MessageFormat;
import java.util.Collection;

/**
 * A helper class that contains various method to check parameters given to a method.<p>
 * 
 * @author Elvis Ligu
 *
 */
public class ArgsCheck {

	/**
	 * Check if the given arg is null and throw an {@link IllegalArgumentException} if so.<p>
	 * 
	 * The message format used here is <pre>{o} must not be null</pre>
	 * 
	 * @param 
	 * 		name argument name
	 * @param 
	 * 		arg to check
	 */
	public static void notNull(final String name, final Object arg) {
		if(arg == null) {
			throw new IllegalArgumentException(
					MessageFormat.format("{0} must not be null", name));
		}
	}

	/**
	 * Check the given args array if it contains more than one null objects.<p>
	 * 
	 * Throw an {@link IllegalArgumentException} if the condition is not fulfilled.
	 * Exception message will be: <pre>only one parameter must be null</pre>
	 * 
	 * @param 
	 * 		args the array to check for null		
	 */
	public static void onlyOneNull(final Object... args) {
		int sum = 0;
		for(final Object o : args) {
			if(o == null) {
				sum++;
				if(sum == 2) {
					throw new IllegalArgumentException("only one parameter must be null");
				}
			}
		}
	}

	/**
	 * Check the given args array if it contains any null object.<p>
	 * 
	 * Throw an {@link IllegalArgumentException} if the condition is not fulfilled.
	 * The args argument will be checked for null using {@link #notNull(String, Object)}.
	 * Exception message format will be: <pre>"{0} must not contain null"</pre>
	 * 
	 * @param
	 * 		name argument name
	 * @param 
	 * 		args the array to check for null		
	 */
	public static void containsNoNull(final String name, final Object... args) {
		notNull(name, args);
		for(final Object o : args) {
			if(o == null) {
				throw new IllegalArgumentException(
						MessageFormat.format("{0} must not contain null", name));
			}
		}
	}

	/**
	 * Check the given args collection if it contains any null object.<p>
	 * 
	 * Throw an {@link IllegalArgumentException} if the condition is not fulfilled.
	 * The args argument will be checked for null using {@link #notNull(String, Object)}.
	 * Exception message format will be: <pre>"{0} must not contain null"</pre>
	 * 
	 * @param
	 * 		name argument name
	 * @param 
	 * 		args the array to check for null		
	 */
	public static void containsNoNull(final String name, final Collection<?> args) {
		notNull(name, args);
		for(final Object o : args) {
			if(o == null) {
				throw new IllegalArgumentException(
						MessageFormat.format("{0} must not contain null", name));
			}
		}
	}

	/**
	 * Check the given args collection if it is empty.<p>
	 * 
	 * Throw an {@link IllegalArgumentException} if the condition is not fulfilled.
	 * The args argument will be checked for null using {@link #notNull(String, Object)}.
	 * Exception message format will be: <pre>"{0} must not be empty"</pre>
	 * 
	 * @param
	 * 		name argument name
	 * @param 
	 * 		args the array to check for null		
	 */
	public static void notEmpty(final String name, final Collection<?> args) {
		notNull(name, args);

		if(args.isEmpty()) {
			throw new IllegalArgumentException(
					MessageFormat.format("{0} must not be empty", name));
		}
	}

	/**
	 * Check the given string arg if it is empty.<p>
	 * 
	 * Throw an {@link IllegalArgumentException} if the condition is not fulfilled.
	 * The args argument will be checked for null using {@link #notNull(String, Object)}.
	 * The <code>arg</code> will be trimmed first.
	 * Exception message format will be: <pre>"{0} must not be empty"</pre>
	 * 
	 * @param 
	 * 		name argument name
	 * @param 
	 * 		arg the string to check if it is empty
	 */
	public static void notEmpty(final String name, final String arg) {
		notNull(name, arg);
		if(arg.trim().isEmpty()) {
			throw new IllegalArgumentException(
					MessageFormat.format("{0} must not be empty", name));
		}
	}

	/**
	 * Check if the given condition is false and throw an {@link IllegalArgumentException} if so.<p>
	 * 
	 * The message format used here is <pre>{o} must be true</pre>
	 * 
	 * @param 
	 * 		name argument name
	 * @param 
	 * 		arg to check
	 */
	public static void isTrue(final String name, final boolean condition) {
		if(!condition) {
			throw new IllegalArgumentException(
					MessageFormat.format("{0} must be true", name));
		}
	}

	/**
	 * Check if the given arg class is the same as or a subtype of clazz argument.<p>
	 * 
	 * This will check for null to by calling {@link #notNull(String, Object)}.
	 * 
	 * @param 
	 * 		name argument name
	 * @param 
	 * 		clazz class to check the argument
	 * @param 
	 * 		arg to check
	 */
	public static void isSubtype(final String name, final Class<?> clazz, final Object arg) {
		notNull(name, arg);
		if(!clazz.isAssignableFrom(arg.getClass())) {
			throw new IllegalArgumentException(
					MessageFormat.format("{0} is not a {1} instance", name, clazz.getName()));
		}
	}
}
