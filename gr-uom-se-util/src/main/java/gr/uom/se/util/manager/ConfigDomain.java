/**
 * 
 */
package gr.uom.se.util.manager;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public interface ConfigDomain {

   String getName();
   
   <T> T getProperty();
   
   void setProperty(String name, Object val);
   
   void save();
}
