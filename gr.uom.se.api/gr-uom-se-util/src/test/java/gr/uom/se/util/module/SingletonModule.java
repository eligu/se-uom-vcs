/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.module.annotations.Module;

/**
 * @author Elvis Ligu
 */
public class SingletonModule implements ISingletonModule {

   private static SingletonModule INSTANCE;
   
   private SingletonModule() {}
   
   public static SingletonModule getInstance() {
      if(INSTANCE == null) {
         INSTANCE = new SingletonModule();
      }
      return INSTANCE;
   }
   
   public ISingletonModule getModule() {
      return new SingletonModule();
   }
}

@Module(provider = SingletonModule.class)
interface ISingletonModule {
   public ISingletonModule getModule();
}

