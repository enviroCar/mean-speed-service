package org.envirocar.meanspeed;

import org.envirocar.meanspeed.model.FeatureCollection;
import org.envirocar.meanspeed.model.Track;

public interface TrackParser {
    Track createTrack(FeatureCollection collection);
}
