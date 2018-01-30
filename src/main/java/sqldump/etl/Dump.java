package sqldump.etl;

import sqldump.log.Log;
import sqldump.log.PingPong;

import java.io.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * Static class for dumping SQL ResultSet s to files
 */
public class Dump {

    /**
     * determines, how many files are written at once, in Byte? Byte thing is not clear
     * https://docs.oracle.com/javase/7/docs/api/java/io/BufferedWriter.html doesn't say
     * anything about it.
     */
    private static int BUFFER_SIZE = 1024*10;

    /**
     * Writes the list of Strings to a file using the given writer
     * @param records records to write
     * @param writer writer to use
     * @throws IOException If an error occurs during file handling
     */
    private static void write(List<String> records, Writer writer) throws IOException {
        for (String record: records) {
            writer.write(record);
        }
        writer.flush();
        writer.close();
    }

    /**
     * Writes the given list of strings to the given file
     * @param filename filename to be written
     * @param records the records as List of strings
     * @param bufSize the buffersize
     * @throws IOException
     */
    private static void writeBuffered(String filename, List<String> records, int bufSize, boolean append) throws IOException {
        try {
            FileWriter writer = new FileWriter(filename, append);
            BufferedWriter bufferedWriter = new BufferedWriter(writer, bufSize);
            write(records, bufferedWriter);
        } finally {
        }
    }


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

           ResultSetMetaData meta = rs.getMetaData() ; // use last element, since all others are closed
           int numberOfColumns = meta.getColumnCount() ;

           if( ! append) {
               String dataHeaders = "\"" + meta.getColumnName(1) + "\"" ;
               for (int i = 2 ; i < numberOfColumns + 1 ; i ++ ) {
                   dataHeaders += ",\"" + meta.getColumnName(i) + "\"" ;
               }
               fileWriter.append(dataHeaders + "\r\n") ;
               log.info("written header.");
           }
           else {
               log.info("appending...");
           }
           fileWriter.flush();
           fileWriter.close();

           PingPong ping2 = log.info("converting data to CSV (one dot corresponds with 10k lines)");
           List<String> records = new ArrayList<String>();
           int counter =0;
           while (rs.next()) {
        	   
        	   boolean StringType = (meta.getColumnType(1) == java.sql.Types.VARCHAR);
        	   
               StringBuffer sbRow = new StringBuffer();
               if(StringType) {
            	   sbRow.append("\"" + rs.getString(1) + "\""  );
               }else {
            	   sbRow.append(rs.getString(1));
               }

               for (int j = 2 ; j < numberOfColumns + 1 ; j ++ ) {
            	   StringType = (meta.getColumnType(j) == java.sql.Types.VARCHAR);
            	   if(StringType) {
            		   sbRow.append(",\"" + rs.getString(j) + "\"");
            	   }else {
            		   sbRow.append("," + rs.getString(j));
            	   }
               }
               sbRow.append("\r\n");
               records.add(sbRow.toString());

               if (records.size()%10000 == 0) {
            	   counter+=records.size();
                   System.out.print(".");
                   writeBuffered(filename, records, BUFFER_SIZE, true); // always apppend
                   records = new ArrayList<String>();
                   sbRow.setLength(0);
               }

           }
           log.finished(ping2);
           System.out.print(" (" + counter + " entries)");
           if (rs != null) {
               rs.close();
           }

           //PingPong ping3 = log.info("dumping CSV to file ...");
           //writeBuffered(filename, records, BUFFER_SIZE, true); // always apppend
           //log.finished(ping3);

           log.finished(ping);

        } catch (Exception e){
           log.error("query failed (" + filename + "), error: ");
           e.printStackTrace();
        }
    }
}
