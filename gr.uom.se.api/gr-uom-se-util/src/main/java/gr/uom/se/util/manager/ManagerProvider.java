/**
 * 
 */
package gr.uom.se.util.manager;

import java.lang.annotation.Annotation;
import java.util.Map;

import gr.uom.se.util.module.ParameterProvider;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ManagerProvider implements ParameterProvider {

   /**
    *  {@inheritDoc}
    */
   @Override
   public <T> T getParameter(Class<T> parameterType, Annotation[] annotations,
         Map<String, Map<String, Object>> properties) {
      // TODO Auto-generated method stub
      return null;
   }

}
