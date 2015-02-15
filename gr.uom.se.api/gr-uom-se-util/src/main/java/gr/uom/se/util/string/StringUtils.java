package gr.uom.se.util.string;

import gr.uom.se.util.validation.ArgsCheck;

/**
 * A simple class with static methods to be used with character sequences.
 * <p>
 * 
 * @author Elvis Ligu
 */
public class StringUtils {

   /**
    * Replace the characters at given character sequence with the characters of
    * value.
    * <p>
    * If the given sequence is a {@link StringBuilder} it will return this
    * builder after calling replace on it.
    *
    * @param cs the character sequence to be replaced
    * @param start the position from where to start the replace
    * @param end the end of replaced characters (exclusive)
    * @param value the value that will replace the characters
    * @return a new character sequence or the same if the provided one is a
    * {@link StringBuilder}
    */
   public static CharSequence replace(CharSequence cs, int start, int end, CharSequence value) {
      ArgsCheck.notNull("cs", cs);
      ArgsCheck.notNull("value", value);
      StringBuilder sb;
      if (cs instanceof StringBuilder) {
         sb = (StringBuilder) cs;
      } else {
         sb = new StringBuilder(cs);
      }
      return sb.replace(start, end, value.toString());
   }

   /**
    * Check if the given character sequence starts with the prefix in the given
    * char sequence.
    * <p>
    * This is the same as calling
    * {@link #startsWith(CharSequence, CharSequence, int)} where the int will be
    * 0.
    *
    * @param cs the character sequence to be replaced
    * @param prefix the prefix to check
    * @return true if the given {@code cs} parameter starts with the given
    * {@code prefix}
    */
   public static boolean startsWith(CharSequence cs, CharSequence prefix) {
      return startsWith(cs, prefix, 0);
   }

   /**
    * Check if the given character sequence starts with the prefix in the given
    * char sequence.
    * <p>
    *
    * Start to look for the given prefix from the {@code toffset} parameter. If
    * the offset is out of the range of the characters it will return false.
    *
    * @param cs the character sequence to be replaced
    * @param prefix the prefix to check
    * @param toffset from where to start for the prefix.
    * @return true if the given {@code cs} parameter starts with the given
    * {@code prefix}
    */
   public static boolean startsWith(CharSequence cs, CharSequence prefix, int toffset) {
      int to = toffset;
      int po = 0;
      int pc = prefix.length();

      if ((toffset < 0) || (toffset > cs.length() - pc)) {
         return false;
      }
      while (--pc >= 0) {
         if (cs.charAt(to++) != prefix.charAt(po++)) {
            return false;
         }
      }
      return true;
   }

   /**
    * Get the index of the specified character with this sequence.
    * <p>
    * This works the same as {@link #indexOf(CharSequence, int, int)} where the
    * fromIndex parameter is 0.
    *
    * @param cs the character sequence to be replaced
    * @param ch the character to look the index for
    * @return the index of the specified character within the given sequence
    */
   public static int indexOf(CharSequence cs, int ch) {
      return indexOf(cs, ch, 0);
   }

   /**
    * Get the index of the specified character with this sequence.
    * <p>
    * If {@code fromIndex} is negative it will start from the beginning. If it
    * is greater or equal then the length of the sequence it will return -1.
    *
    * @param cs the character sequence to be replaced
    * @param ch the character to look the index for
    * @param fromIndex the start from where to look for this character
    * @return the index of the specified character within the given sequence, -1
    * if the character was not found.
    */
   public static int indexOf(CharSequence cs, int ch, int fromIndex) {
      final int max = cs.length();
      if (fromIndex < 0) {
         fromIndex = 0;
      } else if (fromIndex >= max) {
         // Note: fromIndex might be near -1>>>1.
         return -1;
      }

      if (ch < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
         // handle most cases here (ch is a BMP code point or a
         // negative value (invalid code point))
         for (int i = fromIndex; i < max; i++) {
            if (cs.charAt(i) == ch) {
               return i;
            }
         }
         return -1;
      } else {
         return indexOfSupplementary(cs, ch, fromIndex);
      }
   }

   private static int indexOfSupplementary(CharSequence cs, int ch, int fromIndex) {
      if (Character.isValidCodePoint(ch)) {
         final char hi = Character.highSurrogate(ch);
         final char lo = Character.lowSurrogate(ch);
         final int max = cs.length() - 1;
         for (int i = fromIndex; i < max; i++) {
            if (cs.charAt(i) == hi && cs.charAt(i + 1) == lo) {
               return i;
            }
         }
      }
      return -1;
   }
}
