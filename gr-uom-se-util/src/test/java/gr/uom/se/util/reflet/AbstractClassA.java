/**
 * 
 */
package gr.uom.se.util.reflet;

/**
 * @author Elvis
 *
 */
public abstract class AbstractClassA implements InterfaceA {

   public AbstractClassA() {
   }
   
   private AbstractClassA(String arg) {
      
   }
   
   protected String name() {
      return null;
   }
   
   void packageMethod(String arg) {
   }
   
   protected static class AbstractClassANested {
      private void nestedMethod(){}
      void packageNestedMethod() {}
   }
}
