package com.example.stocksimulation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksimulation.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AppDescriptionActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_description);

        // Firebase 초기화
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        Button startSimulationButton = findViewById(R.id.startSimulationButton);

        startSimulationButton.setOnClickListener(v -> {
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();

            // 초기 자산 데이터
            Map<String, Object> initialAssets = new HashMap<>();
            initialAssets.put("balance", 10000.00);           // 총 자산: $10,000
            initialAssets.put("availableBalance", 10000.00);  // 투자 가능한 금액: $10,000
            initialAssets.put("investedBalance", 0.00);       // 투자된 금액: $0
            initialAssets.put("investReturns", 0.00);         // 투자 수익률
            initialAssets.put("returns", 0.0);                // 총 수익률: 0%
            initialAssets.put("stocks", Collections.emptyList()); // 보유 주식 정보

            // Firestore에 자산 정보 저장
            firestore.collection("users").document(userId)
                .update(initialAssets)
                .addOnSuccessListener(aVoid -> {
                // 저장 성공 시 자산 화면으로 이동
                Intent intent = new Intent(AppDescriptionActivity.this, AssetsActivity.class);
                startActivity(intent);
                finish(); // 앱 설명 화면 종료
            })
            .addOnFailureListener(e ->
            // 저장 실패 시 오류 메시지
            Toast.makeText(AppDescriptionActivity.this,
                "자산 초기화 실패: " + e.getMessage(),
                Toast.LENGTH_SHORT
            ).show()
            );
        } else {
            // 사용자 정보가 없는 경우
            Toast.makeText(this, "사용자 정보가 없습니다. 다시 로그인 해주세요.", Toast.LENGTH_SHORT).show();
        }
    });
    }
}
