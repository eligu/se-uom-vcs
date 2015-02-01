/**
 * 
 */
package gr.uom.se.util.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ConfigDomainListener implements PropertyChangeListener {

   Map<String, Object> changed = new HashMap<>();
   boolean notified = false;
   
   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      String name = evt.getPropertyName();
      Object value = evt.getNewValue();
      changed.put(name, value);
      notified = true;
   }
}
