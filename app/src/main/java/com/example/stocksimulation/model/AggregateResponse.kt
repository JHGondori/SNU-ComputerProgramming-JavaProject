package com.example.stocksimulation.model

data class AggregateResponse(
    val ticker: String,
    val results: List<AggregateResult>?,
    val queryCount: Int,
    val request_id: String,
    val status: String,
    val adjusted: Boolean
)