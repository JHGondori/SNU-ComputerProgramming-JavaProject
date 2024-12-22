package com.example.stocksimulation.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stocksimulation.R;
import com.example.stocksimulation.adapter.RankingAdapter;
import com.example.stocksimulation.model.UserRanking;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RankingActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private RecyclerView rankingRecyclerView;
    private RankingAdapter rankingAdapter;
    private Button totalReturnButton, investmentReturnButton, backButton;

    private final int SELECTED_COLOR = Color.parseColor("#FFDDDDDD"); // 선택된 버튼 색상 (연한 회색)
    private final int DEFAULT_COLOR = Color.parseColor("#FFFFFFFF"); // 기본 버튼 색상 (흰색)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        firestore = FirebaseFirestore.getInstance();

        // RecyclerView 설정
        rankingRecyclerView = findViewById(R.id.rankingRecyclerView);
        rankingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rankingAdapter = new RankingAdapter(new ArrayList<>()); // 빈 리스트로 초기화
        rankingRecyclerView.setAdapter(rankingAdapter);

        // 버튼 설정
        totalReturnButton = findViewById(R.id.totalReturnButton);
        investmentReturnButton = findViewById(R.id.investmentReturnButton);
        backButton = findViewById(R.id.backButton);

        // 총 수익률 랭킹 불러오기
        totalReturnButton.setOnClickListener(v -> {
            loadRankingData("returns");
            setSelectedButton(totalReturnButton, investmentReturnButton);
        });

        // 투자 수익률 랭킹 불러오기
        investmentReturnButton.setOnClickListener(v -> {
            loadRankingData("investReturns");
            setSelectedButton(investmentReturnButton, totalReturnButton);
        });

        // 돌아가기 버튼
        backButton.setOnClickListener(v -> finish());

        // 초기 상태: 총 수익률 버튼 선택
        setSelectedButton(totalReturnButton, investmentReturnButton);
        loadRankingData("returns");
    }

    private void loadRankingData(String rankingType) {
        firestore.collection("users")
                .orderBy(rankingType, com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10) // 상위 10명
                .get()
                .addOnSuccessListener(documents -> {
                    List<UserRanking> userRankings = new ArrayList<>();
                    for (QueryDocumentSnapshot document : documents) {
                        String nickname = document.getString("nickname") != null ? document.getString("nickname") : "Unknown";
                        Double returnRate = document.getDouble(rankingType) != null ? document.getDouble(rankingType) : 0.0;
                        userRankings.add(new UserRanking(nickname, returnRate));
                    }
                    rankingAdapter.updateRankingList(userRankings);
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(RankingActivity.this, "랭킹 데이터를 불러오는 데 실패했습니다: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setSelectedButton(Button selectedButton, Button otherButton) {
        selectedButton.setBackgroundColor(SELECTED_COLOR); // 선택된 버튼 색상 변경
        otherButton.setBackgroundColor(DEFAULT_COLOR); // 다른 버튼 색상 원래대로
    }
}
