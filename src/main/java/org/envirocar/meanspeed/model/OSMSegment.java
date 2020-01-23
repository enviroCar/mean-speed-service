package org.envirocar.meanspeed.model;

import java.util.ArrayList;
import java.util.List;

public class OSMSegment {

	private Long id;
	
	private List<Measurement> measurements;
	
	public OSMSegment(Long osmID) {
		this.id = osmID;
		this.measurements = new ArrayList<Measurement>();
	}
	
	public boolean addMeasurement(Measurement measurement) {
		return this.measurements.add(measurement);
	}

	public Long getId() {
		return id;
	}

	public List<Measurement> getMeasurements() {
		return measurements;
	}
	
}
