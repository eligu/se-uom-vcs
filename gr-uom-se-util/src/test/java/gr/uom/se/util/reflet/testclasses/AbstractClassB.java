/**
 * 
 */
package gr.uom.se.util.reflet.testclasses;

import gr.uom.se.util.reflet.AbstractClassA;

/**
 * @author Elvis
 *
 */
public abstract class AbstractClassB extends AbstractClassA implements InterfaceB {

   @Override
   public Integer methodB(int num, int radix) {
      return null;
   }
   
   protected abstract Class<?> getType();
}
