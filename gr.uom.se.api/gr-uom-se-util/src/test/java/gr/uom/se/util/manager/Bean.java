package gr.uom.se.util.manager;

import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.module.ModuleManager;
import gr.uom.se.util.module.annotations.Property;
import gr.uom.se.util.module.annotations.ProvideModule;

public class Bean {
   
      @Property(domain = ManagerConstants.DEFAULT_DOMAIN, name = "moduleManager")
      ModuleManager moduleManager;
      
      @Property(domain = ManagerConstants.DEFAULT_DOMAIN, name = "mainManager")
      AbstractManager mainManager;
      
      @Property(domain = ManagerConstants.DEFAULT_DOMAIN, name = "configManager")
      ConfigManager configManager;

      @ProvideModule
      public Bean(ModuleManager moduleManager, AbstractManager mainManager,
            ConfigManager configManager) {
         this.mainManager = mainManager;
         this.moduleManager = moduleManager;
         this.configManager = configManager;
      }
   }