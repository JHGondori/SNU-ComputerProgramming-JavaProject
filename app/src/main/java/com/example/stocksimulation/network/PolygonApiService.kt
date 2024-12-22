package com.example.stocksimulation.network

import com.example.stocksimulation.model.AggregateResponse
import com.example.stocksimulation.model.TickerResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PolygonApiService {
    // 주식 검색 기능 추가
    @GET("v3/reference/tickers")
    suspend fun searchTickers(
        @Query("search") query: String,  // 검색어
        @Query("apiKey") apiKey: String // API 키
    ): Response<TickerResponse>

    @GET("v2/aggs/ticker/{stockTicker}/range/{multiplier}/{timespan}/{from}/{to}")
    suspend fun getAggregates(
        @Path("stockTicker") stockTicker: String,
        @Path("multiplier") multiplier: Int,
        @Path("timespan") timespan: String,
        @Path("from") from: String,
        @Path("to") to: String,
        @Query("apiKey") apiKey: String
    ): Response<AggregateResponse>
}
