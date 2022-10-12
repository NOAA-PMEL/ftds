package sdig.ftds.iosp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDatasets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
public class FerretDataSource implements thredds.servlet.DatasetSource  {
    private final static Logger log = LoggerFactory.getLogger(FerretDataSource.class.getName());

    @Override
    public boolean isMine(HttpServletRequest req) {
        StringBuffer urlbuf = req.getRequestURL();
        String url;
        try {
            url = URLDecoder.decode(urlbuf.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return false;
        }
        if ( url.contains("_expr_") && FerretCommands.containsCommand(url)) {
            return true;
        }
        return false;    }

    @Override
    public NetcdfFile getNetcdfFile(HttpServletRequest req, HttpServletResponse res) throws IOException {
        StringBuffer jnl = new StringBuffer("DEFINE ALIAS letdeq1 let/d=1\nDEFINE ALIAS ATTRCMD SET ATT/LIKE=\n");

        StringBuffer urlbuf = req.getRequestURL();
        String url = URLDecoder.decode(urlbuf.toString(), "UTF-8");
        if ( url.length() == urlbuf.length() ) {
            // Length unchanged, decoding was unnecessary.
            url = urlbuf.toString();
        }

        // Are there still escaped curly braces?
        // Sometimes the client encodes a URL that is already encoded.
        while ( url.contains("%7B") || url.contains("%7D") ) {
            url = URLDecoder.decode(url, "UTF-8");
        }

        log.debug("building netcdf file from "+url);

        String base = getBaseURL(url);
        ArrayList<String> expressions = getExpressions(url);

        FerretIOServiceProvider fiosp = new FerretIOServiceProvider();

        String data_path = fiosp.getDataDir();


        if ( expressions.size() == 2 ) {
            if ( !expressions.get(0).trim().equals("") ) {
                List<String> expression_one = new ArrayList<String>();
                String od = URLDecoder.decode(expressions.get(0), "UTF-8");
                String[] urls = od.split(",");
                for ( int i = 0; i < urls.length; i++ ) {
                    String [] cmds;
                    if ( urls[i].contains("_cr_") ) {
                        cmds = urls[i].split("_cr_");
                        for (int j = 0; j < cmds.length; j++) {
                            expression_one.add(cmds[j]);
                        }
                    } else if ( urls[i].contains(";") ) {
                        cmds = urls[i].split(";");
                        for (int j = 0; j < cmds.length; j++) {
                            expression_one.add(cmds[j]);
                        }
                    } else {
                        expression_one.add(urls[i]);
                    }
                }
                for ( int i = 0; i < expression_one.size(); i++ ) {
                    String dataURL = URLDecoder.decode(expression_one.get(i), "UTF-8");
                    if ( FerretCommands.containsCommand(dataURL) ) {
                        // This is actually a command, not a data URL.  Treat it as such...
                        if ( !FerretCommands.containsForbiddenCommand(dataURL) ) {
                            jnl.append(dataURL+"\n");
                        }
                    } else {
                        if ( !FerretCommands.containsForbiddenCommand(dataURL) ) {
                            int ds = i + 2;
                            jnl.append("use \""+dataURL+"\"\n");
                        }
                    }
                }
            }
            // Everything in the first bracket goes before the dataset.
            jnl.append("use \""+base+"\"\n");

            if ( !expressions.get(1).trim().equals("") ) {

                // Decode the expression because it might contain an encode URL.
                String expr_two =  URLDecoder.decode(expressions.get(1), "UTF-8");
                String [] cmds;
                if ( expr_two.contains("_cr_") ) {
                    cmds = expr_two.split("_cr_");
                } else {
                    cmds = expr_two.split(";");
                }
                for ( int i = 0; i < cmds.length; i++ ) {
                    cmds[i] = cmds[i].replaceAll("_q-t_", "\"");
                    if ( !FerretCommands.containsForbiddenCommand(cmds[i])) {
                        jnl.append(cmds[i].replace("_qt_","\"")+"\n");
                    }
                }
            }
        } else if ( expressions.size() == 1 ) {
            if ( !expressions.get(0).trim().equals("") ) {
                String od = URLDecoder.decode(expressions.get(0), "UTF-8");
                String[] urls =od.split(",");
                for ( int i = 0; i < urls.length; i++ ) {
                    String dataURL = URLDecoder.decode(urls[i], "UTF-8");
                    if ( FerretCommands.containsCommand(dataURL) ) {
                        // This is actually a command, not a data URL.  Treat it as such...
                        if ( !FerretCommands.containsForbiddenCommand(dataURL) ) {
                            jnl.append(dataURL+"\n");
                        }
                    } else {
                        if ( !FerretCommands.containsForbiddenCommand(dataURL) ) {
                            int ds = i + 2;
                            jnl.append("use \""+dataURL.replace("_qt_","\"")+"\"\n");
                        }
                    }
                }
            }
        } else if ( expressions.size() == 0 ) {
            throw new IOException("Expression parsing failed for this URL. "+url+" Now expressions found inside the curly brackets.");
        } else if ( expressions.size() > 2 ) {
            throw new IOException("Expression parsing failed for this URL. "+url+" Too many expressions found.");
        }

        String key = JDOM2Utils.MD5Encode(jnl.toString());

        String script = data_path+ File.separator+"data_expr_"+key+".jnl";

        log.debug("using "+script+" for temporary script file.");
        if ( script.contains("../") || script.contains("/..") ) {
            throw new IOException("Illegal script file name.");
        }
        File script_file = new File(script);
        if (!script_file.exists()) {
            PrintWriter data_script = new PrintWriter(new FileOutputStream(script_file));
            data_script.println(jnl);
            data_script.close();
        }

        NetcdfFile ncfile = NetcdfDatasets.openFile(script, null);
        return ncfile;
    }
    /**
     * Return the portion of the URL that comes before the expression (_expr_).
     * @param url the full URL from which to extract the base.
     * @return the base portion of the URL
     */
    private String getBaseURL(String url) {
        return url.substring(0, url.indexOf("_expr_"));
    }
    /**
     *
     * @param url
     * @return An ArrayList of the expressions (the strings contained in the curly braces) in the URL.  The first expression
     * (if it exists) is a comma separated list of OPeNDAP or local data sets.  The second expression (if it exists) is a semi-colon
     * separated list of Ferret commands.
     */
    private ArrayList<String> getExpressions(String url) {
        ArrayList<String> expressions = new ArrayList<String>();
        String[] tokens = null;
        if ( url.contains("_expr_")) {
            url = url.substring(url.indexOf("_expr_")+6, url.length());
            tokens = url.split("\\{");

            for ( int i = 1; i < tokens.length; i++) {
                log.debug("found expression: "+tokens[i].substring(0,tokens[i].indexOf("}")));
                expressions.add(tokens[i].substring(0,tokens[i].indexOf("}")));
            }
        }
        return expressions;
    }
}
