package org.envirocar.qad;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.envirocar.meanspeed.database.PostgreSQLDatabase;
import org.junit.Test;

public class DatabaseTest {

	@Test
	public void testDatabaseConnection() {
		
		PostgreSQLDatabase database = new PostgreSQLDatabase();
		
		long osmID = new Random().nextInt();
		
		if(osmID < 0) {
			osmID = osmID * -1;
		}
		
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
