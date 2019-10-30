package org.envirocar.meanspeed.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

//import java.time.Duration;
import okhttp3.OkHttpClient;

import org.envirocar.meanspeed.mapmatching.MapMatchingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.net.URL;
import java.time.Duration;

@Configuration
public class RetrofitConfiguration {

    @Bean
    public Retrofit retrofit(JacksonConverterFactory factory, OkHttpClient client, @Value("${trackcount.mapMatching.url}") URL url) {
        return new Retrofit.Builder().addConverterFactory(factory).client(client).baseUrl(url).build();
    }

    @Bean
    public JacksonConverterFactory jacksonConverterFactory(ObjectMapper mapper) {
        return JacksonConverterFactory.create(mapper);
    }

    @Bean
    public MapMatchingService mapMatchingService(Retrofit retrofit, @Value("${trackcount.mapMatching.url}") URL url) {
        return retrofit.newBuilder().baseUrl(url).build().create(MapMatchingService.class);
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                       .followRedirects(true)
                       .followSslRedirects(true).callTimeout(Duration.ofMillis(30000))
                       .connectTimeout(Duration.ofMillis(30000)).readTimeout(Duration.ofMillis(30000))
//                     .addInterceptor(new LoggingInterceptor())
                       .build();
    }

}
