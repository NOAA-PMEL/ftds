import ucar.nc2.*;

import java.io.*;
import java.util.*;

public class Avg {

    public static void main(String[] args) {
        try {
            NetcdfFiles.registerIOProvider("sdig.ftds.iosp.FerretIOServiceProvider");
            NetcdfFile my_file = NetcdfFiles.open("C:\\Users\\schweitzer\\Documents\\IdeaProjects\\ftds\\src\\test\\java\\coads_avg.jnl", null);
            List<Variable> v = my_file.getVariables();
            for (int i = 0; i < v.size(); i++) {
                Variable var = v.get(i);
                System.out.println(var.getShortName());
            }
        } catch (IllegalAccessException | InstantiationException | IOException | ClassNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

}
