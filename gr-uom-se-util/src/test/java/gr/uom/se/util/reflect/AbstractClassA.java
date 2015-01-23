/**
 * 
 */
package gr.uom.se.util.reflect;

/**
 * @author Elvis
 *
 */
public abstract class AbstractClassA implements InterfaceA {

   public AbstractClassA() {
   }
   
   @SuppressWarnings("unused")
   private AbstractClassA(String arg) {
      
   }
   
   protected String name() {
      return null;
   }
   
   void packageMethod(String arg) {
   }
   
   protected static class AbstractClassANested {
      @SuppressWarnings("unused")
      private void nestedMethod(){}
      void packageNestedMethod() {}
   }
}
