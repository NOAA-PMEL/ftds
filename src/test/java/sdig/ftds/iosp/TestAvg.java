package sdig.ftds.iosp;

import org.junit.jupiter.api.*;
import ucar.ma2.*;
import ucar.nc2.*;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestAvg {

    @BeforeAll
    void registerIOSP() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        NetcdfFiles.registerIOProvider("sdig.ftds.iosp.FerretIOServiceProvider");
    }
    @Test
    void testAvg() throws IOException {
        NetcdfFile my_file = NetcdfFiles.open("src/test/java/sdig/ftds/iosp/coads_avg.jnl", null);
        List<Variable> v = my_file.getVariables();
        String test_name = "";
        Variable test_variable = null;
        for (int i = 0; i < v.size(); i++) {
            Variable var = v.get(i);
            String sn = var.getShortName();
            if (sn.contains("_avg")) {
                test_name = sn;
                test_variable = var;
            }
        }
        assert test_variable != null;
        assertEquals(2, test_variable.getDimensions().asList().size());
        assertEquals("sst_avg", test_name);
    }
    @Test
    void testValue() throws IOException, InvalidRangeException {
        NetcdfFile my_file = NetcdfFiles.open("src/test/java/sdig/ftds/iosp/coads_avg.jnl", null);
        Variable avg = my_file.findVariable("sst_avg");
        Range r1 = new Range("COADSX", 40, 50);
        Range r2 = new Range("COADSY", 30, 40);
        List<Range> ranges = new ArrayList<>();
        ranges.add(r1);
        ranges.add(r2);
        Array a = avg.read(ranges);
        // incorrect assertions until we can get the correct values
        assertEquals(a.getFloat(0), 28.4456310, .01);
        assertEquals(a.getDouble(19), 2.0d);
        System.out.println(a.getDouble(0));
        System.out.println(a.getDouble(19));
    }
}