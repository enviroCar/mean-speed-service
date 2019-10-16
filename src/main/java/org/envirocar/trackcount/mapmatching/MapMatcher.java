package org.envirocar.trackcount.mapmatching;

import org.envirocar.trackcount.model.FeatureCollection;

public interface MapMatcher {

    FeatureCollection mapMatch(FeatureCollection featureCollection) throws MapMatchingException;

}
