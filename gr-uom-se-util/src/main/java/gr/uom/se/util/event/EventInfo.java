/**
 * 
 */
package gr.uom.se.util.event;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.Date;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class EventInfo {

   private Object source;
   private Date startDate;
   
   public EventInfo() {
   }
   
   public EventInfo(Object source, Date date) {
      ArgsCheck.notNull("source", source);
      this.source = source;
      this.startDate = date;
   }
   
   public Object getSource() {
      return source;
   }
   
   public Date getStartDate() {
      return startDate;
   }
}
