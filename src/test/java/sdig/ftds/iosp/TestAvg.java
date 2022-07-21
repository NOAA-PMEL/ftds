package sdig.ftds.iosp;

import org.junit.jupiter.api.*;
import ucar.nc2.*;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestAvg {
    @Test
    void testAvg() throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException {
        NetcdfFiles.registerIOProvider("sdig.ftds.iosp.FerretIOServiceProvider");
        NetcdfFile my_file = NetcdfFiles.open("C:\\Users\\schweitzer\\Documents\\IdeaProjects\\ftds\\src\\test\\java\\sdig\\ftds\\iosp\\coads_avg.jnl", null);
        List<Variable> v = my_file.getVariables();
        String test_name = "";
        for (int i = 0; i < v.size(); i++) {
            Variable var = v.get(i);
            String sn = var.getShortName();
            if (sn.contains("_avg")) {
                test_name = sn;
            }
        }
        assertEquals("sst_avg", test_name);
    }
}