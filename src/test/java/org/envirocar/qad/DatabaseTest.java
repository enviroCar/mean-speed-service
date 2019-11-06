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
package org.envirocar.qad;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.Random;
import java.util.UUID;

import org.envirocar.meanspeed.database.PostgreSQLDatabase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class DatabaseTest {

	@Autowired
	private PostgreSQLDatabase database;
	
	@Test
	public void testDatabaseConnection() {
				
//		try {
//			database.createDemoTracks();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		String trackID = UUID.randomUUID().toString();
		
		try {
			boolean trackIDExists = database.trackIDExists(trackID);
			
			assertTrue(!trackIDExists);
			
		} catch (SQLException e) {
			fail(e.getMessage());
		}
		
		try {
			database.insertTrackID(trackID);
			
		} catch (SQLException e) {
			fail(e.getMessage());
		}
		
		try {
			boolean trackIDExists = database.trackIDExists(trackID);
			
			assertTrue(trackIDExists);
			
		} catch (SQLException e) {
			fail(e.getMessage());
		}
		
		
		
//		long osmID = new Random().nextInt();
//		
//		if(osmID < 0) {
//			osmID = osmID * -1;
//		}
//		
//		int count = database.getSegmentMetadata(osmID);
//				
//		assertTrue(count == -1);
//		
//		boolean success = database.insertSegmentMetadata(osmID, 1);
//		
////		assertTrue(success);
//		
//		success = database.updateSegmentMetadata(osmID, 2);
//		
////		assertTrue(success);
//		
//		count = database.getSegmentMetadata(osmID);
//		
//		assertTrue(count == 2);
//		
//		osmID = new Random().nextInt();
//		
//		database.increaseSegmentMetadata(osmID);
//		
//		count = database.getSegmentMetadata(osmID);
//		
//		assertTrue(count == 1);
//		
//		database.increaseSegmentMetadata(osmID);
//		
//		count = database.getSegmentMetadata(osmID);
//		
//		assertTrue(count == 2);
		
	}
	
}
