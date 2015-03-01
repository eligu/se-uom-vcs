/**
 * 
 */
package gr.uom.se.util.config;

import gr.uom.se.util.validation.ArgsCheck;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This is a configuration domain with support for property change.
 * <p>
 * Subclasses of this class will be able to listen on property changes, if they
 * register a change listener, for a given property or for all properties.
 * <p>
 * Note that, this implementation adds an overhead to the maps of the properties
 * because each change ({@link #setProperty(String, Object)} will lock the
 * entire map down as long as the change and the listener of the change are
 * executing. Even more the reading will be suspended until the change is being
 * performed. To avoid starvation and other racing condition issues the
 * listeners must finish their job as soon as possible.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class AbstractPropertyChangeConfigDomain extends
      AbstractConfigDomain {

   /**
    * Provider of property change support.
    * <p>
    */
   private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(
         this);

   /**
    * To ensure that property writes doesn't overlap with reads.
    * <p>
    * Also property writes will be serialized.
    */
   private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

   public AbstractPropertyChangeConfigDomain(String name) {
      super(name);
   }

   /**
    * {@inheritDoc}
    * <p>
    * Set a property to this domain and notify listeners for the change.
    * <p>
    * This method will first check if the given property is already there and
    * has the same value as the new value. If not then it will fire a property
    * change event. This method is thread safe in that if a caller tries to
    * change a mapping for a given key no other caller can do that until the
    * listener is notified. However this will impose additional overhead because
    * each change to the properties will be serialized and each read from
    * properties will wait if a change is being performed. An other risk is that
    * listeners must finish their jobs as soon as possible, otherwise it will
    * lock the entire map to be locked down and readings will be impossible.
    */
   @Override
   public void setProperty(String name, Object val) {
      lock.writeLock().lock();
      try {
         setThis(name, val);
      } finally {
         lock.writeLock().unlock();
      }
   }

   private void setThis(String name, Object val) {
      Object oldVal = set(name, val);
      if (oldVal == null && val == null) {
         return;
      }
      changeSupport.firePropertyChange(name, oldVal, val);
   }

   /**
    * Set the given properties to this domain and notify listeners for the
    * changes.
    * <p>
    * This method works the same as calling in a for loop
    * {@link #setProperty(String, Object)} except that all the property changes
    * will be atomic. If clients needs to change a lot of properties at once but
    * ensure that no other thread change one of their properties before their
    * operation has completed, should use this method, as this will ensure that
    * all listeners will be notified by the client's thread.
    * <p>
    * The properties parameter must not be null and must not be changed by
    * others while this operation is executing.
    * 
    * @param properties
    */
   public void setProperties(Map<String, Object> properties) {
      ArgsCheck.notNull("properties", properties);
      lock.writeLock().lock();
      try {
         for (String name : properties.keySet()) {
            setThis(name, properties.get(name));
         }
      } finally {
         lock.writeLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    * <p>
    * This will return a copy of all properties within this domain. The
    * operation is atomic and ensures that no other thread can change the
    * properties of this domain before this is ends.
    */
   @Override
   public Map<String, Object> getProperties() {
      lock.readLock().lock();
      try {
         Map<String, Object> properties = new HashMap<>();
         properties.putAll(this.properties);
         return properties;
      } finally {
         lock.readLock().unlock();
      }
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void merge(ConfigDomain domain) {
      ArgsCheck.notNull("domain", domain);
      lock.writeLock().lock();
      try {
         Map<String, Object> newProperties = domain.getProperties();
         for(String name : newProperties.keySet()) {
            this.properties.put(name, newProperties.get(name));
         }
      } finally {
         lock.writeLock().unlock();
      }
   }
   /**
    * {@inheritDoc}
    * <p>
    * This will return a copy of all properties starting with the given prefix.
    * The operation is atomic and ensures that no other thread can change the
    * properties of this domain before this is ends.
    */
   @Override
   public Map<String, Object> getPropertiesWithPrefix(String prefix) {
      ArgsCheck.notNull("prefix", prefix);
      lock.readLock().lock();
      try {
         Map<String, Object> properties = new HashMap<>();
         for (String name : this.properties.keySet()) {
            if (name.startsWith(prefix)) {
               properties.put(name, this.properties.get(name));
            }
         }
         return properties;
      } finally {
         lock.readLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    * <p>
    * This will return a copy of all properties ending with the given suffix.
    * The operation is atomic and ensures that no other thread can change the
    * properties of this domain before this is ends.
    */
   @Override
   public Map<String, Object> getPropertiesWithSuffix(String suffix) {
      ArgsCheck.notNull("suffix", suffix);
      lock.readLock().lock();
      try {
         Map<String, Object> properties = new HashMap<>();
         for (String name : this.properties.keySet()) {
            if (name.endsWith(suffix)) {
               properties.put(name, this.properties.get(name));
            }
         }
         return properties;
      } finally {
         lock.readLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    * <p>
    * This method will lock down the calling thread if there are other threads
    * that makes changes to properties. Especially if the listener of the
    * property that is being changed is busy it will cause the caller to wait
    * for a long time.
    */
   @Override
   public Object getProperty(String name) {
      lock.readLock().lock();
      try {
         return super.getProperty(name);
      } finally {
         lock.readLock().unlock();
      }
   }

   /**
    * Add a change listener to listen for changes of properties.
    * 
    * @param listener
    */
   public void addChangeListener(PropertyChangeListener listener) {
      this.changeSupport.addPropertyChangeListener(listener);
   }

   /**
    * Add a change listener for the given property.
    * <p>
    * 
    * @param propertyName
    *           the name of the property to listen for changes
    * @param listener
    *           the listener that will receive signals
    */
   public void addChangeListener(String propertyName,
         PropertyChangeListener listener) {
      this.changeSupport.addPropertyChangeListener(propertyName, listener);
   }

   /**
    * Remove a change listener.
    * <p>
    * 
    * @param listener
    */
   public void removeChangeListener(PropertyChangeListener listener) {
      this.changeSupport.removePropertyChangeListener(listener);
   }

   /**
    * Remove a change listener for the specified property.
    * <p>
    * 
    * @param propertyName
    * @param listener
    */
   public void removeChangeListener(String propertyName,
         PropertyChangeListener listener) {
      this.changeSupport.removePropertyChangeListener(propertyName, listener);
   }
}
