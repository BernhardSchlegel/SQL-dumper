package sqldump;

import sqldump.etl.OracleConnection;
import sqldump.etl.Query;
import sqldump.log.Log;
import sqldump.log.PingPong;
import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Main class of the programm.
 * Created by Bennhard Schlegel on 17.10.2016.
 */
public class SQLDump {
    private void printInstructions() {
        log.info("to use the programm, please provide all arguments: java -jar sql_dumper.jar sql out user pwd con");
        log.infoIdent("   sql: Path to sql folder, holding the \"example.sql\" files, e.g. \"C:\\sql\\\"");
        log.infoIdent("   out: Path to output folder where the output csv files will be stored, e.g. \"C:\\out\\\"");
        log.infoIdent("   user: the username");
        log.infoIdent("   pwd: the password");
        log.infoIdent("   con: the connection string, e.g. \"jdbc:oracle:thin:@sdwh.yourcompany.net:1311:SDWH\"");
    }

    private Log log = new Log();

    /**
     * Main function. Triggers all program logic
     * @param args parameters passed by user
     */
    public void run(String[] args) {
        log.logLogo();

        if(args.length != 5) {
            log.error(5-args.length + " argument(s) missing, please provide all of them.");
            return;
        }

        String sqlPath = args[0];
        String outPath = args[1];
        String username = args [2];
        String password = args [3];
        String connectionString = args[4];

        log.info("using the following options:");
        log.infoIdent("   location of sql folder:    " + sqlPath);
        log.infoIdent("   location of output folder: " + outPath);
        log.infoIdent("   username:                  " + username);
        log.infoIdent("   password:                  " + password);
        log.infoIdent("   connection string:         " + connectionString);

        // attach data to outpath
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
        Calendar cal = Calendar.getInstance();
        String dateString = dateFormat.format(cal.getTime());
        outPath += dateString + "\\";

        // check and create paths
        if (checkAndCreatePaths(sqlPath, outPath) != true) {
            return;
        }

        File folder = new File(sqlPath);
        File[] listOfSQLFiles = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".sql");
            }
        });
        Integer numOfFiles = listOfSQLFiles.length;
        try {
            Connection con = OracleConnection.Create(username, password, connectionString, log);

            if(numOfFiles > 0) {
                PingPong ppTotal = log.info("found " + numOfFiles + " .sql files in directory. Starting...");
                for (int i = 0; i < listOfSQLFiles.length; i++) {
                    if (listOfSQLFiles[i].isFile()) {
                        try {
                            String currentSQLFile = listOfSQLFiles[i].getName();
                            String fileNameWithOutExt = currentSQLFile.replaceFirst("[.][^.]+$", "");

                            // execute query
                            PingPong pp = log.info("working file " + (i+1) + "/" + numOfFiles + " (" + currentSQLFile + ")...");
                            Query qry = new Query(con, log);
                            qry.getDataFromSQLFile(sqlPath + "\\" + currentSQLFile, outPath + "\\" + fileNameWithOutExt + ".csv");
                            log.finished(pp);
                        } catch (Exception e) {
                            log.error("error occured while executing file \"" + listOfSQLFiles[i].getName().toString() + "\": ");
                            e.printStackTrace();
                        }
                    }
                }
                log.finished(ppTotal);
            }
            else {
                log.error("no .sql files found (ending with .sql, lowercase). returning...");
            }
        } catch (SQLException e) {
            log.error("failed establishing connection:");
            e.printStackTrace();
        }
    }

    /**
     * checks, if the specified paths are valid and existing. Creates directories if possible
     * for programm to work.
     * @param sqlPath Location of the SQL files.
     * @param outPath outputpath. SQL dumper will create a subdirectory namend after YYYY_MM_DD and store the results
     *                as .CSV
     * @return true if everything worked out, false else
     */
    private Boolean checkAndCreatePaths(String sqlPath, String outPath) {
        // check if sql path is ok
        File f = new File(sqlPath);
        if (!(f.exists() && f.isDirectory())) {
            log.error("sql path " + sqlPath + " does not exist. Aborting...");
            printInstructions();
            return false;
        }

        // check if outpath is ok
        File f2 = new File(outPath);
        if (f2.exists()) {
            if (!f2.isDirectory()) {
                log.error("out path " + outPath + " exists and is no directory. Aborting...");
                printInstructions();
                return false;
            }
        }
        else {
            log.info("out path " + outPath + " not existing. Creating...");
            File file = new File(outPath);
            file.mkdirs();
            log.info("outpath created");
        }

        return true;
    }
}
