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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A default implementation for the main manager.
 * <p>
 * This manager requires the subclasses to provide a module manager
 * implementation in order to work.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class AbstractMainManager implements MainManager {

   /**
    * A lock for synchronizing writes to managers and for allowing reads
    * where there are no writes.
    */
   private ReentrantReadWriteLock managersLock = new ReentrantReadWriteLock();
   /**
    * The set of loaded managers.
    * <p>
    */
   private List<Key> managers = new ArrayList<Key>();

   /**
    * The set of registered manager classes.
    * <p>
    */
   private Set<Class<?>> registeredManagers = new HashSet<>();

   /**
    * Used to execute init and stop methods.
    */
   private Executor executor = new Executor();

   public AbstractMainManager() {
   }

   /**
    * Subclasses must provide access to a module manager. This method should
    * never return null.
    * 
    * @return the module manager this instance is using.
    */
   protected abstract ModuleManager getModuleManager();

   /**
    * {@inheritDoc}
    */
   @Override
   public void registerManager(Class<?> managerClass) {
      ArgsCheck.notNull("managerClass", managerClass);

      // Register into the cache of managers
      managersLock.writeLock().lock();
      try {
         registerManager0(managerClass);
      } finally {
         managersLock.writeLock().unlock();
      }
   }

   /**
    * Register without lock.
    * 
    * @param managerClass
    */
   private void registerManager0(Class<?> managerClass) {
      // Check the class of this manager
      // It should be a concrete implementation so
      // when it is required to be loaded by an interface
      // it can be loaded
      if (!ReflectionUtils.isConcrete(managerClass)) {
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
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void registerLoaded(Object manager) {
      ArgsCheck.notNull("manager", manager);
      managersLock.writeLock().lock();
      try {
         registerLoaded0(manager);
      } finally {
         managersLock.writeLock().unlock();
      }
   }

   /**
    * Register without lock.
    * 
    * @param manager
    */
   private void registerLoaded0(Object manager) {
      // Ensure that this manager is removed first
      // If a manager that is a subtype of this
      // manager is already loaded it will be unloaded
      removeManager0(manager.getClass());
      // Register to the classes
      registerManager0(manager.getClass());
      // Now add to the loaded managers
      // as a non started manager
      managers.add(new Key(manager, false));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T removeManager(Class<T> managerClass) {
      ArgsCheck.notNull("managerClass", managerClass);
      // Register into the cache of managers
      managersLock.writeLock().lock();
      try {
         return removeManager0(managerClass);
      } finally {
         managersLock.writeLock().unlock();
      }
   }

   /**
    * Remove without lock.
    * 
    * @param managerClass
    * @return
    */
   @SuppressWarnings("unchecked")
   private <T> T removeManager0(Class<T> managerClass) {

      // Unload the manager first
      Object manager = unloadManager0(managerClass);
      // Now remove from the list of registered managers
      if (manager != null) {
         registeredManagers.remove(manager.getClass());
      }
      return (T) manager;
   }

   /**
    * {@inheritDoc}
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

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T loadManager(Class<T> manager) {

      ArgsCheck.notNull("manager", manager);

      managersLock.writeLock().lock();
      try {
         return loadManager0(manager);
      } finally {
         managersLock.writeLock().unlock();
      }
   }

   /**
    * Load without lock.
    * 
    * @param manager
    * @return
    */
   @SuppressWarnings("unchecked")
   private <T> T loadManager0(Class<T> manager) {
      // If manager is not registered then
      // register it and load it
      Class<T> rManager = getRegisteredManager0(manager);
      if (rManager == null) {
         registerManager0(manager);
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
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T unloadManager(Class<T> manager) {
      ArgsCheck.notNull("manager", manager);

      managersLock.writeLock().lock();
      try {
         return unloadManager0(manager);
      } finally {
         managersLock.writeLock().unlock();
      }
   }

   /**
    * Unload without lock.
    * 
    * @param manager
    * @return
    */
   @SuppressWarnings("unchecked")
   private <T> T unloadManager0(Class<T> manager) {
      stopManager0(manager);
      Key managerKey = getManager0(manager);
      if (managerKey != null) {
         // Remove this manager from cache
         managers.remove(managerKey);
         // Remove this manager from module manager so he can not be injected
         getModuleManager().removeAsProperty(managerKey.manager);

         return (T) managerKey.manager;
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T startManager(Class<T> manager) {
      ArgsCheck.notNull("manager", manager);

      managersLock.writeLock().lock();
      try {
         return startManager0(manager);
      } finally {
         managersLock.writeLock().unlock();
      }
   }

   /**
    * Start without lock.
    * 
    * @param manager
    * @return
    */
   @SuppressWarnings({ "unchecked" })
   private <T> T startManager0(Class<T> manager) {
      // Try to load if it is not loaded
      loadManager0(manager);

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
               ModuleUtils.resolveModuleConfig(managerInstance.getClass()));
      }
      managerKey.started = true;
      return (T) managerKey.manager;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T stopManager(Class<T> manager) {
      ArgsCheck.notNull("manager", manager);

      managersLock.writeLock().lock();
      try {
         return stopManager0(manager);
      } finally {
         managersLock.writeLock().unlock();
      }
   }

   /**
    * Stop without lock.
    * 
    * @param manager
    * @return
    */
   @SuppressWarnings("unchecked")
   private <T> T stopManager0(Class<T> manager) {
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
               ModuleUtils.resolveModuleConfig(managerInstance.getClass()));
      }
      managerKey.started = false;
      return (T) managerKey.manager;
   }

   /**
    * Find the instance method that is annotated with {@link Init}.
    * <p>
    * 
    * @param manager
    * @return
    */
   private Method findInitMethod(Class<?> manager) {
      return ManagerUtils.findInstanceInitMethod(manager, getClass());
   }

   /**
    * Find the instance method that is annotated with {@link Stop}.
    * <p>
    * 
    * @param manager
    * @return
    */
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

   /**
    * {@inheritDoc)
    */
   @Override
   public boolean isLoaded(Class<?> manager) {
      managersLock.readLock().lock();
      try {
         return getManager0(manager) != null;
      } finally {
         managersLock.readLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isRegistered(Class<?> manager) {
      managersLock.readLock().lock();
      try {
         return getRegisteredManager0(manager) != null;
      } finally {
         managersLock.readLock().unlock();
      }

   }

   /**
    * {@inheritDoc)
    * 
    */
   @Override
   public boolean isStarted(Class<?> manager) {
      managersLock.readLock().lock();
      try {
         Key key = getManager0(manager);
         if (key != null) {
            return key.started;
         }
         return false;
      } finally {
         managersLock.readLock().unlock();
      }
   }
   
   public void activate(Class<?> activator) {
      
   }

   /**
    * Get a manager key who manager instance is a subtype of the given type or
    * is the same.
    * <p>
    * If the same instance was found it will be returned, otherwise a subtype
    * will be looked for.
    * 
    * @param manager
    * @return
    */
   private Key getManager0(Class<?> manager) {
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
   }

   /**
    * Get a registered manager who is a subtype of the given type or is the
    * same.
    * <p>
    * If the same instance was found it will be returned, otherwise a subtype
    * will be looked for.
    * 
    * @param manager
    * @return
    */
   @SuppressWarnings("unchecked")
   private <T> Class<T> getRegisteredManager0(Class<T> manager) {
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
   }

   /**
    * A small class to keep the loaded managers.
    * 
    * @author Elvis Ligu
    * @version 0.0.1
    * @since 0.0.1
    */
   private static class Key {
      boolean started;
      Object manager;

      public Key(Object manager, boolean started) {
         this.started = started;
         this.manager = manager;
      }
   }

   /**
    * An executor to execute init and stop methods.
    * 
    * @author Elvis Ligu
    * @version 0.0.1
    * @since 0.0.1
    */
   private class Executor extends AbstractMethodConstructorExecutor {

      @Override
      protected ParameterProvider resolveParameterProvider(Class<?> type,
            Map<String, Map<String, Object>> properties) {
         return getModuleManager().getParameterProvider(type);
      }
   }
}
