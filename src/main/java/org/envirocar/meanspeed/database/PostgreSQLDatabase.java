package org.envirocar.meanspeed.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgreSQLDatabase {
	
    private static final Logger LOG = LoggerFactory.getLogger(PostgreSQLDatabase.class);

    private static String connectionURL = null;	
    private static Connection conn = null;

    /** SQL to insert a response into the database */
    public static final String insertionString = "INSERT INTO COUNT VALUES (?, ?)";

    /** SQL to update a response, that was already stored in the database */
    public static final String updateString = "UPDATE COUNT SET COUNT = (?) WHERE OSM_ID = (?)";

    /** SQL to retrieve a response from the database */
    public static final String selectionString = "SELECT COUNT FROM COUNT WHERE OSM_ID = (?)";

    protected static PreparedStatement insertSQL = null;
    protected static PreparedStatement updateSQL = null;
    protected static PreparedStatement selectSQL = null;
    
    public static final String pgCreationString = "CREATE TABLE COUNT ("
            + "OSM_ID INTEGER NOT NULL PRIMARY KEY, "
            + "COUNT SMALLINT)";
    
    public PostgreSQLDatabase() {
    	
    	Properties postgresProperties = new Properties();
    	
    	try {
			postgresProperties.load(getClass().getClassLoader().getResourceAsStream("postgres.properties"));
		} catch (IOException e) {
			LOG.error("Could not load properties." , e);
		}
    	
    	String host = postgresProperties.getProperty("host");
    	String port = postgresProperties.getProperty("port");
    	String dbname = postgresProperties.getProperty("dbname");
    	String username = postgresProperties.getProperty("username");
    	String password = postgresProperties.getProperty("password");
    	
		try {

            Class.forName("org.postgresql.Driver");
            PostgreSQLDatabase.connectionURL = "jdbc:postgresql:" + host + ":" + port + "/" + dbname;
            LOG.debug("Database connection URL is: " + PostgreSQLDatabase.connectionURL);
            
            if(!createConnection(username, password)) {
            	LOG.error("Could not connect to database.");
            }
            
            if(!createCountTable()) {
            	LOG.error("Could not create count table.");
            }
            
            if(!createPreparedStatements()) {
            	LOG.error("Could not create prepared statements.");
            }            
            
		} catch (Exception e) {
			LOG.error("Could not connect to database.");
		}
	}

	private boolean createConnection(String username, String password) {
		Properties props = new Properties();

		props.setProperty("create", "true");
		props.setProperty("user", username);
		props.setProperty("password", password);
		PostgreSQLDatabase.conn = null;
		try {
			PostgreSQLDatabase.conn = DriverManager.getConnection(PostgreSQLDatabase.connectionURL, props);
			PostgreSQLDatabase.conn.setAutoCommit(false);
			LOG.info("Connected to database.");
		} catch (SQLException e) {
			LOG.error("Could not connect to or create the database.", e);
			return false;
		}
		return true;
	}

    private boolean createCountTable() {
        try {
            ResultSet rs;
            DatabaseMetaData meta = PostgreSQLDatabase.conn.getMetaData();
            rs = meta.getTables(null, null, "count", new String[]{"TABLE"});
            if (!rs.next()) {
                LOG.info("Table COUNT does not yet exist.");
                Statement st = PostgreSQLDatabase.conn.createStatement();
                st.executeUpdate(PostgreSQLDatabase.pgCreationString);

                PostgreSQLDatabase.conn.commit();

                meta = PostgreSQLDatabase.conn.getMetaData();

                rs = meta.getTables(null, null, "count", new String[]{"TABLE"});
                if (rs.next()) {
                    LOG.info("Succesfully created table COUNT.");
                } else {
                    LOG.error("Could not create table COUNT.");
                    return false;
                }
            }
        } catch (SQLException e) {
            LOG.error("Connection to the Postgres database failed: " + e.getMessage());
            return false;
        }
        return true;
    }

    private static boolean createPreparedStatements() {
        try {
            PostgreSQLDatabase.closePreparedStatements();
            PostgreSQLDatabase.insertSQL = PostgreSQLDatabase.conn.prepareStatement(insertionString);
            PostgreSQLDatabase.selectSQL = PostgreSQLDatabase.conn.prepareStatement(selectionString);
            PostgreSQLDatabase.updateSQL = PostgreSQLDatabase.conn.prepareStatement(updateString);
        } catch (SQLException e) {
            LOG.error("Could not create the prepared statements.", e);
            return false;
        }
        return true;
    }

    private static boolean closePreparedStatements() {
        try {
            if (PostgreSQLDatabase.insertSQL != null) {
                PostgreSQLDatabase.insertSQL.close();
                PostgreSQLDatabase.insertSQL = null;
            }
            if (PostgreSQLDatabase.selectSQL != null) {
                PostgreSQLDatabase.selectSQL.close();
                PostgreSQLDatabase.selectSQL = null;
            }
            if (PostgreSQLDatabase.updateSQL != null) {
                PostgreSQLDatabase.updateSQL.close();
                PostgreSQLDatabase.updateSQL = null;
            }
        } catch (SQLException e) {
            LOG.error("Prepared statements could not be closed.", e);
            return false;
        }
        
        return true;
    }
    
    public void increaseTrackCount(long osmId) {
    	
    	int trackCount = getTrackCount(osmId);
    	
    	if (trackCount == -1) {
    		
    		insertTrackCount(osmId, 1);
    		
    	} else {
    		
    		trackCount++;
    		
    		updateTrackCount(osmId, trackCount);
    	}    	
    }
    
    public int getTrackCount(Long osmId) {
    	
    	int result = 0;
    	
    	try {
			selectSQL.setLong(1, osmId);
			
			ResultSet resultSet = selectSQL.executeQuery();
			
			if(!resultSet.next()) {
				return -1;
			}
			
			result = resultSet.getInt(1);
						
		} catch (SQLException e) {
			LOG.error("Could not create selection SQL.", e);
		}
    	
    	return result;    	
    }
    
    public boolean insertTrackCount(Long osmId, int count) {
    	
    	boolean result = false; 
    	
    	try {
			insertSQL.setLong(1, osmId);
			insertSQL.setInt(2, count);
			int resultInt = insertSQL.executeUpdate();
			conn.commit();
//			LOG.info("Result: " + resultInt);
		} catch (SQLException e) {
			LOG.error("Could not insert count for OSM ID: " + osmId, e);
		}
    	
    	return result;
    }
    
    public boolean updateTrackCount(Long osmId, int count) {
    	
    	boolean result = false; 
    	
    	try {
			updateSQL.setInt(1, count);
			updateSQL.setLong(2, osmId);
			int resultInt = updateSQL.executeUpdate();
			conn.commit();
//			LOG.info("Result: " + resultInt);
		} catch (SQLException e) {
			LOG.error("Could not insert count for OSM ID: " + osmId, e);
		}
    	
    	return result;
    }
    
}
