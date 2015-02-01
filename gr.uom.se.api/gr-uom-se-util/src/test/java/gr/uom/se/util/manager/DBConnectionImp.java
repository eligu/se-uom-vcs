package gr.uom.se.util.manager;

import gr.uom.se.util.module.annotations.ProvideModule;

public class DBConnectionImp implements DBConnection {

   MainManager manager;

   ConnectionConfig config;

   /**
    * This constructor must be used by the module loader to load the connection.
    * If used with a module manager without the presence of an AbstractMainManager it
    * will not work, because the default module loader can not load an instance
    * of AbstractMainManager. However when the module manager is retrieved from
    * AbstractMainManager it will provide an instance of abstract manager (the manager
    * instance) and will create an instance of ConnectionConfig which will be
    * injected the values from a config manager. That is possible because
    * the default parameter provider is replaced in AbstractMainManager and will
    * provide any registered manager instance if the parameter is a super type
    * of the registered managers.
    */
   @ProvideModule
   public DBConnectionImp(MainManager mainManager, ConnectionConfig config) {
      this.manager = mainManager;
      this.config = config;
   }

   /* {@inheritDoc)
    * @see gr.uom.se.util.manager.DBConnection#getConfig()
    */
   @Override
   public ConnectionConfig getConfig() {
      return config;
   }

   /* {@inheritDoc)
    * @see gr.uom.se.util.manager.DBConnection#getManager()
    */
   @Override
   public MainManager getManager() {
      return manager;
   }
}
