package sdig.ftds.iosp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.NetcdfFile;
import ucar.unidata.io.RandomAccessFile;

import java.io.IOException;

public class FerretNetcdfFile extends NetcdfFile {
    private final static Logger log = LoggerFactory.getLogger(FerretNetcdfFile.class.getName());

    /**
     * Construct a new netCDF file from the RandomAccessFile (which contains the resulting Ferret script) and TDS request URI that was used to
     * create the script.
     * @param raf
     * @param location
     * @throws IOException
     */
    public FerretNetcdfFile(RandomAccessFile raf, String location) throws IOException {
        super(new FerretIOServiceProvider(), raf, location, null);
        log.debug("Constructed new netCDF file at: "+location);
        finish();
    }
}
