package com.example.stocksimulation.activity

import java.util.*
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stocksimulation.R
import com.example.stocksimulation.network.RetrofitInstance
import com.example.stocksimulation.adapter.StockAdapter
import com.example.stocksimulation.model.Stock
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat

class AssetsActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var stockRecyclerView: RecyclerView
    private lateinit var stockAdapter: StockAdapter
    private val apiKey = "YourAPIkey" // Polygon.io API 키

    private var totalStockValue: Double = 0.0
    private var totalInvested: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assets)

        val assetTitleTextView: TextView = findViewById(R.id.assetTitleTextView)
        val assetsTextView: TextView = findViewById(R.id.assetsTextView)
        val investTextView: TextView = findViewById(R.id.investTextView)
        val returnTextView: TextView = findViewById(R.id.returnTextView)
        val investReturnTextView: TextView = findViewById(R.id.investReturnTextView)
        val stockInfoButton: Button = findViewById(R.id.stockInfoButton)
        val logoutButton: Button = findViewById(R.id.logoutButton)
        val rankingButton: Button = findViewById(R.id.rankingButton)  // 랭킹 버튼 추가

        // Firebase 초기화
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        stockRecyclerView = findViewById(R.id.stockRecyclerView)
        stockRecyclerView.layoutManager = LinearLayoutManager(this)
        stockAdapter = StockAdapter(mutableListOf()) // 빈 리스트로 초기화
        stockRecyclerView.adapter = stockAdapter

        val userId = auth.currentUser?.uid
        //user 데이터 보여주기
        if (userId != null) {
            // Firestore에서 데이터 가져오기
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nickname = document.getString("nickname") ?: "Unknown User"
                        val availableBalance = document.getDouble("availableBalance") ?: 0.0

                        var dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        var calendar = Calendar.getInstance()
                        var to = dateFormat.format(calendar.time)

                        calendar.add(Calendar.DAY_OF_YEAR, -2)
                        var from = dateFormat.format(calendar.time)

                        // Stocks 정보 가져오기
                        val stocks = document.get("stocks") as? Map<String, Map<String, Any>> ?: emptyMap()

                        // 주식 목록을 저장할 리스트
                        val stockList = mutableListOf<Stock>()

                        // CoroutineScope 내부의 비동기 작업을 추적
                        val deferredJobs = stocks.map { (symbol, stockData) ->
                            CoroutineScope(Dispatchers.IO).launch {
                                val invested = stockData["totalInvested"] as? Double ?: 0.0
                                val stockCount = stockData["count"] as? Long ?: 0L
                                val stockName = stockData["stockName"] as? String ?: "Unknown Stock"

                                try {
                                    // API 호출
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
                                        val stockPrice = aggregateData?.lastOrNull()?.c ?: 0.0

                                        val stockReturn =
                                            if (invested > 0) (stockPrice * stockCount - invested) / invested * 100 else 0.0

                                        synchronized(this@AssetsActivity) {
                                            stockList.add(
                                                Stock(
                                                    symbol = symbol,
                                                    name = stockName,
                                                    count = stockCount,
                                                    totalInvested = invested,
                                                    currentValue = stockPrice * stockCount,
                                                    returnRate = stockReturn
                                                )
                                            )
                                        }
                                    } else {
                                        // 오류 처리
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                this@AssetsActivity,
                                                "주식 가격을 가져오는 데 실패했습니다.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    // 오류 처리
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            this@AssetsActivity,
                                            "오류 발생: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }

                        // 모든 비동기 작업 완료 후 실행
                        CoroutineScope(Dispatchers.Main).launch {
                            deferredJobs.forEach { it.join() } // 모든 비동기 작업 완료 대기

                            // 주식 목록 기반으로 총합 계산
                            totalStockValue = stockList.sumOf { it.currentValue }
                            totalInvested = stockList.sumOf { it.totalInvested }

                            val totalBalance = availableBalance + totalStockValue
                            val investReturnRate =
                                if (totalInvested > 0) (totalStockValue - totalInvested) / totalInvested * 100 else 0.00
                            val overallReturnRate = (totalBalance - 10000.0) / 10000.0 * 100

                            // UI 업데이트
                            assetTitleTextView.text = "${nickname} 님의 자산"
                            assetsTextView.text = "보유 자산: ${"%.2f".format(totalBalance)} USD"
                            investTextView.text = "투자한 자산: ${"%.2f".format(totalStockValue)} USD"
                            investReturnTextView.text = "투자 수익률: ${"%.2f".format(investReturnRate)}%"
                            returnTextView.text = "총 수익률: ${"%.2f".format(overallReturnRate)}%"

                            // Firestore 데이터 업데이트
                            firestore.collection("users").document(userId)
                                .update(
                                    mapOf(
                                        "balance" to totalBalance,
                                        "investReturns" to investReturnRate,
                                        "returns" to overallReturnRate
                                    )
                                )
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this@AssetsActivity,
                                        "자산 데이터 업데이트 완료",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this@AssetsActivity,
                                        "데이터 업데이트 실패: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            // RecyclerView 업데이트
                            stockAdapter.updateStockList(stockList)
                        }

                    } else {
                        Toast.makeText(this, "데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "데이터를 불러오는데 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
        }
        //주식 검색 창으로 이동
        stockInfoButton.setOnClickListener {
            // 주식 검색 화면으로 이동
            val intent = Intent(this, StockSearchActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 랭킹 버튼 클릭 시 RankingActivity로 이동
        rankingButton.setOnClickListener {
            val intent = Intent(this, RankingActivity::class.java)
            startActivity(intent)
        }

        //로그아웃 버튼 구현
        logoutButton.setOnClickListener {
            auth.signOut() // Firebase 로그아웃
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()

            // 로그인 화면으로 이동
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // 현재 액티비티 종료
        }
    }

}
