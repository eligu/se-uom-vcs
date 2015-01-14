/**
 * 
 */
package gr.uom.se.filter;

/**
 * @author Elvis Ligu
 *
 */
public interface Filter<T> {
   
   boolean accept(T t);
}
