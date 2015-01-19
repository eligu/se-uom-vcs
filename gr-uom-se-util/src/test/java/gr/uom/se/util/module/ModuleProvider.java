package gr.uom.se.util.module;

import gr.uom.se.util.module.annotations.Property;
import gr.uom.se.util.module.annotations.ProvideModule;

public class ModuleProvider {

   public ModuleProvider() {
   }
   
   @ProvideModule
   public IModule getModule(@Property(domain = "default", name = "var") int var) {
      Module module = new Module();
      module.setVar(var);
      return module;
   }
}
