/**
 * 
 */
package gr.uom.se.util.reflect;

import gr.uom.se.util.filter.Filter;
import gr.uom.se.util.filter.FilterUtils;
import gr.uom.se.util.validation.ArgsCheck;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A class of static utility methods used for reflection.
 * <p>
 * This class and its implementations is not a comprehended reflection tool but
 * a set of methods that are used throughout software packages developed by
 * University of Macedonia, Soft. Eng. team.
 * 
 * @author Elvis Ligu
 */
public class ReflectionUtils {

   /**
    * Return true if the given type is not an interface, not an array not an
    * abstract, not an enum and not a primitive.
    * <p>
    * 
    * @param clazz
    *           to check for
    * @return true if the type is concrete implementation
    */
   public static boolean isConcrete(Class<?> clazz) {
      ArgsCheck.notNull("clazz", clazz);
      int mod = clazz.getModifiers();
      return !(clazz.isInterface() || clazz.isArray()
            || Modifier.isAbstract(mod) || clazz.isEnum() || clazz
               .isPrimitive());
   }

   /**
    * Get the default (non parameter) constructor.
    * <p>
    * This method will return even a private constructor.
    * 
    * @param module
    *           the class of constructor
    * @return the default constructor or null if there is not any
    */
   @SuppressWarnings("unchecked")
   public static <T> Constructor<T> getDefaultConstructor(Class<T> module) {

      ArgsCheck.notNull("module", module);
      ArgsCheck.isTrue("module not interface", !module.isInterface());
      ArgsCheck.isTrue("module", !module.isAnnotation());

      for (Constructor<?> c : module.getDeclaredConstructors()) {
         Class<?>[] types = c.getParameterTypes();
         if (types == null || types.length == 0) {
            return (Constructor<T>) c;
         }
      }
      return null;
   }

   /**
    * Get the constructors of the given class based on the given filter.
    * <p>
    * 
    * @param module
    *           the class of constructors
    * @param filter
    *           to be used to check the returned constructors, null is allowed.
    * @return a set of constructors for the given class based on the provided
    *         filter
    */
   @SuppressWarnings("unchecked")
   public static <T> Set<Constructor<T>> getConstructors(Class<T> module,
         Filter<Constructor<?>> filter) {

      ArgsCheck.notNull("module", module);
      ArgsCheck.isTrue("module " + module.getClass()
            + " must have implementation", !module.isInterface());
      ArgsCheck.isTrue("module " + module.getClass()
            + " not an annotation", !module.isAnnotation());
      ArgsCheck.isTrue("module " + module.getClass()
            + " not a primitive", !module.isPrimitive());

      // Use the null filter if no filter is provided
      if (filter == null) {
         filter = FilterUtils.NULL();
      }

      Set<Constructor<T>> constructors = new HashSet<>();
      for (Constructor<?> c : module.getDeclaredConstructors()) {
         if (filter.accept(c)) {
            constructors.add((Constructor<T>) c);
         }
      }
      return constructors;
   }

   /**
    * Get all methods of the {@code source} class that can be invoked from the
    * {code accessing} class.
    * <p>
    * The returned methods are accessible from the caller {@code accessing} and
    * can be invoked without throwing an {@link IllegalAccessException}.
    * <p>
    * The returned methods contain even inherited methods from other super
    * classes.
    * <p>
    * In order to filter which methods should be returned use
    * {@link #getAccessibleMethods(Class, Class, Filter)}.
    * 
    * @param source
    *           of the methods that are returned
    * @param accessing
    *           the class that will invoke the returned methods
    * @return the accessible methods of {@code source} class that can be invoked
    *         from {@code accessing} class.
    */
   public static Set<Method> getAccessibleMethods(Class<?> source,
         Class<?> accessing) {
      return getAccessibleMethods(source, accessing, null);
   }

   /**
    * Get all methods of the {@code source} class that can be invoked from the
    * {code accessing} class.
    * <p>
    * The returned methods are accessible from the caller {@code accessing} and
    * can be invoked without throwing an {@link IllegalAccessException}.
    * <p>
    * The returned methods contain even inherited methods from other super
    * classes.
    * 
    * @param source
    *           of the methods that are returned
    * @param accessing
    *           the class that will invoke the returned methods
    * @return the accessible methods of {@code source} class that can be invoked
    *         from {@code accessing} class.
    */
   public static Set<Method> getAccessibleMethods(Class<?> source,
         Class<?> accessing, Filter<Method> filter) {

      AccessibleMemberFilter<Method> accessFilter = new AccessibleMemberFilter<Method>(
            source, accessing);

      if (filter == null) {
         filter = accessFilter;
      } else {
         filter = FilterUtils.and(filter, accessFilter);
      }

      Set<Method> methods = getMethods(source, filter);
      return methods;
   }

   /**
    * Get the declared methods of {@code source} based on the given filter.
    * <p>
    * 
    * @param source
    *           of methods to be returned
    * @param filter
    *           to choose the methods, may be null
    * @return the filtered declared methods of {@code source}
    */
   public static Set<Method> getDeclaredMethods(Class<?> source,
         Filter<Method> filter) {

      ArgsCheck.notNull("source", source);

      if (filter == null) {
         filter = FilterUtils.NULL();
      }
      Set<Method> methods = new HashSet<>();
      for (Method m : source.getDeclaredMethods()) {
         if (filter.accept(m)) {
            methods.add(m);
         }
      }
      return methods;
   }

   /**
    * Get all the methods inherited from the specified {@code source}.
    * <p>
    * This method may return private methods. To prevent private methods from
    * results use a method filter.
    * 
    * @param source
    * @param filter
    * @return
    */
   @SuppressWarnings("unchecked")
   public static Set<Method> getInheritedMethods(Class<?> source,
         Filter<Method> filter) {

      ArgsCheck.notNull("source", source);

      // Static methods can not be inherited so filter them out
      Filter<Method> nonStatic = FilterUtils
            .not((Filter<Method>) MemberModifierFilter.STATIC_FILTER);
      if (filter == null) {
         filter = nonStatic;
      } else {
         filter = FilterUtils.and(filter, FilterUtils
               .not((Filter<Method>) MemberModifierFilter.STATIC_FILTER));
      }
      Class<?> superClass = source.getSuperclass();
      Set<Method> methods = new HashSet<>();
      while (superClass != null) {
         Set<Method> found = getDeclaredMethods(superClass, filter);
         checkedCopy(methods, found);
         superClass = superClass.getSuperclass();
      }

      return methods;
   }

   /**
    * Copy the methods of second collection to methods of first collection.
    * <p>
    * All methods returned will have distinct signatures.
    * 
    * @param methods1
    * @param methods2
    */
   static void checkedCopy(Set<Method> methods1, Set<Method> methods2) {
      for (Method m : methods2) {
         if (!containsSameMethod(methods1, m)) {
            methods1.add(m);
         }
      }
   }

   /**
    * Get the methods of all interfaces of {@code source} walking up the
    * hierarchy tree.
    * <p>
    * The methods will be added to {@code methods} parameter. After the
    * execution the set will contain methods with distinct signatures.
    * 
    * @param methods
    *           the set that all retrieved methods will be stored
    * @param source
    *           from which to walk up the hierarchy of interfaces
    * @param filter
    *           to specify which methods should be retrieved, may be null
    */
   public static void getInterfaceMethods(Set<Method> methods, Class<?> source,
         Filter<Method> filter) {

      ArgsCheck.notNull("source", source);
      ArgsCheck.notNull("methods", methods);

      if (source.isInterface()) {
         Set<Method> declared = getDeclaredMethods(source, filter);
         checkedCopy(methods, declared);
      }
      for (Class<?> type : source.getInterfaces()) {
         getInterfaceMethods(methods, type, filter);
      }
      source = source.getSuperclass();
      if (source != null) {
         getInterfaceMethods(methods, source, filter);
      }

   }

   /**
    * Get all the methods walking up the {@code source} class hierarchy.
    * <p>
    * The methods can be filtered specifying a filter instance.
    * 
    * @param source
    *           to walk up the hierarchy of methods
    * @param filter
    *           to limit the returned methods, may be null
    * @return all the methods of {code source} class
    */
   public static Set<Method> getMethods(Class<?> source, Filter<Method> filter) {
      // Methods declared within class
      Set<Method> methods = getDeclaredMethods(source, filter);
      // Methods that are inherited
      checkedCopy(methods, getInheritedMethods(source, filter));
      // Interface methods that are not implemented,
      // in case this class is abstract or an interface
      getInterfaceMethods(methods, source, filter);
      return methods;
   }

   /**
    * Check if {@code methods} set contains any method with the same signature
    * as the {code method} parameter.
    * <p>
    * 
    * @param methods
    *           to check for containment
    * @param method
    *           the signature of which to check against
    * @return true if the set contains any method with the same signature as the
    *         {code method} parameter.
    */
   static boolean containsSameMethod(Collection<Method> methods, Method method) {
      for (Method m : methods) {
         if (!m.getName().equals(method.getName())) {
            continue;
         }
         if (!m.getReturnType().equals(method.getReturnType())) {
            continue;
         }

         if (Arrays.equals(m.getTypeParameters(), method.getTypeParameters())) {
            return true;
         }
      }
      return false;
   }

   /**
    * Check if the given {@code mod} modifiers contain any of the specified
    * modifiers.
    * <p>
    * This method will work only if the {@code mod} is retrieved using
    * {@code getModifier()} method and the specified modifiers are numbers
    * values as {@link Modifier#PUBLIC}.
    * 
    * @param mod
    *           the modifiers of a member or a type
    * @param modifiers
    *           to check for availability
    * @return true if {@code mod} has any of the specified {@code modifiers}
    */
   public static boolean hasAnyOfModifiers(int mod, int... modifiers) {
      for (int m : modifiers) {
         if ((mod & m) != 0) {
            return true;
         }
      }
      return false;
   }

   /**
    * Check if the given {@code member} of {@code target} is accessible from the
    * given {@code clazz}.
    * <p>
    * 
    * @param member
    *           to check for access
    * @param target
    *           that contains the member
    * @param clazz
    *           that requires access to the member from the given target
    * @return true if the member is accessible from clazz, using the target
    */
   public static boolean isAccessible(Member member, Class<?> target,
         Class<?> clazz) {

      Class<?> declaringClass = member.getDeclaringClass();
      int mod = member.getModifiers();

      // If the member is public then it is obviously
      // accessible
      if (Modifier.isPublic(mod)) {
         return true;
      }

      if (Modifier.isPrivate(mod) && declaringClass.equals(target)) {
         // Case when the member and the given class has the same
         // top declaring class
         Class<?> mTopClass = getTopDeclaringClass(declaringClass);
         Class<?> cTopClass = getTopDeclaringClass(clazz);
         if (mTopClass.equals(cTopClass)) {
            return true;
         }
      }

      return declaringClass.getPackage().equals(clazz.getPackage())
            && !Modifier.isPrivate(mod);

   }

   /**
    * If the specified class is a nested type then return the top class that
    * this type is declared.
    * <p>
    * 
    * @param clazz
    *           the nested type to get the top class
    * @return the top class contained the given class, if the specified class is
    *         not nested it will return this one.
    */
   public static Class<?> getTopDeclaringClass(Class<?> clazz) {
      Class<?> topDeclaring = clazz.getDeclaringClass();
      if (topDeclaring == null) {
         return clazz;
      } else {
         return getTopDeclaringClass(topDeclaring);
      }
   }
}
