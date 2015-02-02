package gr.uom.se.util.manager;

import gr.uom.se.util.manager.annotations.Init;
import gr.uom.se.util.module.annotations.ProvideModule;

public class DefaultDummyManager implements DummyManager {

   private String name;
   private MainManager manager;
   
   @ProvideModule
   public DefaultDummyManager(MainManager manager) {
      this.manager = manager;
   }
   
   public String getName() {
      return name;
   }
   
   public MainManager getManager() {
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
