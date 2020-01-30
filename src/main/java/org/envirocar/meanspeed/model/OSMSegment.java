package org.envirocar.meanspeed.model;

import java.util.ArrayList;
import java.util.List;

public class OSMSegment {

	private Long id;
	private long gId;

	private List<Measurement> measurements;
	
	public OSMSegment(Long osmID, Long gId) {
		this.id = osmID;
		this.gId = gId;
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
	
	public long getGId() {
		return gId;
	}
	
}
