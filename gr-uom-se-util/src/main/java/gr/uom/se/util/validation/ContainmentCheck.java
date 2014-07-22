/**
 * 
 */
package gr.uom.se.util.validation;

import java.util.Collection;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ContainmentCheck {

   /**
    * Check the given args array if it contains at most one null object.
    * <p>
    * 
    * @param args
    *           the array to check for null. Will return true if only one of the
    *           specified objects is null.
    */
   public static boolean atMostOneNull(final Object... args) {
      int sum = 0;
      for (final Object o : args) {
         if (o == null) {
            sum++;
            if (sum == 2) {
               return false;
            }
         }
      }
      return true;
   }

   /**
    * Check the given args collection if it contains at most one null object.
    * <p>
    * 
    * @param args
    *           the array to check for null. Will return true if at at most one
    *           of the specified objects is null.
    */
   public static boolean atMostOneNull(final Collection<?> args) {
      int sum = 0;
      for (final Object o : args) {
         if (o == null) {
            sum++;
            if (sum == 2) {
               return false;
            }
         }
      }
      return true;
   }

   /**
    * Check the given args array if it contains any null object.
    * <p>
    * 
    * @param args
    *           the array to check for null.
    * @return true if the specified array contains no null
    * 
    */
   public static boolean containsNoNull(final Object... args) {
      for (final Object o : args) {
         if (o == null) {
            return false;
         }
      }
      return true;
   }

   /**
    * Check the given collection if it contains any null object.
    * <p>
    * 
    * @param args
    *           the collection to check for null.
    * @return true if the specified collection contains no null
    * 
    */
   public static boolean containsNoNull(final Collection<?> args) {
      for (final Object o : args) {
         if (o == null) {
            return false;
         }
      }
      return true;
   }

   /**
    * Check if all the objects from the given source array are contained within
    * the array of symbols.
    * <p>
    * Note that two null objects are considered equals. Also, if the source
    * array is empty than this will return always true.
    * 
    * @param source
    *           array of objects to check
    * @param symbols
    *           array of symbols that are allowed to be contained in source
    * @return true if all objects contained in source are already contained in
    *         symbols two.
    */
   public static boolean containsOnly(final Object[] source,
         final Object[] symbols) {
      for (Object t : source) {
         if (!contains(symbols, t)) {
            return false;
         }
      }
      return true;
   }

   /**
    * Check if the given object is contained within source.
    * <p>
    * 
    * Note that two null objects are considered equals.
    * 
    * @param object
    *           to check for containment
    * @param source
    *           the array to check for containment
    * @return true if object is contained in source
    */
   public static boolean contains(final Object[] source, final Object object) {
      int i = 0;
      while (i < source.length) {
         Object s = source[i++];
         if (s == null) {
            if (object == null) {
               return true;
            }
         } else if (s.equals(object)) {
            return true;
         }
      }
      return false;
   }

   /**
    * Check if the given character is contained within source.
    * <p>
    * 
    * 
    * @param char to check for containment
    * @param source
    *           the array to check for containment
    * @return true if char is contained in source
    */
   public static boolean contains(final char[] source, final char ch) {
      int i = 0;
      while (i < source.length) {
         char s = source[i++];
         if (s == ch) {
            return true;
         }
      }
      return false;
   }

   /**
    * Check if all the objects from the given source array are contained within
    * the array of symbols.
    * <p>
    * Note that, if the source array is empty than this will return always true.
    * 
    * @param source
    *           array of objects to check
    * @param symbols
    *           array of symbols that are allowed to be contained in source
    * @return true if all objects contained in source are already contained in
    *         symbols two.
    */
   public static boolean containsOnly(final char[] source, final char[] symbols) {
      return containsOnly(source, 0, source.length, symbols);
   }

   /**
    * Check if all the objects from the given source array are contained within
    * the array of symbols.
    * <p>
    * Note that, if the source array is empty than this will return always true.
    * 
    * @param source
    *           array of objects to check
    * @param symbols
    *           array of symbols that are allowed to be contained in source
    * @param start
    *           the start of chars from source array to check
    * @param end
    *           the end of chars (exclusive) to check from source array
    * @return true if all objects contained in source are already contained in
    *         symbols two.
    */
   public static boolean containsOnly(final char[] source, int start, int end,
         final char[] symbols) {
      for (int i = start; i < end; i++) {
         char t = source[i];
         if (!contains(symbols, t)) {
            return false;
         }
      }
      return true;
   }

   /**
    * Check if all the objects from the given source are contained within the
    * collection of symbols.
    * <p>
    * Note that two null objects are considered equals. Also, if the source is
    * empty than this will return always true.
    * 
    * @param source
    *           collection of objects to check
    * @param symbols
    *           collection of symbols that are allowed to be contained in source
    * @return true if all objects contained in source are already contained in
    *         symbols two.
    */
   public static boolean containsOnly(final Collection<?> source,
         final Collection<?> symbols) {
      for (Object t : source) {
         if (!symbols.contains(t)) {
            return false;
         }
      }
      return true;
   }

   /**
    * Check if all the objects from the given source are contained within the
    * array of symbols.
    * <p>
    * Note that two null objects are considered equals. Also, if the source is
    * empty than this will return always true.
    * 
    * @param source
    *           collection of objects to check
    * @param symbols
    *           arra of symbols that are allowed to be contained in source
    * @return true if all objects contained in source are already contained in
    *         symbols two.
    */
   public static boolean containsOnly(final Collection<?> source,
         final Object... symbols) {
      for (Object t : source) {
         if (!contains(symbols, t)) {
            return false;
         }
      }
      return true;
   }
}
