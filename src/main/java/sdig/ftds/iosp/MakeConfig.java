package sdig.ftds.iosp;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MakeConfig {
    static ObjectMapper mapper = new ObjectMapper();
    public static void main(String[] args) {
        HashMap<String, List<String>> environment = new HashMap<>();
        environment.put("FER_DIR", Arrays.asList("/joe", "/fro"));
        environment.put("FER_DATA", Arrays.asList("/bill", "/fill"));
        FerretJSONConfig config = new FerretJSONConfig();
        config.setEnvironment(environment);
        config.setTimeLimit(19203);
        try {
            mapper.writeValue(new File("auto_config.json"), config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
