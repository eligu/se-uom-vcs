/**
 * 
 */
package gr.uom.se.util.manager;

import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.manager.annotations.Init;
import gr.uom.se.util.manager.annotations.Stop;
import gr.uom.se.util.module.ModuleLoader;
import gr.uom.se.util.module.ModuleManager;
import gr.uom.se.util.reflect.ReflectionUtils;
import gr.uom.se.util.validation.ArgsCheck;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class AbstractManager {

   private ConfigManager config;
   private ModuleManager moduleManager;

   private ReentrantReadWriteLock managersLock = new ReentrantReadWriteLock();
   private Set<Key> managers = new HashSet<Key>();

   private ReentrantReadWriteLock registeredManagersLock = new ReentrantReadWriteLock();
   private Set<Class<?>> registeredManagers = new HashSet<>();

   public void registerManager(Class<?> managerClass) {

      ArgsCheck.notNull("managerClass", managerClass);
      // Register first as a class manager
      moduleManager.registerAsProperty(managerClass);
      // Register into the cache of managers
      registeredManagersLock.writeLock().lock();
      try {
         registeredManagers.add(managerClass);
      } finally {
         registeredManagersLock.writeLock().unlock();
      }
   }

   public void  loadManager(Class<?> manager) {

      ArgsCheck.notNull("manager", manager);
      // If manager is not registered then
      // register it and load it
      if (!isRegistered(manager)) {
         registerManager(manager);
      }
      
      managersLock.writeLock().lock();
      try {
         // Check if the manager is loaded
         if (isLoaded(manager)) {
            return;
         }
         // Load the manager
         ModuleLoader loader = moduleManager.getLoader(manager);
         if (loader == null) {
            throw new IllegalArgumentException("manager " + manager
                  + " can not be loaded");
         }

         Object mo = loader.load(manager);
         if (mo == null) {
            throw new IllegalArgumentException("manager " + manager
                  + " can not be loaded");
         }

         managers.add(new Key(mo, false));
      } finally {
         managersLock.writeLock().unlock();
      }
   }

   public void startManager(Class<?> manager) {
      ArgsCheck.notNull("manager", manager);

      // Try to load if it is not loaded
      loadManager(manager);

      // The manager is now loaded
      // now find a method to start the manager
      Method initMethod = findInitMethod(manager);
      
      managersLock.writeLock().lock();
      try {
         
      } finally {
         managersLock.writeLock().unlock();
      }
      
   }

   private Method findInitMethod(Class<?> manager) {
      Set<Method> methods = ReflectionUtils.getAccessibleMethods(manager,
            this.getClass());
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
   
   private Method findStopMethod(Class<?> manager) {
      Set<Method> methods = ReflectionUtils.getAccessibleMethods(manager,
            this.getClass());
      for (Method m : methods) {
         int mod = m.getModifiers();
         if (!Modifier.isStatic(mod)) {
            Stop an = m.getAnnotation(Stop.class);
            if (an != null) {
               return m;
            }
         }
      }
      return null;
   }

   public boolean isLoaded(Class<?> manager) {
      managersLock.readLock().lock();
      try {
         for (Key m : managers) {
            if (manager.isAssignableFrom(m.manager.getClass())) {
               return true;
            }
         }
         return false;
      } finally {
         managersLock.readLock().unlock();
      }
   }
   
   private boolean isRegistered(Class<?> manager) {
      registeredManagersLock.readLock().lock();
      try {
         for (Class<?> m : registeredManagers) {
            if (manager.isAssignableFrom(m)) {
               return true;
            }
         }
         return false;
      } finally {
         registeredManagersLock.readLock().unlock();
      }
   }

   private boolean isStarted(Class<?> manager) {
      managersLock.readLock().lock();
      try {
         Key assignable = null;
         Iterator<Key> it = managers.iterator();
         while (it.hasNext() && assignable == null) {
            Key current = it.next();
            if (current.manager.getClass().equals(manager)) {
               return current.started;
            } else if (manager.isAssignableFrom(current.manager.getClass())) {
               assignable = current;
            }
         }

         if (assignable != null) {
            return assignable.started;
         } else {
            return false;
         }
      } finally {
         managersLock.readLock().unlock();
      }
   }

   private static class Key {
      boolean started;
      Object manager;

      public Key(Object manager, boolean started) {
         this.started = started;
         this.manager = manager;
      }
   }
}
