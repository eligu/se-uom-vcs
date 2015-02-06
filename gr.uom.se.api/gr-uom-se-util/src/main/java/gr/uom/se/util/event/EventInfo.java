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

   private Object description;
   private Date startDate;
   
   public EventInfo() {
   }
   
   public EventInfo(Object description, Date date) {
      ArgsCheck.notNull("description", description);
      this.description = description;
      this.startDate = date;
   }
   
   public Object getDescription() {
      return description;
   }
   
   public Date getStartDate() {
      return startDate;
   }
}
