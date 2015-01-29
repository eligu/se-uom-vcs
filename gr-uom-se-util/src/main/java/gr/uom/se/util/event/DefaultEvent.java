/**
 * 
 */
package gr.uom.se.util.event;

import gr.uom.se.util.validation.ArgsCheck;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class DefaultEvent implements Event {

   private EventType type;
   private EventInfo info;
   
   public DefaultEvent(EventType type, EventInfo info) {
      ArgsCheck.notNull("type", type);
      ArgsCheck.notNull("info", info);
      this.type = type;
      this.info = info;
   }
   
   /** {@inheritDoc}
    */
   @Override
   public EventType getType() {
      return type;
   }

   /**
    *  {@inheritDoc}
    */
   @Override
   public EventInfo getInfo() {
      return info;
   }
}
