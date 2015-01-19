/**
 * 
 */
package gr.uom.se.util.manager;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class PropertyConfigDomain implements ConfigDomain {

   private String name;
   
   /**
    * 
    */
   public PropertyConfigDomain(String name, String fileName) {
      
   }

   /* {@inheritDoc)
    * @see gr.uom.se.util.manager.ConfigDomain#getName()
    */
   @Override
   public String getName() {
      // TODO Auto-generated method stub
      return null;
   }

   /* {@inheritDoc)
    * @see gr.uom.se.util.manager.ConfigDomain#getProperty()
    */
   @Override
   public <T> T getProperty() {
      // TODO Auto-generated method stub
      return null;
   }

   /* {@inheritDoc)
    * @see gr.uom.se.util.manager.ConfigDomain#setProperty(java.lang.String, java.lang.Object)
    */
   @Override
   public void setProperty(String name, Object val) {
      // TODO Auto-generated method stub

   }

   /* {@inheritDoc)
    * @see gr.uom.se.util.manager.ConfigDomain#save()
    */
   @Override
   public void save() {
      // TODO Auto-generated method stub

   }
}
