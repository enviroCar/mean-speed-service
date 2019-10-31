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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgreSQLDatabase {
	
    private static final Logger LOG = LoggerFactory.getLogger(PostgreSQLDatabase.class);
    
    private static final String TABLE_NAME = "SEGMENT_METADATA";
    private static final String COLUMN_NAME_COUNT = "COUNT";
    private static final String COLUMN_NAME_MEAN_SPEED = "MEAN_SPEED";
    private static final String COLUMN_NAME_ACCUMULATED_SPEED = "ACCUMULATED_SPEED";
    private static final String COLUMN_NAME_OSM_ID = "OSM_ID";
    
    private static String connectionURL = null;	
    private static Connection conn = null;

    /** SQL to insert a response into the database */
    public static final String insertionString = "INSERT INTO " + TABLE_NAME + " VALUES (?, ?, ?, ?)";

    /** SQL to update a response, that was already stored in the database */
    public static final String updateString = "UPDATE " + TABLE_NAME + " SET " + COLUMN_NAME_COUNT + " = (?), " + COLUMN_NAME_MEAN_SPEED + " = (?), " + COLUMN_NAME_ACCUMULATED_SPEED + " = (?) " + " WHERE " + COLUMN_NAME_OSM_ID + " = (?)";

    /** SQL to retrieve a response from the database */
    public static final String selectionString = "SELECT " + COLUMN_NAME_COUNT +  ", " + COLUMN_NAME_MEAN_SPEED +   ", " + COLUMN_NAME_ACCUMULATED_SPEED + " FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_OSM_ID + " = (?)";

    protected static PreparedStatement insertSQL = null;
    protected static PreparedStatement updateSQL = null;
    protected static PreparedStatement selectSQL = null;
    
    public static final String pgCreationString = "CREATE TABLE " + TABLE_NAME + " ("
    		+ COLUMN_NAME_OSM_ID + " INTEGER NOT NULL PRIMARY KEY, "
            + COLUMN_NAME_COUNT + " SMALLINT, "
            + COLUMN_NAME_MEAN_SPEED + " DOUBLE PRECISION,"
            + COLUMN_NAME_ACCUMULATED_SPEED + " DOUBLE PRECISION)";
    
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
            
            if(!createTable()) {
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

    private boolean createTable() {
        try {
            ResultSet rs;
            DatabaseMetaData meta = PostgreSQLDatabase.conn.getMetaData();
            rs = meta.getTables(null, null, TABLE_NAME.toLowerCase(), new String[]{"TABLE"});
            if (!rs.next()) {
                LOG.info(String.format("Table %s does not yet exist.", TABLE_NAME));
                Statement st = PostgreSQLDatabase.conn.createStatement();
                st.executeUpdate(PostgreSQLDatabase.pgCreationString);

                PostgreSQLDatabase.conn.commit();

                meta = PostgreSQLDatabase.conn.getMetaData();

                rs = meta.getTables(null, null, TABLE_NAME.toLowerCase(), new String[]{"TABLE"});
                if (rs.next()) {
                    LOG.info(String.format("Succesfully created table %s.", TABLE_NAME));
                } else {
                    LOG.error(String.format("Could not create table %s.", TABLE_NAME));
                    return false;
                }
            } else {
                LOG.info(String.format("Table %s does exist.", TABLE_NAME));
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
    
    private double calculateMeanSpeed(double accumulatedSpeed, int count) {
		
		return accumulatedSpeed / count;
	}
    
    public void updateSegmentMetadata(long osmId, double speed) {
    	
    	SegmentMetadata segmentMetadata = getSegmentMetadata(osmId);
    	
    	if (segmentMetadata == null) {
    		
    		insertSegmentMetadata(osmId, 1, speed, speed);
    		
    	} else {
    		
    		int count = segmentMetadata.getCount();
    		
    		count++;
    		
    		double accumulatedSpeed = segmentMetadata.getAccumulatedSpeed();
    		
    		accumulatedSpeed = accumulatedSpeed + speed;
    		
			double newMeanSpeed = calculateMeanSpeed(accumulatedSpeed, count);
    		
    		updateSegmentMetadata(osmId, count, newMeanSpeed, accumulatedSpeed);
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
			double meanSpeed = resultSet.getDouble(2);
			double accumulatedSpeed = resultSet.getDouble(3);
			
			LOG.info(String.format("Got count = %d, mean speed = %e and accumulatedSpeed = %e for osm id = %d", count, meanSpeed, accumulatedSpeed, osmId));
			
			result = new SegmentMetadata(count, meanSpeed, accumulatedSpeed);
						
		} catch (SQLException e) {
			LOG.error("Could not create selection SQL.", e);
		}
    	
    	return result;
    }
    
    public boolean insertSegmentMetadata(Long osmId, int count, double meanSpeed, double accumulatedSpeed) {
    	
    	boolean result = false; 
    	
    	try {
			insertSQL.setLong(1, osmId);
			insertSQL.setInt(2, count);
			insertSQL.setDouble(3, meanSpeed);
			insertSQL.setDouble(4, accumulatedSpeed);
			insertSQL.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			LOG.error("Could not insert count for OSM ID: " + osmId, e);
		}
    	
    	return result;
    }
    
    public boolean updateSegmentMetadata(Long osmId, int count, double meanSpeed, double accumulatedSpeed) {
    	
    	boolean result = false; 
    	
    	try {
			updateSQL.setInt(1, count);
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
    
    class SegmentMetadata {
    	
    	private int count;
		private double meanSpeed;
		private double accumulatedSpeed;
    	
    	public SegmentMetadata(int count, double meanSpeed, double accumulatedSpeed) {
			this.count = count;
			this.meanSpeed = meanSpeed;
			this.accumulatedSpeed = accumulatedSpeed;
		}
    	public int getCount() {
			return count;
		}

		public double getMeanSpeed() {
			return meanSpeed;
		}
		public double getAccumulatedSpeed() {
			return accumulatedSpeed;
		}
    	
    	
    }
    
}
