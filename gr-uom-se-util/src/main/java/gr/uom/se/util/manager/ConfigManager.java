package gr.uom.se.util.manager;
/**
 *
 * @author Elvis Ligu
 */
public interface ConfigManager {
    
    public <T> T getProperty(String name);
    
    public void setProperty(String name, Object value);
    
    public <T> T getProperty(String domain, String name);
    
    public void setProperty(String domain, String name, Object value);
    
    public void saveDomain(String domain);
    
    public void loadDomain(String domain);
    
    public void loadDomain(Class<?> domain);
    
    public void createDomain(String domain);
}
