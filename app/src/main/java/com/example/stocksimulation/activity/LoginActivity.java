package com.example.stocksimulation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stocksimulation.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth; // Firebase 인증 객체
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase 인증 초기화
        auth = FirebaseAuth.getInstance();

        // XML 요소 연결
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        // 로그인 버튼 클릭 이벤트
        loginButton.setOnClickListener(v -> {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        loginUser(email, password);
    });

        // 회원가입 버튼 클릭 이벤트
        registerButton.setOnClickListener(v -> {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        registerUser(email, password);
    });
    }

    // 사용자 로그인
    private void loginUser(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
        if (task.isSuccessful()) {
            Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show();
            if (auth.getCurrentUser() != null) {
                firestore.collection("users").document(auth.getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        boolean isNewUser = document.getBoolean("isNewUser") != null && document.getBoolean("isNewUser");

                        if (isNewUser) {
                            // 신규 사용자: WelcomeActivity로 이동
                            firestore.collection("users").document(auth.getCurrentUser().getUid())
                                .update("isNewUser", false);
                            Intent intent = new Intent(this, WelcomeActivity.class);
                            startActivity(intent);
                        } else {
                            // 기존 사용자: AssetsActivity로 이동
                            Intent intent = new Intent(this, AssetsActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        // 문서가 존재하지 않을 경우 예외 처리
                        Toast.makeText(this, "사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                    finish(); // 액티비티 종료
                })
                .addOnFailureListener(exception -> {
                    // Firestore 문서 가져오기 실패
                    Toast.makeText(this, "사용자 정보 로딩 실패: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    passwordEditText.getText().clear();
                });
            }
        } else {
            // 로그인 실패
            Toast.makeText(this, "로그인 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            passwordEditText.getText().clear();
        }
    });
    }

    // 사용자 회원가입
    private void registerUser(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
        if (task.isSuccessful()) {
            if (auth.getCurrentUser() != null) {
                firestore.collection("users").document(auth.getCurrentUser().getUid())
                    .set(new User(true))
                    .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                    emailEditText.getText().clear();
                    passwordEditText.getText().clear();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "회원가입 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    emailEditText.getText().clear();
                    passwordEditText.getText().clear();
                });
            }
        } else {
            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                Toast.makeText(this, "회원가입 실패: 중복 이메일 존재", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
            emailEditText.getText().clear();
            passwordEditText.getText().clear();
        }
    });
    }

    // User 클래스: Firestore에 저장할 사용자 데이터 모델
    public static class User {
        private boolean isNewUser;

        public User(boolean isNewUser) {
            this.isNewUser = isNewUser;
        }

        public boolean getIsNewUser() {
            return isNewUser;
        }

        public void setIsNewUser(boolean isNewUser) {
            this.isNewUser = isNewUser;
        }
    }
}
