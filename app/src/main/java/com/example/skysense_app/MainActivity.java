package com.example.skysense_app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private TextView tvHumidityValue, tvTemperatureValue, tvPressureValue, tvRainfallValue, tvWindSpeedValue, tvUVIndexValue, tvRainPredictionValue, tvWeatherConditionValue;
    private TextView tvWelcome, tvEmail, tvUsername, btnLogout, viewProfileButton, viewGraphButton;
    private DatabaseReference weatherReference;
    private String currentUsername; // Username untuk identifikasi pengguna
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Ambil username dari SharedPreferences
        sharedPreferences = getSharedPreferences("SkySensePrefs", Context.MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", null);

        if (currentUsername == null) {
            redirectToLogin();
            return;
        }

        // Inisialisasi TextView untuk menampilkan data pengguna
//        tvWelcome = findViewById(R.id.tvWelcome);
        btnLogout = findViewById(R.id.idLogout);
        viewProfileButton = findViewById(R.id.idEditProfile);
        viewGraphButton = findViewById(R.id.idViewGraph);

        // Inisialisasi TextView untuk data cuaca
        tvHumidityValue = findViewById(R.id.idKelembapan);
        tvTemperatureValue = findViewById(R.id.idSuhu);
        tvPressureValue = findViewById(R.id.idTekanan);
        tvRainfallValue = findViewById(R.id.idHujan);
        tvWindSpeedValue = findViewById(R.id.idAngin);
        tvUVIndexValue = findViewById(R.id.idUV);
        tvRainPredictionValue = findViewById(R.id.idPrediksi);
        tvWeatherConditionValue = findViewById(R.id.idKondisiCuaca);

        // Referensi ke Firebase Realtime Database
        weatherReference = FirebaseDatabase.getInstance().getReference("uid=2/deviceid=2A/latest_reading");

        // Listener untuk data cuaca
        loadWeatherData();

        // Tombol Logout
        btnLogout.setOnClickListener(v -> logout());

        // Tombol Lihat Profil
        viewProfileButton.setOnClickListener(v -> viewProfile());

        // Tombol Lihat Grafik Cuaca
        viewGraphButton.setOnClickListener(v -> viewGraph());

        // Tombol Navigasi ke GraphWeatherActivity
        viewGraphButton.setOnClickListener(v -> {
            try {
                Intent graphIntent = new Intent(MainActivity.this, GraphWeatherActivity.class);
                startActivity(graphIntent);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Error: Unable to open Graph Activity", Toast.LENGTH_SHORT).show();
            }
        });


    }


    private void loadWeatherData() {
        weatherReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Ambil data cuaca
                    Double humidity = snapshot.child("humidity").getValue(Double.class);
                    Double temperature = snapshot.child("temperature").getValue(Double.class);
                    Double pressure = snapshot.child("pressure_hPa").getValue(Double.class);
                    Double rainfall = snapshot.child("rainfall_mm").getValue(Double.class);
                    Double windSpeed = snapshot.child("windSpeed_kmph").getValue(Double.class);
                    Integer uvIndex = snapshot.child("uvIndex").getValue(Integer.class);
                    String rainPrediction = snapshot.child("rainPrediction").getValue(String.class);
                    String weatherCondition = snapshot.child("weatherCondition").getValue(String.class);

                    String formattedPressure = pressure != null ? String.valueOf(pressure).substring(0, 3) : "N/A";

                    // Tampilkan data di TextView
                    tvHumidityValue.setText(humidity != null ? humidity + "%" : "N/A");
                    tvTemperatureValue.setText(temperature != null ? temperature + "°C" : "N/A");
                    tvPressureValue.setText(formattedPressure != null ? formattedPressure + " hPa" : "N/A");
                    tvRainfallValue.setText(rainfall != null ? rainfall + " mm" : "N/A");
                    tvWindSpeedValue.setText(windSpeed != null ? windSpeed + " km/h" : "N/A");
                    tvUVIndexValue.setText(uvIndex != null ? String.valueOf(uvIndex) : "N/A");
                    tvRainPredictionValue.setText(rainPrediction != null ? rainPrediction : "N/A");
                    tvWeatherConditionValue.setText(weatherCondition != null ? weatherCondition : "N/A");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load weather data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToLogin() {
        Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void logout() {
        redirectToLogin();
    }


    private void viewProfile() {
        Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
        startActivity(intent);
    }

    private void viewGraph() {
        Intent intent = new Intent(MainActivity.this, GraphWeatherActivity.class);
        startActivity(intent);
    }
}