package valentinood.checkers.network.jndi;

import javax.naming.Context;
import javax.naming.NamingException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

public final class ConfigurationReader {
    private ConfigurationReader() {}

    private static Properties props = new Properties();

    static {
        Hashtable<String, String> environment= new Hashtable<>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.fscontext.RefFSContextFactory");
        environment.put(Context.PROVIDER_URL,"file:./network/");

        try (InitialDirContextCloseable context = new InitialDirContextCloseable(environment)) {
            Object object = context.lookup("conf.properties");
            props.load(new FileReader(object.toString()));
        } catch (NamingException | IOException e) {
            e.printStackTrace();
        }
    }

    public static String getString(Key key) {
        return props.getProperty(key.getKey());
    }

    public static int getInt(Key key) {
        return Integer.parseInt(getString(key));
    }

    public enum Key {
        HOST("server.host"),
        SERVER_PORT("server.port"),
        RMI_PORT("rmi.port");

        private String key;

        Key(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

}
