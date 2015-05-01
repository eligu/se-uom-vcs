/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.module.annotations.Module;
import gr.uom.se.util.module.annotations.ProvideModule;

/**
 * @author Elvis Ligu
 */
public class SingletonModule implements ISingletonModule {

   private static SingletonModule INSTANCE;
   
   private SingletonModule() {}
   
   //@ProvideModule
   public static SingletonModule getInstance() {
      if(INSTANCE == null) {
         INSTANCE = new SingletonModule();
      }
      return INSTANCE;
   }
   
   public static SingletonModule newSingleton() {
      throw new UnsupportedOperationException();
   }
   
   //@ProvideModule
   public SingletonModule getModule() {
      return new SingletonModule();
   }
}

@Module(provider = SingletonModule.class)
interface ISingletonModule {
   public ISingletonModule getModule();
}

