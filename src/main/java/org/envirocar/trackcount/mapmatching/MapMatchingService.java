package org.envirocar.trackcount.mapmatching;

import org.envirocar.trackcount.model.FeatureCollection;

import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface MapMatchingService {
    @POST
    @Headers({"Accept: application/json", "Content-Type: application/json"})
    Call<MapMatchingResult> mapMatch(FeatureCollection featureCollection);
}
