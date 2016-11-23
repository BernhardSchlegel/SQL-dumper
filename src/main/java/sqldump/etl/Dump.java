package sqldump.etl;

import sqldump.log.Log;
import sqldump.log.PingPong;

import java.io.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

/**
 * Static class for dumping SQL ResultSet s to files
 */
public class Dump {

    /**
     * determines, how many files are written at once
     */
    private static int LINES_AT_ONCE = 1000;

    /**
     * Dumps the passed ResultSet to a file
     * @param rs The ResultSet to be dumped
     * @param filename Target file, e.g. "C:\temp\out.csv"
     * @param log a log object for accessing the console
     */
    public static void toFile(ResultSet rs, String filename, Boolean append, Log log) {
       try {
           PingPong ping = log.info("Dumping results to \"" + filename + "\"...");

           //PrintWriter csvWriter = new PrintWriter(new FileOutputStream(filename, append)) ;
           FileWriter fileWriter = new FileWriter(filename, append);
           BufferedWriter csvWriter = new BufferedWriter(fileWriter);

           ResultSetMetaData meta = rs.getMetaData() ; // use last element, since all others are closed
           int numberOfColumns = meta.getColumnCount() ;

           if( ! append) {
               String dataHeaders = "\"" + meta.getColumnName(1) + "\"" ;
               for (int i = 2 ; i < numberOfColumns + 1 ; i ++ ) {
                   dataHeaders += ",\"" + meta.getColumnName(i) + "\"" ;
               }
               csvWriter.append(dataHeaders + "\r\n") ;
               log.info("writing header...");
           }
           else {
               log.info("appending...");
           }


           PingPong ping2 = log.info("writing data...");
           int linesBuffered = 0;
           String buffer = "";
           while (rs.next()) {
               String row = "\"" + rs.getString(1) + "\""  ;
               for (int j = 2 ; j < numberOfColumns + 1 ; j ++ ) {
                   row += ",\"" + rs.getString(j) + "\"" ;
               }
               buffer += row + "\r\n";
               linesBuffered ++;

               if(linesBuffered >= LINES_AT_ONCE) {
                   csvWriter.append(buffer);
                   linesBuffered = 0;
                   buffer = "";
               }
           }

           if (linesBuffered != 0) {
               csvWriter.append(buffer);
           }

           if (rs != null) {
               rs.close();
           }
           csvWriter.close();

           log.finished(ping2);

           log.finished(ping);

        } catch (Exception e){
           log.error("query failed (" + filename + "), error: ");
           e.printStackTrace();
        }
    }
}
