package com.example.stocksimulation.model

data class Stock(
    val symbol: String,
    val name: String,
    val count: Long,
    val totalInvested: Double,
    val currentValue: Double,
    val returnRate: Double
)
