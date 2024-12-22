package com.example.stocksimulation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.stocksimulation.R;

public class StockSearchActivity extends AppCompatActivity {

    private EditText searchEditText;
    private Button searchButton;
    private Button backToAssetsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_search);

        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        backToAssetsButton = findViewById(R.id.backToAssetsButton);

        // 검색 버튼 클릭 처리
        searchButton.setOnClickListener(v -> {
        String searchTerm = searchEditText.getText().toString().trim();
            navigateToSearchResult(searchTerm);
    });

        // 뒤로 가기 버튼 클릭 처리
        backToAssetsButton.setOnClickListener(v -> navigateToAssetsActivity());
    }

    // 검색 결과 화면으로 이동
    private void navigateToSearchResult(String searchTerm) {
        Intent intent = new Intent(this, SearchResultActivity.class);
        intent.putExtra("SEARCH_TERM", searchTerm);
        startActivity(intent);
        finish();
    }

    // 자산 화면으로 돌아가기
    private void navigateToAssetsActivity() {
        Intent intent = new Intent(this, AssetsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 기존 스택 정리
        startActivity(intent);
        finish();
    }

    // 토스트 메시지 표시
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
