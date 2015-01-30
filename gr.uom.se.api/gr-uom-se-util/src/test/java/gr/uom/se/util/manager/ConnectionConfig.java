/**
 * 
 */
package gr.uom.se.util.manager;

import gr.uom.se.util.module.annotations.Property;
import gr.uom.se.util.module.annotations.ProvideModule;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ConnectionConfig {

   int port;
   
   String username;

   String password;
   
   String jdbcDriver;
   
   @ProvideModule
   public ConnectionConfig(
         @Property(domain = "dbconfig", name = "port") int port, 
         @Property(domain = "dbconfig", name = "username") String username,
         @Property(domain = "dbconfig", name = "password") String password,
         @Property(domain = "dbconfig", name = "jdbcDriver") String driver) {
      
      this.password = password;
      this.port = port;
      this.username = username;
      this.jdbcDriver = driver;
   }

   public int getPort() {
      return port;
   }

   public String getUsername() {
      return username;
   }

   public String getPassword() {
      return password;
   }

   public String getJdbcDriver() {
      return jdbcDriver;
   }
}
