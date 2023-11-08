package valentinood.checkers.util;

public final class OSUtils {
    private OSUtils() {
    }

    public enum OSType {
        WINDOWS,
        OTHER
    }

    private static final OSType os;

    static {
        String property = System.getProperty("os.name");

        if (property.startsWith("Windows"))
            os = OSType.WINDOWS;
        else
            os = OSType.OTHER;
    }

    public static OSType getOS() {
        return os;
    }


}
