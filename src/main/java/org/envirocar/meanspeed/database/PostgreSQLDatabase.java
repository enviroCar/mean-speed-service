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
    
    private static final String TABLE_NAME = "SEGMENT_METADATA";
    private static final String TABLE_NAME_TRACK_IDS = "TRACK_IDS";
    private static final String TABLE_NAME_TRACK_IDS_NOT_MATCHED = "TRACK_IDS_NOT_MATCHED";
    private static final String COLUMN_NAME_COUNT = "COUNT";
    private static final String COLUMN_NAME_SPEED_MEASUREMENT_COUNT = "SPEED_MEASUREMENT_COUNT";
    private static final String COLUMN_NAME_MEAN_SPEED = "MEAN_SPEED";
    private static final String COLUMN_NAME_ACCUMULATED_SPEED = "ACCUMULATED_SPEED";
    private static final String COLUMN_NAME_CO2_MEASUREMENT_COUNT = "CO2_MEASUREMENT_COUNT";
    private static final String COLUMN_NAME_MEAN_CO2 = "MEAN_CO2";
    private static final String COLUMN_NAME_ACCUMULATED_CO2 = "ACCUMULATED_CO2";
    private static final String COLUMN_NAME_CONSUMPTION_MEASUREMENT_COUNT = "CONSUMPTION_MEASUREMENT_COUNT";
    private static final String COLUMN_NAME_MEAN_CONSUMPTION = "MEAN_CONSUMPTION";
    private static final String COLUMN_NAME_ACCUMULATED_CONSUMPTION = "ACCUMULATED_CONSUMPTION";
    private static final String COLUMN_NAME_OSM_ID = "OSM_ID";
    private static final String COLUMN_NAME_G_ID = "G_ID";
	private static final String COLUMN_NAME_TRACK_ID = "TRACK_ID";
	private static final String COLUMN_NAME_STOPS = "STOPS";
    
    private static String connectionURL = null;
    private static Connection conn = null;

    /** SQL to insert a response into the database */
    public static final String insertionString = "INSERT INTO " + TABLE_NAME + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    /** SQL to update a response, that was already stored in the database */
    public static final String updateStringSpeed = "UPDATE " + TABLE_NAME + " SET " + COLUMN_NAME_SPEED_MEASUREMENT_COUNT + " = (?), " + COLUMN_NAME_MEAN_SPEED + " = (?), " + COLUMN_NAME_ACCUMULATED_SPEED + " = (?) " + " WHERE " + COLUMN_NAME_OSM_ID + " = (?)" + " AND " + COLUMN_NAME_G_ID + " = (?)";

    /** SQL to update a response, that was already stored in the database */
    public static final String updateStringCo2 = "UPDATE " + TABLE_NAME + " SET " + COLUMN_NAME_CO2_MEASUREMENT_COUNT + " = (?), " + COLUMN_NAME_MEAN_CO2 + " = (?), " + COLUMN_NAME_ACCUMULATED_CO2 + " = (?) " + " WHERE " + COLUMN_NAME_OSM_ID + " = (?)" + " AND " + COLUMN_NAME_G_ID + " = (?)";
 
    /** SQL to update a response, that was already stored in the database */
    public static final String updateStringConsumption = "UPDATE " + TABLE_NAME + " SET " + COLUMN_NAME_CONSUMPTION_MEASUREMENT_COUNT + " = (?), " + COLUMN_NAME_MEAN_CONSUMPTION + " = (?), " + COLUMN_NAME_ACCUMULATED_CONSUMPTION + " = (?) " + " WHERE " + COLUMN_NAME_OSM_ID + " = (?)" + " AND " + COLUMN_NAME_G_ID + " = (?)";
     
    /** SQL to update a response, that was already stored in the database */
    public static final String updateStringStops = "UPDATE " + TABLE_NAME + " SET " + COLUMN_NAME_STOPS + " = (?) " + " WHERE " + COLUMN_NAME_OSM_ID + " = (?)" + " AND " + COLUMN_NAME_G_ID + " = (?)";
    
//    /** SQL to update a response, that was already stored in the database */
//    public static final String updateString = "UPDATE " + TABLE_NAME + " SET (?) = (?), (?) = (?), (?) = (?) WHERE " + COLUMN_NAME_OSM_ID + " = (?)";

    /** SQL to update a response, that was already stored in the database */
    public static final String updateStringTrackCount = "UPDATE " + TABLE_NAME + " SET " + COLUMN_NAME_COUNT + " = (?) WHERE " + COLUMN_NAME_OSM_ID + " = (?)" + " AND " + COLUMN_NAME_G_ID + " = (?)";
 
    public static final String selectionString = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_OSM_ID + " = (?)" + " AND " + COLUMN_NAME_G_ID + " = (?)";
    
    protected static PreparedStatement insertSQL = null;
    protected static PreparedStatement updateSQLConsumption = null;
    protected static PreparedStatement updateSQLSpeed = null;
    protected static PreparedStatement updateSQLCo2 = null;
    protected static PreparedStatement updateSQLTrackCount = null;
    protected static PreparedStatement updateSQLStops = null;
    protected static PreparedStatement selectSQL = null;
    
    public static final String pgCreationString = "CREATE TABLE " + TABLE_NAME + " ("
    		+ COLUMN_NAME_OSM_ID + " INTEGER NOT NULL, "
    		+ COLUMN_NAME_G_ID + " INTEGER NOT NULL, "
            + COLUMN_NAME_COUNT + " INTEGER, "
            + COLUMN_NAME_SPEED_MEASUREMENT_COUNT + " INTEGER, "
            + COLUMN_NAME_MEAN_SPEED + " DOUBLE PRECISION,"
            + COLUMN_NAME_ACCUMULATED_SPEED + " DOUBLE PRECISION, "
            + COLUMN_NAME_CO2_MEASUREMENT_COUNT + " INTEGER, "
            + COLUMN_NAME_MEAN_CO2 + " DOUBLE PRECISION,"
            + COLUMN_NAME_ACCUMULATED_CO2 + " DOUBLE PRECISION, "
            + COLUMN_NAME_CONSUMPTION_MEASUREMENT_COUNT + " INTEGER, "
            + COLUMN_NAME_MEAN_CONSUMPTION + " DOUBLE PRECISION,"
            + COLUMN_NAME_ACCUMULATED_CONSUMPTION + " DOUBLE PRECISION,"
            + COLUMN_NAME_STOPS + " INTEGER,"
            + "PRIMARY KEY(" + COLUMN_NAME_OSM_ID + ", " + COLUMN_NAME_G_ID + "))";
    
    public static final String pgCreationStringTrackIDs = "CREATE TABLE " + TABLE_NAME_TRACK_IDS + " ("
    		+ COLUMN_NAME_TRACK_ID + " VARCHAR NOT NULL PRIMARY KEY)";

    public static final String pgCreationStringTrackIDsNotMatched = "CREATE TABLE " + TABLE_NAME_TRACK_IDS_NOT_MATCHED + " ("
    		+ COLUMN_NAME_TRACK_ID + " VARCHAR NOT NULL PRIMARY KEY)";
    
    public PostgreSQLDatabase(String host, String port, String dbname, String username, String password) {
    	
//    	Properties postgresProperties = new Properties();
//    	
//    	try {
//			postgresProperties.load(getClass().getClassLoader().getResourceAsStream("postgres.properties"));
//		} catch (IOException e) {
//			LOG.error("Could not load properties." , e);
//		}
//    	
//    	host = postgresProperties.getProperty("host");
//    	port = postgresProperties.getProperty("port");
//    	dbname = postgresProperties.getProperty("dbname");
//    	username = postgresProperties.getProperty("username");
//    	password = postgresProperties.getProperty("password");
    	
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
            PostgreSQLDatabase.updateSQLConsumption = PostgreSQLDatabase.conn.prepareStatement(updateStringConsumption);
            PostgreSQLDatabase.updateSQLSpeed = PostgreSQLDatabase.conn.prepareStatement(updateStringSpeed);
            PostgreSQLDatabase.updateSQLCo2 = PostgreSQLDatabase.conn.prepareStatement(updateStringCo2);
            PostgreSQLDatabase.updateSQLTrackCount = PostgreSQLDatabase.conn.prepareStatement(updateStringTrackCount);
            PostgreSQLDatabase.updateSQLStops = PostgreSQLDatabase.conn.prepareStatement(updateStringStops);
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
            if (PostgreSQLDatabase.updateSQLConsumption != null) {
                PostgreSQLDatabase.updateSQLConsumption.close();
                PostgreSQLDatabase.updateSQLConsumption = null;
            }
            if (PostgreSQLDatabase.updateSQLSpeed != null) {
                PostgreSQLDatabase.updateSQLSpeed.close();
                PostgreSQLDatabase.updateSQLSpeed = null;
            }
            if (PostgreSQLDatabase.updateSQLCo2 != null) {
                PostgreSQLDatabase.updateSQLCo2.close();
                PostgreSQLDatabase.updateSQLCo2 = null;
            }
            if (PostgreSQLDatabase.updateSQLTrackCount != null) {
                PostgreSQLDatabase.updateSQLTrackCount.close();
                PostgreSQLDatabase.updateSQLTrackCount = null;
            }
            if (PostgreSQLDatabase.updateSQLStops != null) {
                PostgreSQLDatabase.updateSQLStops.close();
                PostgreSQLDatabase.updateSQLStops = null;
            }
        } catch (SQLException e) {
            LOG.error("Prepared statements could not be closed.", e);
            return false;
        }
        
        return true;
    }
    
    private double calculateMean(double accumulatedValues, int count) {
		
		return accumulatedValues / count;
	}
    
    public void updateSegmentSpeed(long osmId, long gId, double speed) {
    	
    	SegmentMetadata segmentMetadata = getSegmentMetadata(osmId, gId);
    	
    	if (segmentMetadata == null) {
    		
    		segmentMetadata = new SegmentMetadata(0, 1, speed, speed, 0, 0, 0, 0, 0, 0, 0);
    		
    		insertSegment(osmId, gId, segmentMetadata);
    		
    	} else {
    		
    		int measurementCount = segmentMetadata.getSpeedMeasurementCount();
    		
    		measurementCount++;
    		
    		double accumulatedSpeed = segmentMetadata.getAccumulatedSpeed();
    		
    		accumulatedSpeed = accumulatedSpeed + speed;
    		
			double newMeanSpeed = calculateMean(accumulatedSpeed, measurementCount);
    		
    		updateSegmentSpeed(osmId, gId, measurementCount, newMeanSpeed, accumulatedSpeed);
    	}
    }
    
    public void updateSegmentCo2(long osmId, long gId, double value) {
    	
    	SegmentMetadata segmentMetadata = getSegmentMetadata(osmId, gId);
    	
    	if (segmentMetadata == null) {
    		
    		segmentMetadata = new SegmentMetadata(0, 0, 0, 0, 1, value, value, 0, 0, 0, 0);
    		
    		insertSegment(osmId, gId, segmentMetadata);
    		
    	} else {
    		
    		int measurementCount = segmentMetadata.getCo2MeasurementCount();
    		
    		measurementCount++;
    		
    		double accumulated = segmentMetadata.getAccumulatedCo2();
    		
    		accumulated = accumulated + value;
    		
			double newMean = calculateMean(accumulated, measurementCount);
    		
    		updateSegmentCo2(osmId, gId, measurementCount, newMean, accumulated);
    	}
    }
    
    public void updateSegmentConsumption(long osmId, long gId, double value) {
    	
    	SegmentMetadata segmentMetadata = getSegmentMetadata(osmId, gId);
    	
    	if (segmentMetadata == null) {
    		
    		segmentMetadata = new SegmentMetadata(0, 0, 0, 0, 0, 0, 0, 1, value, value, 0);
    		
    		insertSegment(osmId, gId, segmentMetadata);
    		
    	} else {
    		
    		int measurementCount = segmentMetadata.getConsumptionMeasurementCount();
    		
    		measurementCount++;
    		
    		double accumulated = segmentMetadata.getAccumulatedConsumption();
    		
    		accumulated = accumulated + value;
    		
			double newMean = calculateMean(accumulated, measurementCount);
    		
    		updateSegment(osmId, gId, measurementCount, newMean, accumulated);
    	}
    }

	public SegmentMetadata getSegmentMetadata(Long osmId, long gId) {
    	
		SegmentMetadata result = null;
    	
    	try {
			selectSQL.setLong(1, osmId);
			selectSQL.setLong(2, gId);
			
			ResultSet resultSet = selectSQL.executeQuery();
			
			if(!resultSet.next()) {
				return result;
			}
			
			int count = resultSet.getInt(COLUMN_NAME_COUNT);
			int speedMeasurementCount = resultSet.getInt(COLUMN_NAME_SPEED_MEASUREMENT_COUNT);
			double meanSpeed = resultSet.getDouble(COLUMN_NAME_MEAN_SPEED);
			double accumulatedSpeed = resultSet.getDouble(COLUMN_NAME_ACCUMULATED_SPEED);
			int co2MeasurementCount = resultSet.getInt(COLUMN_NAME_CO2_MEASUREMENT_COUNT);
			double meanCo2 = resultSet.getDouble(COLUMN_NAME_MEAN_CO2);
			double accumulatedCo2 = resultSet.getDouble(COLUMN_NAME_ACCUMULATED_CO2);
			int consumptionMeasurementCount = resultSet.getInt(COLUMN_NAME_CONSUMPTION_MEASUREMENT_COUNT);
			double meanConsumption = resultSet.getDouble(COLUMN_NAME_MEAN_CONSUMPTION);
			double accumulatedConsumption = resultSet.getDouble(COLUMN_NAME_ACCUMULATED_CONSUMPTION);
			int stops = resultSet.getInt(COLUMN_NAME_STOPS);
			
			LOG.trace(String.format("Got count = %d, measurement count = %d, mean speed = %e and accumulatedSpeed = %e for osm id = %d", count, speedMeasurementCount, meanSpeed, accumulatedSpeed, osmId));
			
			result = new SegmentMetadata(count, speedMeasurementCount, meanSpeed, accumulatedSpeed, co2MeasurementCount, meanCo2, accumulatedCo2, consumptionMeasurementCount, meanConsumption, accumulatedConsumption, stops);
						
		} catch (SQLException e) {
			LOG.error("Could not create selection SQL.", e);
		}
    	
    	return result;
    }
    
    public boolean insertSegment(Long osmId, long gId, SegmentMetadata segmentMetadata) {
    	
    	boolean result = false;
    	
    	try {
			insertSQL.setLong(1, osmId);
			insertSQL.setLong(2, gId);
			insertSQL.setInt(3, 0);//track count will be increased separately
			insertSQL.setInt(4, segmentMetadata.getSpeedMeasurementCount());
			insertSQL.setDouble(5, segmentMetadata.getMeanSpeed());
			insertSQL.setDouble(6, segmentMetadata.getAccumulatedSpeed());
			insertSQL.setInt(7, segmentMetadata.getCo2MeasurementCount());
			insertSQL.setDouble(8, segmentMetadata.getMeanCo2());
			insertSQL.setDouble(9, segmentMetadata.getAccumulatedCo2());
			insertSQL.setInt(10, segmentMetadata.getConsumptionMeasurementCount());
			insertSQL.setDouble(11, segmentMetadata.getMeanConsumption());
			insertSQL.setDouble(12, segmentMetadata.getAccumulatedConsumption());
			insertSQL.setDouble(13, segmentMetadata.getStops());
			insertSQL.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			LOG.error("Could not insert Co2 for OSM ID: " + osmId + " and gid " + gId, e);
		}
    	
    	return result;
    }
    
    public boolean updateSegmentSpeed(Long osmId, long gId, int measurementCount, double meanSpeed, double accumulatedSpeed) {
    	
    	boolean result = false; 
    	
    	try {
			updateSQLSpeed.setInt(1, measurementCount);
    		updateSQLSpeed.setDouble(2, meanSpeed);
    		updateSQLSpeed.setDouble(3, accumulatedSpeed);
			updateSQLSpeed.setLong(4, osmId);
			updateSQLSpeed.setLong(5, gId);
			updateSQLSpeed.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			LOG.error("Could not update speed for OSM ID: " + osmId + " and gid " + gId, e);
		}
    	
    	return result;
    }
    
    public boolean updateSegmentCo2(Long osmId, long gId, int measurementCount, double meanCo2, double accumulatedCo2) {
    	
    	boolean result = false; 
    	
    	try {
			updateSQLCo2.setInt(1, measurementCount);
			updateSQLCo2.setDouble(2, meanCo2);
			updateSQLCo2.setDouble(3, accumulatedCo2);
			updateSQLCo2.setLong(4, osmId);
			updateSQLCo2.setLong(5, gId);
			updateSQLCo2.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			LOG.error("Could not update Co2 for OSM ID: " + osmId + " and gid " + gId, e);
		}
    	
    	return result;
    }
    
    public boolean updateSegment(Long osmId, long gId, int measurementCount, double mean, double accumulated) {
    	
    	boolean result = false; 
    	
    	try {
//			updateSQL.setString(1, COLUMN_NAME_CONSUMPTION_MEASUREMENT_COUNT);
			updateSQLConsumption.setInt(1, measurementCount);
//			updateSQL.setString(3, COLUMN_NAME_MEAN_CONSUMPTION);
			updateSQLConsumption.setDouble(2, mean);
//			updateSQL.setString(5, COLUMN_NAME_ACCUMULATED_CONSUMPTION);
			updateSQLConsumption.setDouble(3, accumulated);
			updateSQLConsumption.setLong(4, osmId);
			updateSQLConsumption.setLong(5, gId);
			updateSQLConsumption.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			LOG.error("Could not update segment for OSM ID: " + osmId + " and gid " + gId, e);
		}
    	
    	return result;
    }
    
    public void updateSegmentTrackCount(Long osmId, long gId) {
    	
    	SegmentMetadata segmentMetadata = getSegmentMetadata(osmId, gId);
    	
    	if (segmentMetadata == null) {
    		
    		updateSegmentTrackCount(osmId, gId, 1);
    		
    	} else {
    		
    		int count = segmentMetadata.getCount();
    		
    		count++;
    		
    		updateSegmentTrackCount(osmId, gId, count);
    	}
    }
    
    public boolean updateSegmentTrackCount(Long osmId, long gId, int count) {
    	
    	boolean result = false;
    	
    	try {
			updateSQLTrackCount.setInt(1, count);
			updateSQLTrackCount.setLong(2, osmId);
			updateSQLTrackCount.setLong(3, gId);
			updateSQLTrackCount.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			LOG.error("Could not insert track count for OSM ID: " + osmId + " and gid " + gId, e);
		}
    	
    	return result;
    }

	public void updateSegmentStops(Long osmId, long gId, int stops) {
    	
    	SegmentMetadata segmentMetadata = getSegmentMetadata(osmId, gId);
    	
    	if (segmentMetadata == null) {
    		
    		segmentMetadata = new SegmentMetadata(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, stops);
    		
    		insertSegment(osmId, gId, segmentMetadata);
    		
    	} else {
    		
    		int currentStops = segmentMetadata.getStops();
    		
    		int newStops = currentStops + stops;
    		        	
        	try {
    			updateSQLStops.setInt(1, newStops);
    			updateSQLStops.setLong(2, osmId);
    			updateSQLStops.setLong(3, gId);
    			updateSQLStops.executeUpdate();
    			conn.commit();
    		} catch (SQLException e) {
    			LOG.error("Could not update stops for OSM ID: " + osmId + " and gid " + gId, e);
    		}
    	}
		
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
    
    class SegmentMetadata {
    	
    	private int count;
    	private int speedMeasurementCount;
		private double meanSpeed;
		private double accumulatedSpeed;
    	private int co2MeasurementCount;
		private double meanCo2;
		private double accumulatedCo2;
    	private int consumptionMeasurementCount;
		private double meanConsumption;
		private double accumulatedConsumption;
		private int stops;
		
    	public SegmentMetadata(int count, int speedMeasurementCount, double meanSpeed, double accumulatedSpeed, 
    			int co2MeasurementCount, double meanCo2, double accumulatedCo2,
    			int consumptionMeasurementCount, double meanConsumption, double accumulatedConsumption, int stops) {
			this.count = count;
			this.speedMeasurementCount = speedMeasurementCount;
			this.meanSpeed = meanSpeed;
			this.accumulatedSpeed = accumulatedSpeed;
			this.co2MeasurementCount = co2MeasurementCount;
			this.meanCo2 = meanCo2;
			this.accumulatedCo2 = accumulatedCo2;
			this.consumptionMeasurementCount = consumptionMeasurementCount;
			this.meanConsumption = meanConsumption;
			this.accumulatedConsumption = accumulatedConsumption;
			this.stops = stops;
		}
    	public int getCount() {
			return count;
		}
		public int getSpeedMeasurementCount() {
			return speedMeasurementCount;
		}
		public double getMeanSpeed() {
			return meanSpeed;
		}
		public double getAccumulatedSpeed() {
			return accumulatedSpeed;
		}
		public int getCo2MeasurementCount() {
			return co2MeasurementCount;
		}
		public void setCo2MeasurementCount(int co2MeasurementCount) {
			this.co2MeasurementCount = co2MeasurementCount;
		}
		public double getMeanCo2() {
			return meanCo2;
		}
		public void setMeanCo2(double meanCo2) {
			this.meanCo2 = meanCo2;
		}
		public double getAccumulatedCo2() {
			return accumulatedCo2;
		}
		public void setAccumulatedCo2(double accumulatedCo2) {
			this.accumulatedCo2 = accumulatedCo2;
		}
		public int getConsumptionMeasurementCount() {
			return consumptionMeasurementCount;
		}
		public void setConsumptionMeasurementCount(int consumptionMeasurementCount) {
			this.consumptionMeasurementCount = consumptionMeasurementCount;
		}
		public double getMeanConsumption() {
			return meanConsumption;
		}
		public void setMeanConsumption(double meanConsumption) {
			this.meanConsumption = meanConsumption;
		}
		public double getAccumulatedConsumption() {
			return accumulatedConsumption;
		}
		public void setAccumulatedConsumption(double accumulatedConsumption) {
			this.accumulatedConsumption = accumulatedConsumption;
		}
		public int getStops() {
			return stops;
		}
		public void setStops(int stops) {
			this.stops = stops;
		}
    	
    }
    
}
