package org.envirocar.segmentmetadata.stopcalculation;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.envirocar.meanspeed.model.Measurement;
import org.envirocar.meanspeed.model.OSMSegment;

public class OSMSegmentStopCalculator {
	
    private double length = -1.0d;
    private int stops = -1;
    private double startThresholdSpeed = 5.0d;
    private double endThresholdSpeed = 10.0d;
    Collection<OSMSegment> osmSegments;

	public OSMSegmentStopCalculator(Collection<OSMSegment> osmSegments) {
		this.osmSegments = osmSegments;
	}
	
	public OSMSegmentStopCalculator() {	}
	
    public void calculateStops() {
        List<Stop> stops = new LinkedList<>();
        int stopStart = -1;
        
        OSMSegment[] osmSegmentArray = osmSegments.toArray(new OSMSegment[osmSegments.size()]);
        
        Iterator<OSMSegment> osmSegmentIterator = osmSegments.iterator();
        
        while (osmSegmentIterator.hasNext()) {
			OSMSegment osmSegment = (OSMSegment) osmSegmentIterator.next();
	        
	        List<Measurement> measurementList = osmSegment.getMeasurements();
	        
	        Measurement[] measurementsArray = measurementList.toArray(new Measurement[measurementList.size()]);
	        
	        for (int idx = 0; idx < measurementsArray.length; ++idx) {
	        	
	        	Measurement measurement = measurementsArray[idx];
	        	
	            double speed = measurement.getValues().getSpeed();
	            if (stopStart < 0) {
	                if (speed <= startThresholdSpeed) {
	                    stopStart = idx;
	                }
	            } else if (speed > endThresholdSpeed) {
	            	System.out.println(measurementsArray[idx].getId());
	                stops.add(new Stop(0, idx));
	                stopStart = -1;
	            }
	        }
	        this.stops = stops.size();
	        System.out.println(this.stops);
		}
    }
    
	public int calculateStops(List<Measurement> measurementList) {
		List<Stop> stops = new LinkedList<>();
		int stopStart = -1;

		Measurement[] measurementsArray = measurementList.toArray(new Measurement[measurementList.size()]);

		for (int idx = 0; idx < measurementsArray.length; ++idx) {

			Measurement measurement = measurementsArray[idx];

			double speed = measurement.getValues().getSpeed();
			if (stopStart < 0) {
				if (speed <= startThresholdSpeed) {
					stopStart = idx;
				}
			} else if (speed > endThresholdSpeed) {
				stops.add(new Stop(0, idx));
				stopStart = -1;
			}
		}
		return stops.size();
	}

    private static final class Stop {
        final int start;
        final int end;

        Stop(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
    
    public int getStopCount() {
    	return stops;
    }
	
}
