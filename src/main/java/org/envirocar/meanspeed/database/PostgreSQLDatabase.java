/**
 * Copyright (C) 2019 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public License
 * version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */
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
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgreSQLDatabase {
	
    private static final Logger LOG = LoggerFactory.getLogger(PostgreSQLDatabase.class);
    
    private static final String TABLE_NAME = "SEGMENT_METADATA";
    private static final String TABLE_NAME_TRACK_IDS = "TRACK_IDS";
    private static final String TABLE_NAME_TRACK_IDS_NOT_MATCHED = "TRACK_IDS_NOT_MATCHED";
    private static final String COLUMN_NAME_COUNT = "COUNT";
    private static final String COLUMN_NAME_MEASUREMENT_COUNT = "MEASUREMENT_COUNT";
    private static final String COLUMN_NAME_MEAN_SPEED = "MEAN_SPEED";
    private static final String COLUMN_NAME_ACCUMULATED_SPEED = "ACCUMULATED_SPEED";
    private static final String COLUMN_NAME_OSM_ID = "OSM_ID";
	private static final String COLUMN_NAME_TRACK_ID = "TRACK_ID";
    
    private static String connectionURL = null;
    private static Connection conn = null;

    /** SQL to insert a response into the database */
    public static final String insertionString = "INSERT INTO " + TABLE_NAME + " VALUES (?, ?, ?, ?, ?)";

    /** SQL to update a response, that was already stored in the database */
    public static final String updateString = "UPDATE " + TABLE_NAME + " SET " + COLUMN_NAME_MEASUREMENT_COUNT + " = (?), " + COLUMN_NAME_MEAN_SPEED + " = (?), " + COLUMN_NAME_ACCUMULATED_SPEED + " = (?) " + " WHERE " + COLUMN_NAME_OSM_ID + " = (?)";

    /** SQL to update a response, that was already stored in the database */
    public static final String updateStringTrackCount = "UPDATE " + TABLE_NAME + " SET " + COLUMN_NAME_COUNT + " = (?) WHERE " + COLUMN_NAME_OSM_ID + " = (?)";
    
    /** SQL to retrieve a response from the database */
    public static final String selectionString = "SELECT " + COLUMN_NAME_COUNT + ", " + COLUMN_NAME_MEASUREMENT_COUNT + ", " + COLUMN_NAME_MEAN_SPEED +   ", " + COLUMN_NAME_ACCUMULATED_SPEED + " FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_OSM_ID + " = (?)";

    protected static PreparedStatement insertSQL = null;
    protected static PreparedStatement updateSQL = null;
    protected static PreparedStatement updateSQLTrackCount = null;
    protected static PreparedStatement selectSQL = null;
    
    public static final String pgCreationString = "CREATE TABLE " + TABLE_NAME + " ("
    		+ COLUMN_NAME_OSM_ID + " INTEGER NOT NULL PRIMARY KEY, "
            + COLUMN_NAME_COUNT + " SMALLINT, "
            + COLUMN_NAME_MEASUREMENT_COUNT + " SMALLINT, "
            + COLUMN_NAME_MEAN_SPEED + " DOUBLE PRECISION,"
            + COLUMN_NAME_ACCUMULATED_SPEED + " DOUBLE PRECISION)";
    
    public static final String pgCreationStringTrackIDs = "CREATE TABLE " + TABLE_NAME_TRACK_IDS + " ("
    		+ COLUMN_NAME_TRACK_ID + " VARCHAR NOT NULL PRIMARY KEY)";

    public static final String pgCreationStringTrackIDsNotMatched = "CREATE TABLE " + TABLE_NAME_TRACK_IDS_NOT_MATCHED + " ("
    		+ COLUMN_NAME_TRACK_ID + " VARCHAR NOT NULL PRIMARY KEY)";
    
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
            
            if(!createTable(TABLE_NAME, pgCreationString)) {
            	LOG.error(String.format("Could not create %s table.", TABLE_NAME));
            }
            
            if(!createTable(TABLE_NAME_TRACK_IDS, pgCreationStringTrackIDs)) {
            	LOG.error(String.format("Could not create %s table.", TABLE_NAME_TRACK_IDS));
            }
            
            if(!createTable(TABLE_NAME_TRACK_IDS_NOT_MATCHED, pgCreationStringTrackIDsNotMatched)) {
            	LOG.error(String.format("Could not create %s table.", TABLE_NAME_TRACK_IDS_NOT_MATCHED));
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

    private boolean createTable(String tableName, String creationString) {
        try {
        	tableName = tableName.toLowerCase();
            ResultSet rs;
            DatabaseMetaData meta = PostgreSQLDatabase.conn.getMetaData();
            rs = meta.getTables(null, null, tableName, new String[]{"TABLE"});
            if (!rs.next()) {
                LOG.info(String.format("Table %s does not yet exist.", tableName));
                Statement st = PostgreSQLDatabase.conn.createStatement();
                st.executeUpdate(creationString);

                PostgreSQLDatabase.conn.commit();

                meta = PostgreSQLDatabase.conn.getMetaData();

                rs = meta.getTables(null, null, tableName, new String[]{"TABLE"});
                if (rs.next()) {
                    LOG.info(String.format("Succesfully created table %s.", tableName));
                } else {
                    LOG.error(String.format("Could not create table %s.", tableName));
                    return false;
                }
            } else {
                LOG.info(String.format("Table %s does exist.", tableName));
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
            PostgreSQLDatabase.updateSQLTrackCount = PostgreSQLDatabase.conn.prepareStatement(updateStringTrackCount);
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
            if (PostgreSQLDatabase.updateSQLTrackCount != null) {
                PostgreSQLDatabase.updateSQLTrackCount.close();
                PostgreSQLDatabase.updateSQLTrackCount = null;
            }
        } catch (SQLException e) {
            LOG.error("Prepared statements could not be closed.", e);
            return false;
        }
        
        return true;
    }
    
    private double calculateMeanSpeed(double accumulatedSpeed, int count) {
		
		return accumulatedSpeed / count;
	}
    
    public void updateSegmentSpeed(long osmId, double speed) {
    	
    	SegmentMetadata segmentMetadata = getSegmentMetadata(osmId);
    	
    	if (segmentMetadata == null) {
    		
    		insertSegmentSpeed(osmId, speed);
    		
    	} else {
    		
    		int measurementCount = segmentMetadata.getMeasurementCount();
    		
    		measurementCount++;
    		
    		double accumulatedSpeed = segmentMetadata.getAccumulatedSpeed();
    		
    		accumulatedSpeed = accumulatedSpeed + speed;
    		
			double newMeanSpeed = calculateMeanSpeed(accumulatedSpeed, measurementCount);
    		
    		updateSegmentSpeed(osmId, measurementCount, newMeanSpeed, accumulatedSpeed);
    	}
    }

	public SegmentMetadata getSegmentMetadata(Long osmId) {
    	
		SegmentMetadata result = null;
    	
    	try {
			selectSQL.setLong(1, osmId);
			
			ResultSet resultSet = selectSQL.executeQuery();
			
			if(!resultSet.next()) {
				return result;
			}
			
			int count = resultSet.getInt(1);
			int measurementCount = resultSet.getInt(2);
			double meanSpeed = resultSet.getDouble(3);
			double accumulatedSpeed = resultSet.getDouble(4);
			
			LOG.trace(String.format("Got count = %d, measurement count = %d, mean speed = %e and accumulatedSpeed = %e for osm id = %d", count, measurementCount, meanSpeed, accumulatedSpeed, osmId));
			
			result = new SegmentMetadata(count, measurementCount, meanSpeed, accumulatedSpeed);
						
		} catch (SQLException e) {
			LOG.error("Could not create selection SQL.", e);
		}
    	
    	return result;
    }
    
    public boolean insertSegmentSpeed(Long osmId, double speed) {
    	
    	boolean result = false;
    	
    	try {
			insertSQL.setLong(1, osmId);
			insertSQL.setInt(2, 0);//track count will be increased separately
			insertSQL.setInt(3, 1);
			insertSQL.setDouble(4, speed);
			insertSQL.setDouble(5, speed);
			insertSQL.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			LOG.error("Could not insert count for OSM ID: " + osmId, e);
		}
    	
    	return result;
    }
    
    public boolean updateSegmentSpeed(Long osmId, int measurementCount, double meanSpeed, double accumulatedSpeed) {
    	
    	boolean result = false; 
    	
    	try {
			updateSQL.setInt(1, measurementCount);
    		updateSQL.setDouble(2, meanSpeed);
    		updateSQL.setDouble(3, accumulatedSpeed);
			updateSQL.setLong(4, osmId);
			updateSQL.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			LOG.error("Could not insert count for OSM ID: " + osmId, e);
		}
    	
    	return result;
    }
    
    public void updateSegmentTrackCount(Long osmId) {
    	
    	SegmentMetadata segmentMetadata = getSegmentMetadata(osmId);
    	
    	if (segmentMetadata == null) {
    		
    		updateSegmentTrackCount(osmId, 1);
    		
    	} else {
    		
    		int count = segmentMetadata.getCount();
    		
    		count++;
    		
    		updateSegmentTrackCount(osmId, count);
    	}
    }
    
    public boolean updateSegmentTrackCount(Long osmId, int count) {
    	
    	boolean result = false;
    	
    	try {
			updateSQLTrackCount.setInt(1, count);
			updateSQLTrackCount.setLong(2, osmId);
			updateSQLTrackCount.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			LOG.error("Could not insert track count for OSM ID: " + osmId, e);
		}
    	
    	return result;
    }
    
    public boolean trackIDExists(String trackID) throws SQLException {
    	
    	PreparedStatement selectTrackID = PostgreSQLDatabase.conn.prepareStatement("SELECT " + COLUMN_NAME_TRACK_ID + " FROM " + TABLE_NAME_TRACK_IDS + " WHERE " + COLUMN_NAME_TRACK_ID + " = (?)");
		    	
    	selectTrackID.setString(1, trackID);
    	
		ResultSet resultSet = selectTrackID.executeQuery();
		
		if(resultSet.next()) {			
			return true;			
		}
		
		selectTrackID = PostgreSQLDatabase.conn.prepareStatement("SELECT " + COLUMN_NAME_TRACK_ID + " FROM " + TABLE_NAME_TRACK_IDS_NOT_MATCHED + " WHERE " + COLUMN_NAME_TRACK_ID + " = (?)");
    	
    	selectTrackID.setString(1, trackID);
    	
		resultSet = selectTrackID.executeQuery();
		
		if(resultSet.next()) {			
			return true;			
		}
		
		return false;
    }
    
    public void insertTrackID(String trackID) throws SQLException {
    	
    	PreparedStatement insertTrackID = PostgreSQLDatabase.conn.prepareStatement("INSERT INTO " + TABLE_NAME_TRACK_IDS + " VALUES (?)");
		
    	insertTrackID.setString(1, trackID);
    	
		insertTrackID.executeUpdate();
		
		conn.commit();
    }
    
    public void insertTrackIDNotMatched(String trackID) throws SQLException {
    	
    	PreparedStatement insertTrackID = PostgreSQLDatabase.conn.prepareStatement("INSERT INTO " + TABLE_NAME_TRACK_IDS_NOT_MATCHED + " VALUES (?)");
		
    	insertTrackID.setString(1, trackID);
    	
		insertTrackID.executeUpdate();
		
		conn.commit();
    }
    
    public void createDemoTracks() throws SQLException {
    	
    	PreparedStatement selectWays = PostgreSQLDatabase.conn.prepareStatement("SELECT DISTINCT OSM_ID FROM bfmap_ways LIMIT 500");
    			
		ResultSet resultSet = selectWays.executeQuery();
				
		while(resultSet.next()) {
						
			String osmID = resultSet.getString("OSM_ID");

			LOG.info(osmID);
			
			double speed = new Random().nextDouble() * 100;
			
			int low = 1;
			int high = 60;
			int count = new Random().nextInt(high-low) + low;
			
			insertSegmentSpeed(Long.valueOf(osmID), speed);
			
		}
    	
    }
    
    class SegmentMetadata {
    	
    	private int count;
    	private int measurementCount;
		private double meanSpeed;
		private double accumulatedSpeed;
    	
    	public SegmentMetadata(int count, int measurementCount, double meanSpeed, double accumulatedSpeed) {
			this.count = count;
			this.measurementCount = measurementCount;
			this.meanSpeed = meanSpeed;
			this.accumulatedSpeed = accumulatedSpeed;
		}
    	public int getCount() {
			return count;
		}

		public int getMeasurementCount() {
			return measurementCount;
		}
		public double getMeanSpeed() {
			return meanSpeed;
		}
		public double getAccumulatedSpeed() {
			return accumulatedSpeed;
		}
    	
    }
    
}
