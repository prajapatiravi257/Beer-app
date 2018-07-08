package com.planetnoobs.rxjava.network;

import com.planetnoobs.rxjava.network.model.Beer;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.GET;

public interface ApiService {

    // Fetch all beers
    @GET("beercraft")
    Single<List<Beer>> fetchAllBeer();

}
