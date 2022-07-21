# F-TDS

This is an implementation of the Java netCDF I/O Service Provider spec which
enables netCDF Java clients to read Ferret journal files which open data sets
and use Ferret scripting language to define virtual data variables.

These variables will show up virtually when the netCDF Java client reads the
header (the equivalent of reading the contents of ncdump -c). When the client
actually reads data, the Ferret scripts underlying the virtual variable 
definition will run and the data necessary to fulfill the read request will
get generated and cached.

This repository also includes an implementation of the thredds.DatasetSource
interface which allows the ISOP to be plugged into a THREDDS server to
dynamically build virtual variables from information on the THREDDS URL.

The ISOP can be plugged into a TDS and virtual variables can be defined by
configuring the .jnl Ferret script files as data sets in the TDS 
configuration catalogs.

#### Installation Instructions
 ... to follow after implementation is complete

#### Legal Disclaimer
*This repository is a software product and is not official communication
of the National Oceanic and Atmospheric Administration (NOAA), or the
United States Department of Commerce (DOC).  All NOAA GitHub project
code is provided on an 'as is' basis and the user assumes responsibility
for its use.  Any claims against the DOC or DOC bureaus stemming from
the use of this GitHub project will be governed by all applicable Federal
law.  Any reference to specific commercial products, processes, or services
by service mark, trademark, manufacturer, or otherwise, does not constitute
or imply their endorsement, recommendation, or favoring by the DOC.
The DOC seal and logo, or the seal and logo of a DOC bureau, shall not
be used in any manner to imply endorsement of any commercial product
or activity by the DOC or the United States Government.*
