/**
 * 
 */
package gr.uom.se.util.validation;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;

/**
 * A helper class that contains various method to check parameters given to a
 * method.
 * <p>
 * 
 * @author Elvis Ligu
 */
public class ArgsCheck {

   /**
    * Check if the given arg is null and throw an
    * {@link IllegalArgumentException} if so.
    * <p>
    * 
    * The message format used here is
    * 
    * <pre>
    * {o} must not be null
    * </pre>
    * 
    * @param name
    *           argument name
    * @param arg
    *           to check
    */
   public static void notNull(final String name, final Object arg) {
      if (arg == null) {
         throw new IllegalArgumentException(MessageFormat.format(
               "{0} must not be null", name));
      }
   }

   /**
    * Check if the given arg1 is greater than the given arg2 and throw an
    * {@link IllegalArgumentException} if not.
    * <p>
    * The arguments will be checked for null first using
    * {@link #notNull(String, Object)} method.
    * <p>
    * The message format used here is
    * <p>
    * <code>
    * {0} must be greater than {1}
    * </code>
    * <p>
    * where <code>{1}</code> is the <code>arg1Name</code> and <code>{1}</code>
    * is the <code>arg2Name</code>.
    * <p>
    * 
    * @param arg1Name
    *           the name of the arg1 as a string
    * @param arg2Name
    *           the name if the arg2 as a string
    * @param arg1
    *           the first argument
    * @param arg2
    *           the second argument
    */
   public static <T extends Comparable<T>> void greaterThan(
         final String arg1Name, final String arg2Name, final T arg1,
         final T arg2) {
      comparableCheck(arg1Name, arg2Name, arg1, arg2,
            "{0} must be greater than {1}", 1);
   }

   private static <T extends Comparable<T>> void comparableCheck(
         final String arg1Name, final String arg2Name, final T arg1,
         final T arg2, String compMsg, int comp) {

      notNull(arg1Name, arg1);
      notNull(arg2Name, arg2);
      if (comp == 0) {
         if (!eq(arg1, arg2)) {
            throw new IllegalArgumentException(MessageFormat.format(compMsg,
                  arg1Name, arg2Name));
         }
      } else if (comp == -1) {
         if (!lt(arg1, arg2)) {
            throw new IllegalArgumentException(MessageFormat.format(compMsg,
                  arg1Name, arg2Name));
         }
      } else if (comp == 1) {
         if (!gt(arg1, arg2)) {
            throw new IllegalArgumentException(MessageFormat.format(compMsg,
                  arg1Name, arg2Name));
         }
      } else if (comp == 10) {
         if (!(gt(arg1, arg2) || eq(arg1, arg2))) {
            throw new IllegalArgumentException(MessageFormat.format(compMsg,
                  arg1Name, arg2Name));
         }
      } else if (comp == -10) {
         if (!(lt(arg1, arg2) || eq(arg1, arg2))) {
            throw new IllegalArgumentException(MessageFormat.format(compMsg,
                  arg1Name, arg2Name));
         }
      }
   }

   private static <T extends Comparable<T>> boolean gt(final T arg1,
         final T arg2) {
      return arg1.compareTo(arg2) > 0;
   }

   private static <T extends Comparable<T>> boolean lt(final T arg1,
         final T arg2) {
      return arg1.compareTo(arg2) < 0;
   }

   private static <T extends Comparable<T>> boolean eq(final T arg1,
         final T arg2) {
      return arg1.compareTo(arg2) == 0;
   }

   /**
    * Check if the given arg1 is less than the given arg2 and throw an
    * {@link IllegalArgumentException} if not.
    * <p>
    * The arguments will be checked for null first using
    * {@link #notNull(String, Object)} method.
    * <p>
    * The message format used here is
    * <p>
    * <code>
    * {0} must be less than {1}
    * </code>
    * <p>
    * where <code>{1}</code> is the <code>arg1Name</code> and <code>{1}</code>
    * is the <code>arg2Name</code>.
    * <p>
    * 
    * @param arg1Name
    *           the name of the arg1 as a string
    * @param arg2Name
    *           the name if the arg2 as a string
    * @param arg1
    *           the first argument
    * @param arg2
    *           the second argument
    */
   public static <T extends Comparable<T>> void lessThan(final String arg1Name,
         final String arg2Name, final T arg1, final T arg2) {
      comparableCheck(arg1Name, arg2Name, arg1, arg2,
            "{0} must be less than {1}", -1);
   }

   /**
    * Check if the given arg1 is equal to the given arg2 and throw an
    * {@link IllegalArgumentException} if not.
    * <p>
    * The arguments will be checked for null first using
    * {@link #notNull(String, Object)} method. Note that this is used for
    * natural ordering and not for true equality, that is, the
    * {@link Comparable#compareTo(Object)} will be used to determine equality.
    * <p>
    * The message format used here is
    * <p>
    * <code>
    * {0} must be equal to {1}
    * </code>
    * <p>
    * where <code>{1}</code> is the <code>arg1Name</code> and <code>{1}</code>
    * is the <code>arg2Name</code>.
    * <p>
    * 
    * @param arg1Name
    *           the name of the arg1 as a string
    * @param arg2Name
    *           the name if the arg2 as a string
    * @param arg1
    *           the first argument
    * @param arg2
    *           the second argument
    */
   public static <T extends Comparable<T>> void equals(final String arg1Name,
         final String arg2Name, final T arg1, final T arg2) {
      comparableCheck(arg1Name, arg2Name, arg1, arg2,
            "{0} must be equal to {1}", 0);
   }

   /**
    * Check if the given arg1 is greater than or equal to the given arg2 and
    * throw an {@link IllegalArgumentException} if not.
    * <p>
    * The arguments will be checked for null first using
    * {@link #notNull(String, Object)} method.
    * <p>
    * The message format used here is
    * <p>
    * <code>
    * {0} must be greater than or equal to {1}
    * </code>
    * <p>
    * where <code>{1}</code> is the <code>arg1Name</code> and <code>{1}</code>
    * is the <code>arg2Name</code>.
    * <p>
    * 
    * @param arg1Name
    *           the name of the arg1 as a string
    * @param arg2Name
    *           the name if the arg2 as a string
    * @param arg1
    *           the first argument
    * @param arg2
    *           the second argument
    */
   public static <T extends Comparable<T>> void greaterThanOrEqual(
         final String arg1Name, final String arg2Name, final T arg1,
         final T arg2) {
      comparableCheck(arg1Name, arg2Name, arg1, arg2,
            "{0} must be greater than or equal to {1}", 10);
   }
   
   /**
    * Check if the given arg1 is less than or equal to the given arg2 and
    * throw an {@link IllegalArgumentException} if not.
    * <p>
    * The arguments will be checked for null first using
    * {@link #notNull(String, Object)} method.
    * <p>
    * The message format used here is
    * <p>
    * <code>
    * {0} must be less than or equal to {1}
    * </code>
    * <p>
    * where <code>{1}</code> is the <code>arg1Name</code> and <code>{1}</code>
    * is the <code>arg2Name</code>.
    * <p>
    * 
    * @param arg1Name
    *           the name of the arg1 as a string
    * @param arg2Name
    *           the name if the arg2 as a string
    * @param arg1
    *           the first argument
    * @param arg2
    *           the second argument
    */
   public static <T extends Comparable<T>> void lessThanOrEqual(
         final String arg1Name, final String arg2Name, final T arg1,
         final T arg2) {
      comparableCheck(arg1Name, arg2Name, arg1, arg2,
            "{0} must be less than or equal to {1}", -10);
   }

   /**
    * Check the given args array if it contains more than one null objects.
    * <p>
    * 
    * Throw an {@link IllegalArgumentException} if the condition is not
    * fulfilled. Exception message will be:
    * 
    * <pre>
    * only one parameter may be null
    * </pre>
    * 
    * @param args
    *           the array to check for null
    */
   public static void atMostOneNull(final Object... args) {

      if (!ContainmentCheck.atMostOneNull(args)) {
         throw new IllegalArgumentException("only one parameter may be null");
      }
   }

   /**
    * Check the given args array if it contains any null object.
    * <p>
    * 
    * Throw an {@link IllegalArgumentException} if the condition is not
    * fulfilled. The args argument will be checked for null using
    * {@link #notNull(String, Object)}. Exception message format will be:
    * 
    * <pre>
    * &quot;{0} must not contain null&quot;
    * </pre>
    * 
    * @param name
    *           argument name
    * @param args
    *           the array to check for null
    */
   public static void containsNoNull(final String name, final Object... args) {
      notNull(name, args);

      if (!ContainmentCheck.containsNoNull(args)) {
         throw new IllegalArgumentException(MessageFormat.format(
               "{0} must not contain null", name));
      }
   }

   /**
    * Check the given args collection if it contains any null object.
    * <p>
    * 
    * Throw an {@link IllegalArgumentException} if the condition is not
    * fulfilled. The args argument will be checked for null using
    * {@link #notNull(String, Object)}. Exception message format will be:
    * 
    * <pre>
    * &quot;{0} must not contain null&quot;
    * </pre>
    * 
    * @param name
    *           argument name
    * @param args
    *           the array to check for null
    */
   public static void containsNoNull(final String name, final Collection<?> args) {
      notNull(name, args);
      for (final Object o : args) {
         if (o == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                  "{0} must not contain null", name));
         }
      }
   }

   /**
    * Check the given args collection if it is empty.
    * <p>
    * 
    * Throw an {@link IllegalArgumentException} if the condition is not
    * fulfilled. The args argument will be checked for null using
    * {@link #notNull(String, Object)}. Exception message format will be:
    * 
    * <pre>
    * &quot;{0} must not be empty&quot;
    * </pre>
    * 
    * @param name
    *           argument name
    * @param args
    *           the array to check for null
    */
   public static void notEmpty(final String name, final Collection<?> args) {
      notNull(name, args);

      if (args.isEmpty()) {
         throw new IllegalArgumentException(MessageFormat.format(
               "{0} must not be empty", name));
      }
   }
   
   /**
    * Check the given args array if it is empty.
    * <p>
    * 
    * Throw an {@link IllegalArgumentException} if the condition is not
    * fulfilled. The args argument will be checked for null using
    * {@link #notNull(String, Object)}. Exception message format will be:
    * 
    * <pre>
    * &quot;{0} must not be empty&quot;
    * </pre>
    * 
    * @param name
    *           argument name
    * @param args
    *           the array to check for null
    */
   public static void notEmpty(final String name, final Object[] args) {
      notNull(name, args);

      if (args.length == 0) {
         throw new IllegalArgumentException(MessageFormat.format(
               "{0} must not be empty", name));
      }
   }

   /**
    * Check the given string arg if it is empty.
    * <p>
    * 
    * Throw an {@link IllegalArgumentException} if the condition is not
    * fulfilled. The args argument will be checked for null using
    * {@link #notNull(String, Object)}. The <code>arg</code> will be trimmed
    * first. Exception message format will be:
    * 
    * <pre>
    * &quot;{0} must not be empty&quot;
    * </pre>
    * 
    * @param name
    *           argument name
    * @param arg
    *           the string to check if it is empty
    */
   public static void notEmpty(final String name, final String arg) {
      notNull(name, arg);
      if (arg.trim().isEmpty()) {
         throw new IllegalArgumentException(MessageFormat.format(
               "{0} must not be empty", name));
      }
   }

   /**
    * Check if the given condition is false and throw an
    * {@link IllegalArgumentException} if so.
    * <p>
    * 
    * The message format used here is
    * 
    * <pre>
    * {o} must be true
    * </pre>
    * 
    * @param name
    *           argument name
    * @param arg
    *           to check
    */
   public static void isTrue(final String name, final boolean condition) {
      if (!condition) {
         throw new IllegalArgumentException(MessageFormat.format(
               "{0} must be true", name));
      }
   }

   /**
    * Check if the given arg class is the same as or a subtype of clazz
    * argument.
    * <p>
    * 
    * The message format used here is
    * 
    * <pre>
    * &quot;{0} is not a {1} instance&quot;
    * </pre>
    * 
    * This will check for null too by calling {@link #notNull(String, Object)}.
    * <p>
    * 
    * @param name
    *           argument name
    * @param clazz
    *           class to check the argument
    * @param arg
    *           to check
    */
   public static void isSubtype(final String name, final Class<?> clazz,
         final Object arg) {
      notNull(name, arg);
      isSubtype(name, clazz, arg.getClass());
   }
   
   /**
    * Check if the given type is the same as or a subtype of clazz
    * argument.
    * <p>
    * 
    * The message format used here is
    * 
    * <pre>
    * &quot;{0} is not a {1} instance&quot;
    * </pre>
    * 
    * This will check for null too by calling {@link #notNull(String, Object)}.
    * <p>
    * 
    * @param name
    *           argument name
    * @param clazz
    *           class to check the argument
    * @param type
    *           to check
    */
   public static void isSubtype(final String name, final Class<?> clazz, 
         final Class<?> type) {
      notNull("clazz", clazz);
      notNull("type", type);
      if (!clazz.isAssignableFrom(type)) {
         throw new IllegalArgumentException(MessageFormat.format(
               "{0} is not a {1} instance", name, clazz.getName()));
      }
   }
   

   /**
    * Check if the given key is a key in the given map.
    * <p>
    * 
    * The message format used here is
    * 
    * <pre>
    * &quot;{0} is not a contained in {1}&quot;
    * </pre>
    * 
    * This will check for null to by calling {@link #notNull(String, Object)}.
    * <p>
    * 
    * @param name
    *           argument name
    * @param clazz
    *           class to check the argument
    * @param arg
    *           to check
    */
   public static <T> void containsKey(final String keyName, T key,
         final Map<T, ?> map, final String mapName) {
      notNull(keyName, key);
      if (!map.containsKey(key)) {
         throw new IllegalArgumentException(MessageFormat.format(
               "{0} is not a contained in {1}", keyName, mapName));
      }
   }
}
