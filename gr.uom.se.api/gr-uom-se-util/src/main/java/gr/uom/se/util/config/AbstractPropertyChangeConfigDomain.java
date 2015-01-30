/**
 * 
 */
package gr.uom.se.util.config;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * This is a configuration domain with support for property change/
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class AbstractPropertyChangeConfigDomain extends
      AbstractConfigDomain {

   private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(
         this);

   public AbstractPropertyChangeConfigDomain(String name) {
      super(name);
   }

   /**
    * Set a property to this domain and notify listeners for the change.
    * <p>
    */
   @Override
   public void setProperty(String name, Object val) {
      Object oldVal = super.getProperty(name);
      if(equals(oldVal, val)) {
         return;
      }
      super.setProperty(name, val);
      changeSupport.firePropertyChange(name, oldVal, val);
   }

   private static boolean equals(Object o1, Object o2) {
      if(o1 != null && o2 != null) {
         return o1.equals(o2);
      }
      return (o1 == null && o2 == null);
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
