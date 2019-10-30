package org.envirocar.meanspeed.mapmatching;

import org.envirocar.meanspeed.model.FeatureCollection;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface MapMatchingService {
    @POST(value = "mapmatching")
    @Headers({"Accept: application/json", "Content-Type: application/json"})    
    Call<MapMatchingResult> mapMatch(@Body FeatureCollection featureCollection);
}
