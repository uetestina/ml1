package de.otto.hmac.proxy;


public class ProxyConfiguration {

    private static int port;
    private static String targetHost;
    private static boolean secure;

    private static String user;
    private static String password;
    private static boolean help;
    private static boolean verbose;
    private static boolean daemon;

    private static int sourcePort;

    public static int getSourcePort() {
        return sourcePort;
    }

    public static void setSourcePort(int sourcePort) {
        ProxyConfiguration.sourcePort = sourcePort;
    }

    public static int getPort() {
        return port;
    }

    public static void setPort(int port) {
        ProxyConfiguration.port = port;
    }

    public static boolean isSecure() {
        return secure;
    }

    public static void setSecure(boolean secure) {
        ProxyConfiguration.secure = secure;
    }

    public static String getTargetHost() {
        return targetHost;
    }

    public static void setTargetHost(String targetHost) {
        ProxyConfiguration.targetHost = targetHost;
    }

    public static String getUser() {
        return user;
    }

    public static void setUser(String user) {
        ProxyConfiguration.user = user;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        ProxyConfiguration.password = password;
    }

    public static boolean isHelp() {
        return help;
    }

    public static void setHelp(boolean help) {
        ProxyConfiguration.help = help;
    }

    public static void setVerbose(boolean verbose) {
        ProxyConfiguration.verbose = verbose;
    }

    public static boolean isVerbose() {
        return verbose;
    }

    public static boolean isDaemon() {
        return daemon;
    }

    public static void setDaemon(boolean daemon) {
        ProxyConfiguration.daemon = daemon;
    }
}
