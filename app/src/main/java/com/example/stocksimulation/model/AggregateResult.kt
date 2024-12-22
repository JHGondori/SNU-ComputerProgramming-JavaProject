package com.example.stocksimulation.model

data class AggregateResult(
    val c: Double, // Close price
    val h: Double, // High price
    val l: Double, // Low price
    val o: Double, // Open price
    val v: Double, // Volume
    val vw: Double, // Volume-weighted average price
    val t: Long // Timestamp아님
)