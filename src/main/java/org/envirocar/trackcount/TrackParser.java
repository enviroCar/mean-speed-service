package org.envirocar.trackcount;

import org.envirocar.trackcount.model.FeatureCollection;
import org.envirocar.trackcount.model.Track;

public interface TrackParser {
    Track createTrack(FeatureCollection collection);
}
