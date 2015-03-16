/**
 * 
 */
package gr.uom.se.util.module;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author Elvis Ligu
 */
public class MyParameterProvider implements ParameterProvider{

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T getParameter(Class<T> parameterType, Annotation[] annotations,
         Map<String, Map<String, Object>> properties) {
      return null;
   }

}
