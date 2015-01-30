package gr.uom.se.util.manager;

import gr.uom.se.util.module.annotations.ProvideModule;

public class DBConnection {

   AbstractManager manager;
   
   ConnectionConfig config;
   
   @ProvideModule
   public DBConnection(AbstractManager mainManager, ConnectionConfig config) {
      this.manager = mainManager;
      this.config = config;
   }
   
   public ConnectionConfig getConfig() {
      return config;
   }
   public AbstractManager getManager() {
      return manager;
   }
}
