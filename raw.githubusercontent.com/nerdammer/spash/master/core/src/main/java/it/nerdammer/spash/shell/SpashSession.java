package it.nerdammer.spash.shell;

/**
 * Contains all information related to the user's session.
 *
 * @author Nicola Ferraro
 */
public class SpashSession {

    /**
     * The default working directory.
     */
    private static final String DEFAULT_WORKING_DIR = "/";

    /**
     * The user connected to this session.
     */
    private String user;

    /**
     * The current working directory.
     */
    private String workingDir = DEFAULT_WORKING_DIR;

    public SpashSession(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    @Override
    public String toString() {
        return "SpashSession{" +
                "user='" + user + '\'' +
                '}';
    }
}
