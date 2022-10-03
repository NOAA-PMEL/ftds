package sdig.ftds.iosp;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * This is the Tool class (based on the Anagram Tool class) that runs Ferret so it can do work for the IOSP.
 * @author rhs
 *
 */
public class FerretTool extends Tool {
    private final static Logger log = LoggerFactory.getLogger(FerretTool.class.getName());
    ObjectMapper mapper = new ObjectMapper();
    FerretConfig ferretConfig;
    String scriptDir;
    String tempDir;
    String dataDir;
    /**
     * The default constructor that reads the config file and readies the tool to be used.
     * @throws Exception if there is a configuration or runtime error
     */
    public FerretTool() throws Exception {

        // Set up the Java Properties Object
        String configFilePath = System.getProperty("ftds.content.dir");
        File configFile;
        if ( configFilePath != null ) {
            configFile = new File(configFilePath + File.separator + "ferretConfig.json");
        } else {
            throw new Exception("-Dftds.content.dir system property was not set.");
        }

        if (!configFilePath.endsWith(File.separator)) {
            configFilePath = configFilePath + File.separator;
        }
        scriptDir = configFilePath + "scripts";
        tempDir = configFilePath + "temp";
        dataDir = configFilePath + "data" + File.separator + "dynamic";

        File scriptDirFile = new File(scriptDir);
        if (!scriptDirFile.exists()) {
            throw new IOSPException("You must install the scripts in " + scriptDir + ".");
        }

        File tempDirFile = new File(tempDir);
        if ( !tempDirFile.exists() ) {
            boolean temp = tempDirFile.mkdirs();
            if ( !temp ) {
                throw new IOSPException("Could not create the temp directory in " + tempDir);
            }
        }

        File dataDirFile = new File(dataDir);
        if ( !dataDirFile.exists() ) {
            boolean data = dataDirFile.mkdirs();
            if ( !data ) {
                throw new IOSPException("Could not create the data directory in " + dataDir);
            }
        }


        ferretConfig = mapper.readValue(configFile, FerretConfig.class);

        ferretConfig.addScriptDir(scriptDir);

        log.debug("config file parsed.");

    }
    /**
     * This runs the tool and captures the output to a debug file.
     * @param driver the command to run (ferret in this case).
     * @param jnl the command file.
     * @param cacheKey the cache key (should be removed since we don't need it)
     * @param output_filename where to write STDOUT, can be a debug file or the header.xml file.
     * @throws Exception
     */
    public void run (String driver, String jnl, String cacheKey, String temporary_filename, String output_filename) throws Exception {
        log.debug("Running the FerretTool.");
        HashMap<String, String> envMap = ferretConfig.getEnvironmentAsStrings();
        log.debug("got enviroment.");
        for(Iterator<String> it = envMap.keySet().iterator(); it.hasNext(); ) {
            String k = it.next();
            String e = envMap.get(k);
            log.debug("key=" + k);
            log.debug("value=" + e);
        }
        RuntimeEnvironment runTimeEnv = new RuntimeEnvironment();
        log.debug("Constructed new runTimeEnv.");
        runTimeEnv.setParameters(envMap);

        log.debug("Setting up the Ferret journal file.");

        String journalName;
        synchronized(this) {
            journalName = "ferret_operation"
                    + "_" + System.currentTimeMillis();
        }

        if ( tempDir.contains("../") || tempDir.contains("/..") || cacheKey.contains("../") || cacheKey.contains("/..") || journalName.contains("../") || journalName.contains("/..") ) {
            throw new Exception("Illegal file name.");

        }

        File jnlFile = new File(tempDir +File.separator+ cacheKey + File.separator + journalName + ".jnl");

        log.debug("Creating Ferret journal file in " + jnlFile.getAbsolutePath() );

        createJournal(jnl, jnlFile);

        log.debug("Finished creating Ferret journal file.");

        String args[] = new String[]{driver, jnlFile.getAbsolutePath(), temporary_filename};

        log.debug("Creating Ferret task.");

        Task ferretTask = null;

        long timeLimit = ferretConfig.getTimeLimit();

        try {
            ferretTask = task(runTimeEnv, args, timeLimit, scriptDir, tempDir);
        } catch (Exception e) {
            log.error("Could not create Ferret task. "+e.toString());
        }

        log.debug("Running Ferret task.");

        if ( temporary_filename.contains("../") || temporary_filename.contains("/..") ) {
            throw new Exception("Illegal temporary file name.");
        }

        if ( output_filename.contains("../") || output_filename.contains("/..") ) {
            throw new Exception("Illegal output file name.");
        }

        File temp_file = new File(temporary_filename);
        File out_file = new File(output_filename);
        try {
            if ( ferretTask != null ) {
                temp_file.createNewFile();
                ferretTask.run();
            }

        } catch (Exception e) {
            log.error("Ferret did not run correctly. "+e.toString());
        }

        log.debug("Ferret Task finished.");

        log.debug("Checking for errors.");



        String output = ferretTask.getOutput();
        String stderr = ferretTask.getStderr();
        if ( output.contains("/..") || output.contains("../") || stderr.contains("/..") || stderr.contains("../") ) {
            throw new Exception("Illegal file name.");
        }

        if ( !ferretTask.getHasError() || stderr.contains("**ERROR: regridding") ) {
            // Everything worked.  Create output.

            log.debug("Ferret task completed without error.");
            log.debug("Output:\n"+output);
            temp_file.renameTo(out_file);
            if ( output_filename != null && !output_filename.equals("") ) {

                String logfile = output_filename+".log";

                if ( logfile.contains("../") || logfile.contains("/..") ) {
                    throw new Exception("Illegal log file name.");
                }
                log.debug("Writing output to "+output_filename);
                PrintWriter logwriter = new PrintWriter(new FileOutputStream(logfile));
                logwriter.println(output);
                logwriter.println(stderr);

                logwriter.flush();
                logwriter.close();


            }
        }
        else {
            // Error was generated.  Make error page instead.
            log.error("Ferret generated an error.");
            String errorMessage = ferretTask.getErrorMessage();
            log.error(errorMessage+"\n"+stderr+"\n");
            log.error(output);
            File bad = new File(out_file.getAbsoluteFile()+".bad");
            temp_file.renameTo(bad);
        }

        log.debug("Finished running the FerretTool.");

    }
    /**
     * Run Ferret and capture STDOUT into the header.xml file for this data set.
     * @param driver which command to run (ferret in this case).
     * @param jnl the file with the commands to run.
     * @param cacheKey not used, should be removed.
     * @return return the full path to the STDOUT file (header.xml in this case).
     * @throws Exception
     */
    public String run_header(String driver, String jnl, String cacheKey) throws Exception {
        log.debug("Entering method.");
        String tempDir   = getTempDir();

        if (!tempDir.endsWith(File.separator)) {
            tempDir = tempDir + File.separator;
        }
        tempDir = tempDir+cacheKey;
        File cacheDir = new File (tempDir);
        String header_filename = tempDir+File.separator+"header.xml";
        String header_temp_filename = header_filename+".tmp";
        if ( header_filename.contains("../") || header_filename.contains("/..") ) {
            throw new Exception("Illegal header file name.");
        }
        if ( !cacheDir.isDirectory() ) {
            cacheDir.mkdir();
        } else {
            File header = new File(header_filename);
            if ( header.exists() ) {
                log.debug("The header file already exists.  It's a cache hit.  Return now.");
                return header_filename;
            }
        }

        log.debug("Generating the XML header for this data source using "+header_filename);
        run(driver, jnl, cacheKey, header_temp_filename, header_filename);

        return header_filename;
    }
    /**
     * Create a journal file that Ferret will use to perform a task.
     * @param jnl
     * @param jnlFile
     * @throws Exception
     */
    private void createJournal(String jnl, File jnlFile) throws Exception {
        PrintWriter jnlWriter = null;
        try {
            jnlWriter = new PrintWriter(new FileOutputStream(jnlFile));
        }
        catch(Exception e) {
            // We need to package these and send them back to the UI.
        }

        if ( jnlWriter != null ) {
            jnlWriter.println(jnl);
            jnlWriter.flush();
            jnlWriter.close();
        }

    }
    /**
     * The construct a Task which is the definition of the command line arguments for particular invocation of the Tool.
     * @param runTimeEnv the Java instantiation of the shell run time environment for the invocation of this command.
     * @param args the command line arguments
     * @param timeLimit how long to let it run
     * @param scriptDir where to look for the "driver" script.
     * @param tempDir where to write temp files if you need it.
     * @return the task object
     * @throws Exception
     */
    public Task task(RuntimeEnvironment runTimeEnv, String[] args, long timeLimit, String scriptDir, String tempDir) throws Exception {
        String[] errors = { "**ERROR", " **ERROR"," **TMAP ERR", "STOP -script mode", "Segmentation fault", "No such"};

        File scriptFile = new File(scriptDir, "data.jnl");
        if (!scriptFile.exists()) {
            throw new Exception("Missing controller script data.jnl");
        }

        scriptFile = new File(scriptDir, "header.jnl");
        if (!scriptFile.exists()) {
            throw new Exception("Missing controller script header.jnl");
        }

        StringBuffer argBuffer = new StringBuffer();


        for (int i = 0; i < args.length; i++) {
            argBuffer.append(args[i]);
            argBuffer.append(" ");
        }


        /*
         * There are two sets of arguments that come into the task.
         * fargs aref from the configuration -gif -script stuff like that.
         * args are those arguments that need to be passed to the [py]ferret command line
         * which in the case of making the header or the name of the script to make the headers,
         * the name of the script to initialize the data set and the name of the XML file to receive the info.
         *
         */

        String pythonBinary = ferretConfig.getPython();

        List<String> fargs = ferretConfig.getArgs();

        int offset = 0;

        String[] cmd = new String[offset + fargs.size() + args.length + 1];

        cmd[offset] = pythonBinary;

        for (int i = 0; i < fargs.size(); i++) {
            cmd[offset + i+1] = fargs.get(i);
        }
        for (int i = 0; i < args.length; i++) {
            cmd[offset + fargs.size()+1+i] = args[i];
        }


        String env[] = runTimeEnv.getEnv();

        File workDirFile = null;
        if (tempDir != null) {
            workDirFile = new File(tempDir);
        }


        Task task = new Task(cmd, env, workDirFile, timeLimit, errors);

        log.debug("command line for task is:\n"
                + task.getCmd());
        return task;

    }
    public String getDataDir() {
        return dataDir;
    }
    public String getTempDir() {
        return tempDir;
    }
    public String getScriptDir() {
        return scriptDir;
    }
}
