package com.example.stocksimulation.activity

import java.util.*
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.stocksimulation.R
import com.example.stocksimulation.model.AggregateResult
import com.example.stocksimulation.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.components.XAxis
import java.text.SimpleDateFormat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class StockActivity : AppCompatActivity() {

    private lateinit var stockNameTextView: TextView
    private lateinit var stockSymbolTextView: TextView
    private lateinit var availableAssetsTextView: TextView

    private lateinit var lineChart: LineChart
    private lateinit var buyButton: Button
    private lateinit var sellButton: Button

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private val apiKey = "YourAPIkey" // Polygon.io API 키
    private var stockPrice: Double = 0.0 // 주식 가격 (기본값)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock)

        // View 초기화
        stockNameTextView = findViewById(R.id.stockNameTextView)
        stockSymbolTextView = findViewById(R.id.stockSymbolTextView)
        availableAssetsTextView = findViewById(R.id.availableAssetsTextView)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        lineChart = findViewById(R.id.lineChart)
        buyButton = findViewById(R.id.buyButton)
        sellButton = findViewById(R.id.sellButton)

        val backToAssetsButton: Button = findViewById(R.id.backToAssetsButton)

        // Intent로 전달된 주식 이름 및 심볼 받기
        val stockName = intent.getStringExtra("STOCK_NAME") ?: "Unknown Stock"
        val stockSymbol = intent.getStringExtra("STOCK_SYMBOL") ?: "Unknown Symbol"

        stockNameTextView.text = stockName
        stockSymbolTextView.text = stockSymbol

        // Aggregates 데이터 가져오기 & 그래프 그리기
        getAggregateData(stockSymbol)

        fetchUserAssets()

        // Buy 버튼 클릭 리스너
        buyButton.setOnClickListener {
            buyStock(stockSymbol, stockName)
        }

        // Sell 버튼 클릭 리스너
        sellButton.setOnClickListener {
            sellStock(stockSymbol, stockName)
        }

        backToAssetsButton.setOnClickListener {
            val intent = Intent(this, AssetsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun getAggregateData(symbol: String) {
        var dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var calendar = Calendar.getInstance()
        var to = dateFormat.format(calendar.time)

        calendar.add(Calendar.DAY_OF_YEAR, -100)
        var from = dateFormat.format(calendar.time)


        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.getAggregates(
                    stockTicker = symbol,
                    multiplier = 1,
                    timespan = "day",
                    from = from,
                    to = to,
                    apiKey = apiKey
                )

                if (response.isSuccessful) {
                    val aggregateData = response.body()?.results
                    aggregateData?.let {
                        withContext(Dispatchers.Main) {
                            stockPrice = it.lastOrNull()?.c ?: 0.0
                            displayChart(it)
                        }
                    } ?: withContext(Dispatchers.Main) {
                        Toast.makeText(this@StockActivity, "데이터가 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@StockActivity, "데이터 가져오기 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@StockActivity, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayChart(aggregates: List<AggregateResult>) {
        val entries = mutableListOf<Entry>()

        aggregates.forEachIndexed { index, aggregateData ->
            // 그래프에 표시할 데이터 (Y값: 가격)
            entries.add(Entry(index.toFloat(), aggregateData.c.toFloat()))
        }

        // 데이터 설정
        val dataSet = LineDataSet(entries, "Stock Price(USD)")
        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // X축 설정
        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                val formattedDate = getFormattedDateForXAxis(index)
                return formattedDate

            }

            private fun getFormattedDateForXAxis(value: Int): String {
                // value 값을 날짜로 변환하는 로직
                val calendar1 = Calendar.getInstance()
                calendar1.add(Calendar.DAY_OF_YEAR, -aggregates.size)
                calendar1.add(Calendar.DAY_OF_YEAR, value)
                val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                return dateFormat.format(calendar1.time)
            }
        }


        // X축 위치 조정
        xAxis.apply {
            setPosition(XAxis.XAxisPosition.BOTTOM)  // X축 위치 설정

            // X축 레이블 각도 설정
            setLabelRotationAngle(45f)  // 45도 각도로 X축 글씨 회전

            // X축 레이블과 관련된 설정
            labelCount = 6 // 레이블 개수 설정
        }
        lineChart.setExtraOffsets(10f, 10f, 10f, 35f)
        lineChart.apply {
            description.isEnabled = true // 제목을 활성화
            description.text = "주식 가격 변화" // 제목
        }
        lineChart.invalidate()  // 그래프 갱신

    }
    private fun fetchUserAssets() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val availableBalance = document.getDouble("availableBalance") ?: 0.0
                        availableAssetsTextView.text = "Available: $${String.format("%.2f", availableBalance)}"
                    } else {
                        Toast.makeText(this, "사용자 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "데이터 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun buyStock(stockSymbol: String, stockName: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val availableBalance = document.getDouble("availableBalance") ?: 0.0
                        val stocks = document.get("stocks") as? MutableMap<String, Any> ?: mutableMapOf()

                        if (availableBalance >= stockPrice) {
                            val currentStockCount = (stocks[stockSymbol] as? Map<*, *>)?.get("count") as? Long ?: 0L
                            val updatedStockCount = currentStockCount + 1 // Add 1 stock to the count
                            val updatedInvestment = ((stocks[stockSymbol] as? Map<*, *>)?.get("totalInvested") as? Double ?: 0.0) + stockPrice

                            val updatedStocks = stocks.apply {
                                this[stockSymbol] = mapOf(
                                    "stockName" to stockName,
                                    "totalInvested" to updatedInvestment,
                                    "count" to updatedStockCount
                                )
                            }

                            firestore.collection("users").document(userId)
                                .update(
                                    mapOf(
                                        "availableBalance" to availableBalance - stockPrice,
                                        "investedBalance" to stockPrice + (document.getDouble("investedBalance") ?: 0.0),
                                        "stocks" to updatedStocks
                                    )
                                )
                                .addOnSuccessListener {
                                    fetchUserAssets()
                                    Toast.makeText(this, "주식 구매 성공", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "잔액이 부족합니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
    }

    private fun sellStock(stockSymbol: String, stockName: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val stocks = document.get("stocks") as? MutableMap<String, Any> ?: mutableMapOf()
                        val stockData = stocks[stockSymbol] as? Map<*, *>
                        val currentStockCount = stockData?.get("count") as? Long ?: 0L
                        val currentInvestment = stockData?.get("totalInvested") as? Double ?: 0.0

                        if (currentStockCount > 0) {
                            val updatedStockCount = currentStockCount - 1 // Subtract 1 stock from the count
                            val updatedInvestment = currentInvestment - stockPrice

                            val updatedStocks = stocks.apply {
                                if (updatedStockCount > 0) {
                                    this[stockSymbol] = mapOf(
                                        "stockName" to stockName,
                                        "totalInvested" to updatedInvestment,
                                        "count" to updatedStockCount
                                    )
                                } else {
                                    this.remove(stockSymbol)
                                }
                            }

                            firestore.collection("users").document(userId)
                                .update(
                                    mapOf(
                                        "availableBalance" to (document.getDouble("availableBalance") ?: 0.0) + stockPrice,
                                        "investedBalance" to (document.getDouble("investedBalance") ?: 0.0) - stockPrice,
                                        "stocks" to updatedStocks
                                    )
                                )
                                .addOnSuccessListener {
                                    fetchUserAssets()
                                    Toast.makeText(this, "주식 판매 성공", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "팔 주식이 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
    }
}
