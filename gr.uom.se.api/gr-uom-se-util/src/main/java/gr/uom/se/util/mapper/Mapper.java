/**
 * 
 */
package gr.uom.se.util.mapper;

/**
 * A mapper is an instance who is capable of mapping a given value (source) to a
 * given type.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public interface Mapper {

   /**
    * Map the given source to the given type {@code to}.
    * <p>
    * 
    * @param source
    *           the source to map
    * @param to
    *           the new value type
    * @return a mapped from {@code source} value of type {@code to}
    */
   <T, S> T map(S source, Class<T> to);
}
