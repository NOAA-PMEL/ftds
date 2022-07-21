package sdig.ftds.iosp;

import java.util.*;

/**
 * A configuration POJO that will be read (and written) by Jackson to keep track of the configuration
 * necessary to run (Py)Ferret in service of the IOSP.
 */
public class FerretConfig {
    private long timeLimit;
    private String python;

    public String getPython() {
        return python;
    }

    public void setPython(String python) {
        this.python = python;
    }

    private HashMap<String, List<String>> environment;
    private List<String> args;

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public HashMap<String, List<String>> getEnvironment() {
        return environment;
    }

    public void addScriptDir(String scriptDir) {
        List<String> fgo = getEnvironment().get("FER_GO");
        fgo.add(scriptDir);
    }

    public void setEnvironment(HashMap<String, List<String>> environment) {
        this.environment = environment;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }

    public HashMap<String, String> getEnvironmentAsStrings() {
        HashMap<String, String> env = new HashMap<>();
        HashMap<String, List<String>> map = getEnvironment();
        for (Iterator<String> keys = map.keySet().iterator(); keys.hasNext(); ) {
            String key = keys.next();
            List<String> values = map.get(key);
            String var_value = values.get(0);
            for (int i = 1; i < values.size(); i++) {
                var_value = var_value + ":" + values.get(i);
            }
            env.put(key, var_value);
        }
        return env;
    }
}
