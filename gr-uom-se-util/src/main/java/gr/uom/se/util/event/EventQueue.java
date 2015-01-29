/**
 * 
 */
package gr.uom.se.util.event;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public interface EventQueue {

   public void trigger(Event event);
   public void addListener(EventType type, EventListener listener);
   public void removeListener(EventType type, EventListener listener);
   public void addListener(EventListener listener);
   public void removeListener(EventListener listener);
}
