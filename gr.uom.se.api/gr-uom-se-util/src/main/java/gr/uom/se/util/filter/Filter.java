/**
 * 
 */
package gr.uom.se.util.filter;

/**
 * @author Elvis Ligu
 *
 */
public interface Filter<T> {
   
   boolean accept(T t);
}
