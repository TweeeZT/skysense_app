package com.example.skysense_app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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


    private TextView HumidityValue, TemperatureValue, PressureValue, RainfallValue,
            WindSpeedValue, UVIndexValue, RainPredictionValue, WeatherConditionValue;
    private TextView btnLogout, viewProfileButton, viewGraphButton;
    private DatabaseReference weatherReference;
    private String currentUsername;
    private SharedPreferences sharedPreferences;
    private RainfallNotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        notificationHelper = new RainfallNotificationHelper(this);

        // Ambil username dari SharedPreferences
        sharedPreferences = getSharedPreferences("SkySensePrefs", Context.MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", null);
        String selectedDevice = sharedPreferences.getString("selected_device", "uid=2/deviceid=2A");

        if (currentUsername == null) {
            redirectToLogin();
            return;
        }

        // Inisialisasi TextView untuk menampilkan data pengguna
        btnLogout = findViewById(R.id.idLogout);
        viewProfileButton = findViewById(R.id.idEditProfile);
        viewGraphButton = findViewById(R.id.idViewGraph);

        // Inisialisasi TextView untuk data cuaca
        HumidityValue = findViewById(R.id.idKelembapan);
        TemperatureValue = findViewById(R.id.idSuhu);
        PressureValue = findViewById(R.id.idTekanan);
        RainfallValue = findViewById(R.id.idHujan);
        WindSpeedValue = findViewById(R.id.idAngin);
        UVIndexValue = findViewById(R.id.idUV);
        RainPredictionValue = findViewById(R.id.idPrediksi);
        WeatherConditionValue = findViewById(R.id.idKondisiCuaca);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        // Inisialisasi Firebase reference dengan device yang dipilih
        weatherReference = FirebaseDatabase.getInstance()
                .getReference(selectedDevice + "/latest_reading");

        // Listener untuk data cuaca
        loadWeatherData();

        // Tombol Logout
        btnLogout.setOnClickListener(v -> logout());

        // Tombol Lihat Profil
        viewProfileButton.setOnClickListener(v -> viewProfile());

        // Tombol Lihat Grafik Cuaca
        viewGraphButton.setOnClickListener(v -> {
            try {
                Intent graphIntent = new Intent(MainActivity.this, GraphWeatherActivity.class);
                startActivity(graphIntent);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Error: Unable to open Graph Activity", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update reference when returning to activity in case device selection changed
        String selectedDevice = sharedPreferences.getString("selected_device", "uid=2/deviceid=2A");
        weatherReference = FirebaseDatabase.getInstance()
                .getReference(selectedDevice + "/latest_reading");
        loadWeatherData();
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
                    HumidityValue.setText(humidity != null ? humidity + "%" : "N/A");
                    TemperatureValue.setText(temperature != null ? temperature + "°C" : "N/A");
                    PressureValue.setText(formattedPressure != null ? formattedPressure + " hPa" : "N/A");
                    RainfallValue.setText(rainfall != null ? rainfall + " mm" : "N/A");
                    WindSpeedValue.setText(windSpeed != null ? windSpeed + " km/h" : "N/A");
                    UVIndexValue.setText(uvIndex != null ? String.valueOf(uvIndex) : "N/A");
                    RainPredictionValue.setText(rainPrediction != null ? rainPrediction : "N/A");
                    WeatherConditionValue.setText(weatherCondition != null ? weatherCondition : "N/A");

                    if (rainfall != null) {
                        notificationHelper.checkRainfallAndNotify(rainfall);
                    }
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