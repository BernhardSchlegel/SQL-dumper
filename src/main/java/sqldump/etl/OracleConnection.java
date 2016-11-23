package sqldump.etl;

import oracle.jdbc.pool.OracleDataSource;
import sqldump.log.Log;
import sqldump.log.PingPong;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * For connecting to Oracle databases.
 */
public class OracleConnection {

    /**
     * Creates a connection to an oracle database
     * @param username A username, e.g. "DB_USER"
     * @param password A password, e.g. "rainbow123"
     * @param connectionString A connection string, e.g. "jdbc:oracle:thin:@sdwh.cooperation.net:1331:SDWH"
     * @param log Object for accessing the console
     * @return a Connection object
     * @throws SQLException
     */
    public static Connection Create(String username, String password, String connectionString, Log log) throws SQLException {
        PingPong ping = log.info("connecting to database...");
        OracleDataSource ods = new OracleDataSource();
        ods.setUser(username);
        ods.setPassword(password);
        ods.setURL(connectionString);
        log.finished(ping);
        return ods.getConnection();
    }
}
