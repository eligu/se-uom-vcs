package gr.uom.se.filter;

public class NullFilter<T> implements Filter<T> {
   public boolean accept(T t) {
      return true;
   }
}