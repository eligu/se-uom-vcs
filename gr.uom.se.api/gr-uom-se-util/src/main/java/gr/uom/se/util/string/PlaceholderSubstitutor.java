package gr.uom.se.util.string;

import java.util.Map;

/**
 * A class containing static methods to replace placeholders of the form ${name}
 * with values.
 * <p>
 * 
 * @author Elvis Ligu
 */
public final class PlaceholderSubstitutor {

   /**
    * Given a character sequence ({@code str}) that contains property templates
    * in the form of <code>${name}</code> where {@code name} is the property
    * name equals to {@code placeHolder} replace all occurrences of it with the
    * given value.
    * <p>
    * @param str the character sequence to replace all placeholders
    * @param placeHolder the name of the placeholder (without ${..})
    * @param value the value to replace all placeholders
    * @return a new string representation after replacing all placeholders
    */
   public final static String replace(CharSequence str, CharSequence placeHolder, CharSequence value) {
      return replace(str, 0, placeHolder, value);
   }

   /**
    * Given a character sequence ({@code str}) that contains property templates
    * in the form of <code>${name}</code> where {@code name} is the property
    * name equals to {@code placeHolder} replace all occurrences of it with the
    * given value.
    * <p>
    * Note that this method will start to replace placeholders from the position
    * {@code begin}.
    *
    * @param str the character sequence to replace all placeholders
    * @param begin the start from where to replace placeholders
    * @param placeHolder the name of the placeholder (without ${..})
    * @param value the value to replace all placeholders
    * @return a new string representation after replacing all placeholders
    */
   public final static String replace(CharSequence str, int begin, CharSequence placeHolder, CharSequence value) {

      final char ch = '$';
      final char ch1 = '{';
      final char ch2 = '}';

      // If the current index of $ is not found
      // then just return
      int current = StringUtils.indexOf(str, ch, begin);
      if (current < 0) {
         return str.toString();
      }

      StringBuilder sb = null;

      int sLen = str.length();
      final int phLen = placeHolder.length();

      // Do this until no $ character was found or the
      // current index + 2 + placeholder length is greater or
      // equal to content length
      while (current > -1 && (current + 2 + phLen < sLen)) {
         // Check if the current char is {
         // if so it follows $
         if (str.charAt(current + 1) == ch1) {
            // If the characters from position current + 2
            // are the same as the placeholder and the character after
            // it is } that means we found a template ${placeHolder} 
            if (StringUtils.startsWith(str, placeHolder, current + 2)
                    && str.charAt(current + phLen + 2) == ch2) {

               // If we didn't created a string builder do it now
               // This is just in case the string has no placeholder
               // and we don't want to waste memory, therefore we lazy
               // initialize string builder.
               if (sb == null) {
                  if (!(str instanceof StringBuilder)) {
                     str = new StringBuilder(str);
                  }
               }
               // Make the replacement
               StringUtils.replace(str, current, current + phLen + 3, value);
               // Set the current position after the new value
               current += value.length();
               // Correct the content length
               sLen = str.length();

            } else {
               // This is in case we found ${ but didn't find the placeholder
               // which speeds up the looking
               current += 2;
            }
         } else {
            // Move the current position one ahead because nothing
            // were found
            current++;
         }
         // Ensure a check in order to preven from searching
         // for $ if current position is at the end
         if ((current + 3 + phLen) > sLen) {
            break;
         }
         current = StringUtils.indexOf(str, ch, current);
      }

      return str.toString();
   }

   /**
    * Given a character sequence ({@code str}) that contains property templates
    * in the form of <code>${name}</code> where {@code name} is the property
    * name equals to a {@code placeholder} replace all occurrences of it with a
    * value.
    * <p>
    * The specified placeholders will be used to find the placeholder values
    * each time a placeholder is encountered. This method is best suited when
    * there are a lot of placeholders to replace in source. Note that this
    * method will start to replace placeholders from the position {@code begin}.
    * <p>
    *
    * @param cs the character sequence to replace all placeholders
    * @param placeholders e key value pairs of placeholders and their values
    * @return a new string representation after replacing all placeholders, if
    * the given sequence is of type {@link StringBuilder} it will return the
    * same instance provided.
    */
   public final static CharSequence replace(CharSequence cs, Map<String, String> placeholders) {
      return replace(cs, 0, placeholders);
   }

   /**
    * Given a character sequence ({@code str}) that contains property templates
    * in the form of <code>${name}</code> where {@code name} is the property
    * name equals to a {@code placeholder} replace all occurrences of it with a
    * value.
    * <p>
    * The specified placeholders will be used to find the placeholder values
    * each time a placeholder is encountered. This method is best suited when
    * there are a lot of placeholders to replace in source. Note that this
    * method will start to replace placeholders from the position {@code begin}.
    * <p>
    *
    * @param cs the character sequence to replace all placeholders
    * @param begin the start from where to replace placeholders
    * @param placeholders e key value pairs of placeholders and their values
    * @return a new string representation after replacing all placeholders, if
    * the given sequence is of type {@link StringBuilder} it will return the
    * same instance provided.
    */
   public final static CharSequence replace(CharSequence cs, int begin, Map<String, String> placeholders) {

      char ch = '$';
      final char ch1 = '}';
      // If no $ character were found into this sequence then
      // return the sequence
      int current = StringUtils.indexOf(cs, ch, begin);
      if (current < 0) {
         return cs;
      }

      StringBuilder str;
      if (cs instanceof StringBuilder) {
         str = (StringBuilder) cs;
      } else {
         str = new StringBuilder(cs);
      }

      int sLen = str.length();
      while (current > -1 && current + 2 < sLen) {
         // Check if this is a start
         if (isStart(str, current)) {
            // If this is a start for placeholder
            // continue to find a name
            int endIndex = current + 2;
            while (endIndex < sLen) {
               // We found an end index here
               if (str.charAt(endIndex) == ch1) {
                  // We should extract the holder name
                  String name = str.substring(current + 2, endIndex);
                  // Now we should find this name within the placeholders
                  String value = placeholders.get(name);
                  // We found a value for the name
                  // so we must replace it
                  if (value != null) {
                     StringUtils.replace(str, current, current + name.length() + 3, value);
                     current += value.length();
                     sLen = str.length();
                  } else {
                     current += name.length();
                  }
                  // There is no need to continue here
                  // as long as we finished with the current
                  // placeholder
                  break;
               } else if (isStart(str, endIndex)) {
                  // We have possibly an embedded placeholder
                  // so we go recursively
                  sLen = replace(str, endIndex, placeholders).length();
               }
               endIndex++;
            }
            // We are at the end and nothing were found
            if (endIndex >= sLen) {
               return str;
            }
         } else {
            current++;
         }

         current = StringUtils.indexOf(str, ch, current);
      }

      return str;
   }

   private static boolean isStart(CharSequence str, int begin) {
      return str.length() > begin + 1
              && str.charAt(begin) == '$'
              && str.charAt(begin + 1) == '{';
   }
}
