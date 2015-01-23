/**
 * 
 */
package gr.uom.se.util.reflect;

import gr.uom.se.util.filter.Filter;
import gr.uom.se.util.filter.FilterUtils;
import gr.uom.se.util.validation.ArgsCheck;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A filter that allows only members that has one of the specified modifiers.
 * <p>
 * 
 * This class accepts an array of modifiers (integers) that are normally values
 * of {@link Modifier} class, such as {@link Modifier#STATIC}. Any other value
 * will not work properly and may cause false positive results.
 * 
 * @author Elvis Ligu
 * @param <T>
 * 
 */
public class MemberModifierFilter<T extends Member> implements Filter<T> {

   /**
    * A filter for the static modifier.
    * <p>
    */
   public static final Filter<? extends Member> STATIC_FILTER = new MemberModifierFilter<>(
         Modifier.STATIC);

   /**
    * A filter for the public modifier.
    * <p>
    */
   public static final Filter<? extends Member> PUBLIC_FILTER = new MemberModifierFilter<>(
         Modifier.PUBLIC);

   /**
    * A filter for the private modifier.
    * <p>
    */
   public static final Filter<? extends Member> PRIVATE_FILTER = new MemberModifierFilter<>(
         Modifier.PRIVATE);

   /**
    * A filter for protected modifier.
    * <p>
    */
   public static final Filter<? extends Member> PROTECTED_FILTER = new MemberModifierFilter<>(
         Modifier.PROTECTED);

   /**
    * Create a modifiers filter that will check if a given member contains all
    * modifiers.
    * <p>
    * Check that all provided values do not conflict with each other, such as
    * two modifiers {@link Modifier#PRIVATE} {@link Modifier#PROTECTED} can not
    * be in the same member.
    * 
    * @param mods
    *           the modifier values of the members to check
    * @return true if the filtered member contains all the modifiers.
    */
   public static <T extends Member> Filter<T> and(int... mods) {
      ArgsCheck.notNull("mods", mods);
      List<Filter<T>> filters = new ArrayList<>();
      for (int i : mods) {
         filters.add(new MemberModifierFilter<T>(i));
      }
      return FilterUtils.and(filters);
   }

   /**
    * Array of modifier values.
    * <p>
    */
   private int[] mods;

   /**
    * Create an instance of modifier filter that will allow each member that
    * contains at least one of the provided modifiers.
    * <p>
    * 
    * @param mods
    *           the modifier values
    */
   public MemberModifierFilter(int... mods) {
      if (mods == null) {
         this.mods = new int[] {};
      } else {

         this.mods = Arrays.copyOf(mods, mods.length);
      }
   }

   @Override
   public boolean accept(T t) {
      return ReflectionUtils.hasAnyOfModifiers(t.getModifiers(), mods);
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + Arrays.hashCode(mods);
      return result;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      MemberModifierFilter<?> other = (MemberModifierFilter<?>) obj;
      if (!Arrays.equals(mods, other.mods))
         return false;
      return true;
   }
}
