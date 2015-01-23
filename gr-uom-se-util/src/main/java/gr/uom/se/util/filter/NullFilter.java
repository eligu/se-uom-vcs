package gr.uom.se.util.filter;

public class NullFilter<T> implements Filter<T> {
   public boolean accept(T t) {
      return true;
   }
}