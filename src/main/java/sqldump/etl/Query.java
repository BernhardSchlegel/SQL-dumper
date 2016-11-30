package sqldump.etl;

import sqldump.etl.esql.ESQL;
import sqldump.log.Log;
import sqldump.log.PingPong;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates a query from a .sql file, stored on disk
 */
public class Query {
    /**
     * Connection, used for creating the query
     */
    private Connection connection;

    /**
     * Logobject for accessing the console
     */
    private Log log;

    /***
     * default constructor
     * @param con Connection object to database
     * @param log logobject, access console
     */
    public Query(Connection con, Log log) {
        this.connection = con;
        this.log = log;
    }

    /**
     * Gets a resultset by executing the SQL in the specified file
     * @param sqlFile the file to be executed
     * @return a ResultSet holding the results from the execution
     */
    public Boolean getDataFromSQLFile(String sqlFile, String outFile)  {

        try {
            FileReader fr = new FileReader(sqlFile);
            BufferedReader br = new BufferedReader(fr);

            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String query = sb.toString().replace(";","");
            br.close();
            return executeQuery(query, outFile);
        } catch (FileNotFoundException e) {
            log.error("failed to load file. error: ");
            e.printStackTrace();
        } catch (IOException e) {
            log.error("failed closing file. error: ");
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Replaces any ESQL found in query and generates subqueries. This process is repeated
     * rekursively for all generated subqueries (by calling this function again for every subquery) until
     * no ESQL is left in the query.
     *
     * @param query The query where the ESQL is replaced. This query is exploded to 1 or more subqueries
     * @return A List of Strings, holding all subqueries
     */
    private List<String> explodeNextESQL(String query) {
        List<String> explodedQueries = new ArrayList<String>();

        query = query.replace("-- $ESQL_","$ESQL_");
        query = query.replace("--$ESQL_","$ESQL_");

        // replace next $ESQL_
        // 1. find instance of $ESQL_ and isolate as string
        // 2. call the ESQL engine to replace string by result(s)
        // 3. generate subqueries, by replacing the instance by all results

        // 1. isolation
        // locate ESQL
        Pattern p = Pattern.compile("(\\$ESQL_.*?\\(.*?\\))");
        Matcher m = p.matcher(query);
        String esql = "";
        if (m.find()) {
            // only work first block, leave up rest to
            // recursion
            esql = m.group(1);
            log.info("esql detected: " + esql);
        }
        else {
            log.info("no esql detected, returning...");
            explodedQueries.add(query);
            return explodedQueries;
        }

        // 2. call engine
        List<String> esqlResults = ESQL.applyESQLFunction(esql, log);

        // 3. generate query
        for(Integer i = 0; i < esqlResults.size(); ++i) {
            String subquery = query.replace(esql, esqlResults.get(i));

            if(ESQL.containsESQL(subquery)) {
                explodedQueries.addAll(explodeNextESQL(subquery));
            }
            else {
                explodedQueries.add(subquery);
            }
        }


        return explodedQueries;
    }

    /**
     * takes on query
     * - searches for the first instance of an ESQL formula
     * - evaluates that
     * - Calls itself again if there is another instance
     * @param query
     * @return
     */
    private List<String> explodeFirstESQL(String query) {
        List<String> returnQueries = new ArrayList<String>();

        // locate ESQL
        Pattern p = Pattern.compile(".*\\-\\-\\s*\\$\\((.*)\\).*");
        Matcher m = p.matcher(query);


        if (m.find()) {
            if(m.groupCount() == 0) {
                returnQueries.add(query);
            }
            if (m.groupCount() > 0) {
                String[] blocks = m.group(1).split(",\\s+");
                List<String> blockPermutations = new ArrayList<String>();

                for(Integer j = 0; j < blocks.length; j++) {
                    String blockValue = blocks[j];
                    log.info("working block " + blockValue);

                    List<String> tmp = ESQL.applyESQLFunction(blockValue, log);

                    for(Integer blocknum = 0; blocknum < blocks.length; blocknum++) {
                        if (blocknum != j) {

                        }
                    }
                }

                if( m.groupCount() > 1) {

                }
            }
        } else {
            returnQueries.add(query);
        }

        return returnQueries;
    }

    /**
     * Generates subqueries
     * @param query the query as string
     * @return list with all subqueries
     */
    private List<String> setESQL(String query) {
        List<String> queries = new ArrayList<String>();

        queries = explodeNextESQL(query);

        List<String> finalInserts = new ArrayList<String>();

        log.info("generated " + queries.size() + " sub-queries from evaluating ESQL.");
        return queries;
    }

    /**
     * Executes the query
     * @param query The query as string (NOT a file, but the content)
     * @return a ResultSet holding the results from the execution
     */
    private Boolean executeQuery(String query, String filename) {
        // generate final queries
        List<String> queries = setESQL(query);

        Statement s = null;
        try {
            s = this.connection.createStatement();
            s.setFetchSize(10000);  // leads to major performance improvements
        } catch (SQLException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            // stack trace as a string
            log.error("executing query failed. message: " + sw.toString());
            log.error("failed to create query. Message:");
            e.printStackTrace();
            return false;
        }
        for(Integer i = 0; i < queries.size(); ++i) {
            PingPong ping = log.info("executing subquery (" + (i+1) + "/" + queries.size() + "): \n" + queries.get(i) );
            ResultSet rsSub = null;
            try {
                rsSub = s.executeQuery( queries.get(i) );
                Dump.toFile(rsSub, filename, !(i == 0), log);
            } catch (SQLException e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                // stack trace as a string
                log.error("executing query failed. message: " + sw.toString());
                return false;
            }
            log.finished(ping);
        }
        return true;
    }
}
