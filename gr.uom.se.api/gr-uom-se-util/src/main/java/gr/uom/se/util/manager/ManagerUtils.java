/**
 * 
 */
package gr.uom.se.util.manager;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

import gr.uom.se.util.config.ConfigDomain;
import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.manager.annotations.Init;
import gr.uom.se.util.reflect.ReflectionUtils;
import gr.uom.se.util.validation.ArgsCheck;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ManagerUtils {

   public static String getManagerDomainName(ConfigManager config) {
      ArgsCheck.notNull("config", config);
      String domain = config.getProperty(
            ManagerConstants.DEFAULT_DOMAIN_PROPERTY, String.class);
      if (domain == null) {
         domain = ManagerConstants.DEFAULT_DOMAIN;
      }
      return domain;
   }

   public static ConfigDomain getManagerDomain(ConfigManager config) {
      String name = getManagerDomainName(config);
      return config.getDomain(name);
   }

   /**
    * Find the instance method that is annotated with {@link Init}.
    * <p>
    * 
    * @param type
    *           to find the method of
    * @param accessor
    *           the one who is going to call this method
    * @return an instance method annotated with {@link Init}.
    */
   public static Method findInstanceInitMethod(Class<?> type, Class<?> accessor) {
      Set<Method> methods = ReflectionUtils
            .getAccessibleMethods(type, accessor);
      for (Method m : methods) {
         int mod = m.getModifiers();
         if (!Modifier.isStatic(mod)) {
            Init an = m.getAnnotation(Init.class);
            if (an != null) {
               return m;
            }
         }
      }
      return null;
   }
   
   /**
    * Find the static method that is annotated with {@link Init}.
    * <p>
    * 
    * @param type
    *           to find the method of
    * @param accessor
    *           the one who is going to call this method
    * @return an instance method annotated with {@link Init}.
    */
   public static Method findStaticInitMethod(Class<?> type, Class<?> accessor) {
      Set<Method> methods = ReflectionUtils
            .getAccessibleMethods(type, accessor);
      for (Method m : methods) {
         int mod = m.getModifiers();
         if (Modifier.isStatic(mod)) {
            Init an = m.getAnnotation(Init.class);
            if (an != null) {
               return m;
            }
         }
      }
      return null;
   }
}
