package org.envirocar.meanspeed.mapmatching;

import org.envirocar.meanspeed.model.FeatureCollection;

public interface MapMatcher {

    FeatureCollection mapMatch(FeatureCollection featureCollection) throws MapMatchingException;

}
