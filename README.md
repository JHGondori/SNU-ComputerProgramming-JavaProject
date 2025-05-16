# Stock Simulation Android App
*A Java, Kotlin Project for SNU Computer Programming Course*

This project is a stock trading simulation Android app developed using Java and Kotlin. It allows users to simulate buying and selling stocks based on real-time data. The app integrates **Firebase** for user data management and **Polygon.io API** to fetch live stock market information.

## 📱 App Information
- **Project Name**: Stock Simulation
- **Namespace**: com.example.stocksimulation
- **App ID**: com.example.stocksimulation
- **Version**: 1.0 (versionCode: 1)
- **Minimum SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 35

## 🛠 Tech Stack
- **Programming Languages**: Java & Kotlin
- **Build System**: Gradle
- **Firebase**: User authentication, Firestore database
- **Polygon.io API**: Real-time stock data retrieval
- **Java Version**:
  - sourceCompatibility : Java 11
  - targetCompatibility : Java 11
- **Kotlin JVM Target**: 11

## 🚀 Features
- 📱 User authentication (email/password)
- 👤 Nickname setup and profile initialization
- 💰 Simulated stock trading with virtual cash ($10,000 initial balance)
- 📊 Real-time price chart (last 100 days)
- 🔍 Stock search and ticker lookup
- 📈 Portfolio management: shows balance, holdings, returns
- 🏆 User ranking system based on return rate
- 🔐 Secure backend with Firebase
- 🌐 Error handling for API and login states

## 📂 Project Structure (Main Packages)
```
com.example.stocksimulation
│
├── activity        // UI screens like login, assets, search, stock details
├── adapter         // RecyclerView adapters (portfolio & rankings)
├── model           // Data models (User, Stock, API responses)
└── network         // Retrofit setup and API interface 
```

## 🛠 Challenges & Solutions
- **Issue**: Handling asynchronous tasks with Firebase and Polygon API using Java was complex and unstable.
- **Solution**: Rewrote critical API calls using Kotlin and coroutines, resulting in smoother async handling and improved stability.

## 📌 Improvements & Future Plans
- Improve UI/UX for more professional look and feel
- Add more detailed stock analytics (e.g., volume, market cap)
- Implement request rate-limiting mechanism to handle Polygon API limitations
