package org.envirocar.trackcount.mapmatching;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import org.envirocar.trackcount.JsonConstants;
import org.envirocar.trackcount.model.Feature;

import java.util.List;

public class MapMatchingResult {
    private Feature matchedRoute;
    private List<MatchedPoint> matchedPoints;

    @JsonGetter(JsonConstants.MATCHED_ROUTE)
    public Feature getMatchedRoute() {
        return matchedRoute;
    }

    @JsonSetter(JsonConstants.MATCHED_ROUTE)
    public void setMatchedRoute(Feature matchedRoute) {
        this.matchedRoute = matchedRoute;
    }

    @JsonGetter(JsonConstants.MATCHED_POINTS)
    public List<MatchedPoint> getMatchedPoints() {
        return matchedPoints;
    }

    @JsonSetter(JsonConstants.MATCHED_POINTS)
    public void setMatchedPoints(List<MatchedPoint> matchedPoints) {
        this.matchedPoints = matchedPoints;
    }
}
