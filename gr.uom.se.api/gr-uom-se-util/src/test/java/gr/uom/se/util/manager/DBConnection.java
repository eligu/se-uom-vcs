package gr.uom.se.util.manager;

public interface DBConnection {

   public abstract ConnectionConfig getConfig();

   public abstract MainManager getManager();
}