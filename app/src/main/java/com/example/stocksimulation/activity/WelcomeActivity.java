package com.example.stocksimulation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksimulation.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class WelcomeActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Firestore 및 FirebaseAuth 초기화
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        EditText nicknameEditText = findViewById(R.id.nicknameEditText);
        Button nextButton = findViewById(R.id.nextButton);

        nextButton.setOnClickListener(v -> {
        String nickname = nicknameEditText.getText().toString().trim();

        if (nickname.isEmpty()) {
            Toast.makeText(this, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;

        if (userId != null) {
            // Firestore에 닉네임 저장
            firestore.collection("users").document(userId)
                .update("nickname", nickname)
                .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "닉네임이 설정되었습니다!", Toast.LENGTH_SHORT).show();
                // 설명 화면으로 이동
                Intent intent = new Intent(this, AppDescriptionActivity.class);
                startActivity(intent);
                finish(); // 환영 화면 종료
            })
            .addOnFailureListener(e ->
            Toast.makeText(this, "닉네임 설정에 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        } else {
            Toast.makeText(this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    });
    }
}
