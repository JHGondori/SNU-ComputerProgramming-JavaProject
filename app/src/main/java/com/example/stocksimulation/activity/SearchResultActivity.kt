package com.example.stocksimulation.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.stocksimulation.R
import com.example.stocksimulation.model.Ticker
import com.example.stocksimulation.model.TickerResponse
import com.example.stocksimulation.network.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.Response


class SearchResultActivity : AppCompatActivity() {

    private lateinit var stockListView: ListView
    private val stockList = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)

        stockListView = findViewById(R.id.stockListView)

        // ListView 초기화
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, stockList)
        stockListView.adapter = adapter

        // 검색어 받기
        val searchTerm = intent.getStringExtra("SEARCH_TERM") ?: ""

        // 검색어가 비어 있지 않다면 검색을 시작
        if (searchTerm.isNotEmpty()) {
            searchStock(searchTerm)
        } else {
            Toast.makeText(this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show()
        }

        stockListView.setOnItemClickListener { _, _, position, _ ->
            val selectedStocksymbol = stockList[position].split(" - ")[0] // 심볼 추출
            val selectedStockname = stockList[position].split(" - ")[1]
            navigateToStockActivity(selectedStocksymbol, selectedStockname)
        }
    }

    private fun searchStock(query: String) {
        lifecycleScope.launch {
            try {
                val apiKey = "YourAPIkey"  // Polygon.io API 키
                val response: Response<TickerResponse> = RetrofitInstance.apiService.searchTickers(query, apiKey)

                if (response.isSuccessful) {
                    val tickerResponse = response.body()
                    if (tickerResponse != null && tickerResponse.results.isNotEmpty()) {
                        // 검색된 주식 심볼 목록을 ListView에 표시
                        displayStockList(tickerResponse.results)
                    } else {
                        Toast.makeText(this@SearchResultActivity, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SearchResultActivity, "검색 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SearchResultActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayStockList(tickers: List<Ticker>) {
        stockList.clear()
        tickers.forEach { ticker ->
            stockList.add("${ticker.ticker} - ${ticker.name}")
        }
        adapter.notifyDataSetChanged()
    }
    private fun navigateToStockActivity(symbol: String, name: String) {
        val intent = Intent(this, StockActivity::class.java)
        intent.putExtra("STOCK_SYMBOL", symbol)
        intent.putExtra("STOCK_NAME", name)
        startActivity(intent)
    }
}
