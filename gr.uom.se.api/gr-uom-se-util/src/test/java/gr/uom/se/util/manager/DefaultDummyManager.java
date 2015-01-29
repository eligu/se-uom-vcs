package gr.uom.se.util.manager;

import gr.uom.se.util.manager.annotations.Init;
import gr.uom.se.util.module.annotations.Property;
import gr.uom.se.util.module.annotations.ProvideModule;

@Property(domain = ManagerConstants.DEFAULT_DOMAIN, name = "dummyManager")
public class DefaultDummyManager implements DummyManager {

   private String name;
   private AbstractManager manager;
   
   @ProvideModule
   public DefaultDummyManager(AbstractManager manager) {
      this.manager = manager;
   }
   
   public String getName() {
      return name;
   }
   
   public AbstractManager getManager() {
      return manager;
   }
   
   public void setName(String name) {
      this.name = name;
   }
   
   @Init
   public void init() {
      System.out.println("Initializing dummy manager");
   }
}
