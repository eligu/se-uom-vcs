/**
 * 
 */
package gr.uom.se.util.manager;

import gr.uom.se.util.manager.annotations.Init;
import gr.uom.se.util.manager.annotations.Stop;
import gr.uom.se.util.module.AbstractMethodConstructorExecutor;
import gr.uom.se.util.module.ModuleLoader;
import gr.uom.se.util.module.ModuleManager;
import gr.uom.se.util.module.ModuleUtils;
import gr.uom.se.util.module.ParameterProvider;
import gr.uom.se.util.reflect.ReflectionUtils;
import gr.uom.se.util.validation.ArgsCheck;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class AbstractMainManager implements MainManager {

   private ReentrantReadWriteLock managersLock = new ReentrantReadWriteLock();
   private Set<Key> managers = new HashSet<Key>();

   private ReentrantReadWriteLock registeredManagersLock = new ReentrantReadWriteLock();
   private Set<Class<?>> registeredManagers = new HashSet<>();

   private Executor executor = new Executor();

   public AbstractMainManager() {
   }

   protected abstract ModuleManager getModuleManager();

   /*
    * {@inheritDoc)
    * 
    * @see gr.uom.se.util.manager.MainManager#registerManager(java.lang.Class)
    */
   @Override
   public void registerManager(Class<?> managerClass) {
      ArgsCheck.notNull("managerClass", managerClass);

      // Register into the cache of managers
      registeredManagersLock.writeLock().lock();
      try {
         // Check the class of this manager
         // It should be a concrete implementation so
         // when it is required to be loaded by an interface
         // it can be loaded
         int mod = managerClass.getModifiers();
         if (Modifier.isInterface(mod) || managerClass.isAnnotation()
               || Modifier.isAbstract(mod) || managerClass.isArray()) {
            throw new IllegalArgumentException(
                  "class "
                        + managerClass
                        + " can not be registered as a manager, it should be a concrete class");
         }
         // Register into the cache
         // Register as a class manager
         if (registeredManagers.add(managerClass)) {
            getModuleManager().registerAsProperty(managerClass);
         }
      } finally {
         registeredManagersLock.writeLock().unlock();
      }
   }

   /*
    * {@inheritDoc)
    * 
    * @see gr.uom.se.util.manager.MainManager#registerLoaded(java.lang.Object)
    */
   @Override
   public void registerLoaded(Object manager) {
      ArgsCheck.notNull("manager", manager);
      registeredManagersLock.writeLock().lock();
      managersLock.writeLock().lock();
      try {
         // Ensure that this manager is removed first
         // If a manager that is a subtype of this
         // manager is already loaded it will be unloaded
         removeManager(manager.getClass());
         // Register to the classes
         registerManager(manager.getClass());
         // Now add to the loaded managers
         // as a non started manager
         managers.add(new Key(manager, false));

      } finally {
         managersLock.writeLock().unlock();
         registeredManagersLock.writeLock().unlock();
      }
   }

   /*
    * {@inheritDoc)
    * 
    * @see gr.uom.se.util.manager.MainManager#removeManager(java.lang.Class)
    */
   @SuppressWarnings("unchecked")
   @Override
   public <T> T removeManager(Class<T> managerClass) {
      ArgsCheck.notNull("managerClass", managerClass);
      // Register into the cache of managers
      registeredManagersLock.writeLock().lock();
      managersLock.writeLock().lock();
      try {

         // Unload the manager first
         Object manager = unloadManager(managerClass);
         // Now remove from the list of registered managers
         if (manager != null) {
            registeredManagers.remove(manager.getClass());
         }
         return (T) manager;
      } finally {
         managersLock.writeLock().unlock();
         registeredManagersLock.writeLock().unlock();
      }
   }

   /*
    * {@inheritDoc)
    * 
    * @see gr.uom.se.util.manager.MainManager#getManager(java.lang.Class)
    */
   @Override
   @SuppressWarnings("unchecked")
   public <T> T getManager(Class<T> manager) {
      managersLock.readLock().lock();
      try {
         Key key = getManager0(manager);
         if (key != null && key.started) {
            return (T) key.manager;
         }
      } finally {
         managersLock.readLock().unlock();
      }
      return startManager(manager);
   }

   /*
    * {@inheritDoc)
    * 
    * @see gr.uom.se.util.manager.MainManager#loadManager(java.lang.Class)
    */
   @Override
   @SuppressWarnings("unchecked")
   public <T> T loadManager(Class<T> manager) {

      ArgsCheck.notNull("manager", manager);

      managersLock.writeLock().lock();
      registeredManagersLock.writeLock().lock();
      try {
         // If manager is not registered then
         // register it and load it
         Class<T> rManager = getRegisteredManager0(manager);
         if (rManager == null) {
            registerManager(manager);
            rManager = manager;
         }

         // Check if the manager is loaded
         Key managerKey = getManager0(rManager);
         if (managerKey != null) {
            return (T) managerKey.manager;
         }

         // Load the manager
         ModuleLoader loader = getModuleManager().getLoader(rManager);
         if (loader == null) {
            throw new IllegalArgumentException("manager " + manager
                  + " can not be loaded, no module loader found");
         }

         T mo = loader.load(rManager);
         if (mo == null) {
            throw new IllegalArgumentException("manager " + manager
                  + " can not be loaded");
         }

         // Add manager to cache
         managers.add(new Key(mo, false));
         // Register manager implementation to module manager so he can
         // be injected to other beans
         getModuleManager().registerAsProperty(mo);
         // return
         return mo;
      } finally {
         registeredManagersLock.writeLock().unlock();
         managersLock.writeLock().unlock();
      }
   }

   /*
    * {@inheritDoc)
    * 
    * @see gr.uom.se.util.manager.MainManager#unloadManager(java.lang.Class)
    */
   @Override
   @SuppressWarnings("unchecked")
   public <T> T unloadManager(Class<T> manager) {
      ArgsCheck.notNull("manager", manager);

      managersLock.writeLock().lock();
      try {
         stopManager(manager);
         Key managerKey = getManager0(manager);
         if (managerKey != null) {
            // Remove this manager from cache
            managers.remove(managerKey);
            // Remove this manager from module manager so he can not be injected
            getModuleManager().removeAsProperty(managerKey.manager);

            return (T) managerKey.manager;
         }
         return null;
      } finally {
         managersLock.writeLock().unlock();
      }
   }

   /*
    * {@inheritDoc)
    * 
    * @see gr.uom.se.util.manager.MainManager#startManager(java.lang.Class)
    */
   @Override
   @SuppressWarnings("unchecked")
   public <T> T startManager(Class<T> manager) {
      ArgsCheck.notNull("manager", manager);

      managersLock.writeLock().lock();
      try {
         // Try to load if it is not loaded
         loadManager(manager);

         // Retrieve the loaded manager
         Key managerKey = getManager0(manager);
         if (managerKey == null || managerKey.manager == null) {
            throw new RuntimeException("manager of class " + manager
                  + " can not be found");
         }

         // If the manager is already started just leave the method
         if (managerKey.started) {
            return (T) managerKey.manager;
         }

         // The manager is now loaded
         // now find a method to start the manager
         Object managerInstance = managerKey.manager;
         Method initMethod = findInitMethod(managerInstance.getClass());

         // If init annotation is not present that means we
         // doesn't have a init method for this manager, otherwise
         // execute the init method
         if (initMethod != null) {
            executor.execute(managerInstance, managerInstance.getClass(),
                  initMethod,
                  ModuleUtils.getModuleConfig(managerInstance.getClass()));
         }
         managerKey.started = true;
         return (T) managerKey.manager;

      } finally {
         managersLock.writeLock().unlock();
      }
   }

   /*
    * {@inheritDoc)
    * 
    * @see gr.uom.se.util.manager.MainManager#stopManager(java.lang.Class)
    */
   @Override
   @SuppressWarnings("unchecked")
   public <T> T stopManager(Class<T> manager) {
      ArgsCheck.notNull("manager", manager);

      managersLock.writeLock().lock();
      try {
         // Retrieve the loaded manager
         Key managerKey = getManager0(manager);
         if (managerKey == null) {
            return null;
         }

         Object managerInstance = managerKey.manager;
         // If the manager is not started just
         // leave this method
         if (!managerKey.started) {
            return (T) managerInstance;
         }

         Method stopMethod = findStopMethod(managerInstance.getClass());

         // If stop annotation is not present that means we
         // doesn't have a stop method for this manager, otherwise
         // execute the method
         if (stopMethod != null) {
            executor.execute(managerInstance, managerInstance.getClass(),
                  stopMethod,
                  ModuleUtils.getModuleConfig(managerInstance.getClass()));
         }
         managerKey.started = false;
         return (T) managerKey.manager;
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

   /*
    * {@inheritDoc)
    * 
    * @see gr.uom.se.util.manager.MainManager#isLoaded(java.lang.Class)
    */
   @Override
   public boolean isLoaded(Class<?> manager) {
      return getManager0(manager) != null;
   }

   /*
    * {@inheritDoc)
    * 
    * @see gr.uom.se.util.manager.MainManager#isRegistered(java.lang.Class)
    */
   @Override
   public boolean isRegistered(Class<?> manager) {
      return getRegisteredManager0(manager) != null;
   }

   /*
    * {@inheritDoc)
    * 
    * @see gr.uom.se.util.manager.MainManager#isStarted(java.lang.Class)
    */
   @Override
   public boolean isStarted(Class<?> manager) {
      Key key = getManager0(manager);
      if (key != null) {
         return key.started;
      }
      return false;
   }

   private Key getManager0(Class<?> manager) {
      managersLock.readLock().lock();
      try {
         Key assignable = null;
         Iterator<Key> it = managers.iterator();
         while (it.hasNext()) {
            Key current = it.next();
            if (current.manager.getClass().equals(manager)) {
               return current;
            } else if (manager.isAssignableFrom(current.manager.getClass())) {
               assignable = current;
            }
         }
         return assignable;
      } finally {
         managersLock.readLock().unlock();
      }
   }

   @SuppressWarnings("unchecked")
   private <T> Class<T> getRegisteredManager0(Class<T> manager) {
      registeredManagersLock.readLock().lock();
      try {
         Class<?> assignable = null;
         Iterator<Class<?>> it = registeredManagers.iterator();
         while (it.hasNext()) {
            Class<?> current = it.next();
            if (current.getClass().equals(manager)) {
               return manager;
            } else if (manager.isAssignableFrom(current)) {
               assignable = current;
            }
         }
         return (Class<T>) assignable;
      } finally {
         registeredManagersLock.readLock().unlock();
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

   private class Executor extends AbstractMethodConstructorExecutor {

      @Override
      protected ParameterProvider resolveParameterProvider(Class<?> type,
            Map<String, Map<String, Object>> properties) {
         return getModuleManager().getParameterProvider(type);
      }
   }
}
