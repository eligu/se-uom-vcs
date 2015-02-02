/**
 * 
 */
package gr.uom.se.util.manager;

import gr.uom.se.util.module.annotations.ProvideModule;
import gr.uom.se.util.validation.ArgsCheck;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class DBManagerImp implements DBManager {

   private DBConnection conn;

   /**
    * The connection will be injected by loaded, which will get it by parameter
    * provider. Parameter provider will load the connection based on
    * configuration.
    * 
    * @param conn
    */
   @ProvideModule
   public DBManagerImp(DBConnection conn) {
      ArgsCheck.notNull("conn", conn);
      this.conn = conn;
   }

   @Override
   public DBConnection getConnection() {
      return conn;
   }
}
