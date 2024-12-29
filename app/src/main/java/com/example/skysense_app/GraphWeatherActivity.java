package com.example.skysense_app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class GraphWeatherActivity extends AppCompatActivity {

    private LineChart rainChart, humidityChart, temperatureChart, windSpeedChart;
    private DatabaseReference weatherReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_weather);

        // Inisialisasi grafik
        rainChart = findViewById(R.id.rainfallChart);
        humidityChart = findViewById(R.id.humidityChart);
        temperatureChart = findViewById(R.id.temperatureChart);
        windSpeedChart = findViewById(R.id.windSpeedChart);

        // Referensi Firebase ke 'history'
        weatherReference = FirebaseDatabase.getInstance().getReference("uid=2/deviceid=2A/history");

        // Muat data dari Firebase
        loadWeatherData();
    }

    private void loadWeatherData() {
        weatherReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<Entry> rainEntries = new ArrayList<>();
                    List<Entry> humidityEntries = new ArrayList<>();
                    List<Entry> temperatureEntries = new ArrayList<>();
                    List<Entry> windSpeedEntries = new ArrayList<>();
                    List<String> timeLabels = new ArrayList<>();

                    int index = 0;

                    for (DataSnapshot data : snapshot.getChildren()) {

                        String timestamp = data.getKey();
                        Double rain = data.child("rainfall_mm").getValue(Double.class);
                        Double humidity = data.child("humidity").getValue(Double.class);
                        Double temperature = data.child("temperature").getValue(Double.class);
                        Double windSpeed = data.child("windSpeed_kmph").getValue(Double.class);

                        if (timestamp != null) {
                            timeLabels.add(formatTimestampToTime(timestamp)); // Menampilkan hanya jam dan menit dari key
                        } else {
                            timeLabels.add("N/A");
                        }

                        if (rain != null) {
                            rainEntries.add(new Entry(index, rain.floatValue()));
                        }
                        if (humidity != null) {
                            humidityEntries.add(new Entry(index, humidity.floatValue()));
                        }
                        if (temperature != null) {
                            temperatureEntries.add(new Entry(index, temperature.floatValue()));
                        }
                        if (windSpeed != null) {
                            windSpeedEntries.add(new Entry(index, windSpeed.floatValue()));
                        }

                        index++;
                    }

                    // Atur grafik untuk masing-masing parameter
                    setupChart(rainChart, rainEntries, "Rainfall (mm)", timeLabels);
                    setupChart(humidityChart, humidityEntries, "Humidity (%)", timeLabels);
                    setupChart(temperatureChart, temperatureEntries, "Temperature (°C)", timeLabels);
                    setupChart(windSpeedChart, windSpeedEntries, "Wind Speed (km/h)", timeLabels);

                } else {
                    Toast.makeText(GraphWeatherActivity.this, "No data available in history.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GraphWeatherActivity.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatTimestampToTime(String timestamp) {
        // Timestamp format is "yyyy-MM-dd_HH-mm-ss" in the key, so split and extract HH:mm from the key
        try {
            // Split timestamp by "_" to get time part (HH-mm-ss)
            String[] parts = timestamp.split("_");  // Split "2024-12-05_15-37-59"
            if (parts.length > 1) {
                String timePart = parts[1];  // "15-37-59"
                timePart = timePart.substring(0, 5);  // Extract "15:37" from "15-37-59"
                return timePart;  // Return only "HH:mm"
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";  // If there's an error, return "N/A"
    }

    private void setupChart(LineChart chart, List<Entry> entries, String label, List<String> timeLabels) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setValueTextSize(0f); // Hilangkan nilai di titik data
        dataSet.setDrawCircles(false); // Hilangkan lingkaran di grafik
        dataSet.setLineWidth(2f); // Tebalkan garis

        // Warna garis
        switch (label) {
            case "Rainfall (mm)":
                dataSet.setColor(getResources().getColor(android.R.color.holo_blue_dark));
                break;
            case "Humidity (%)":
                dataSet.setColor(getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "Temperature (°C)":
                dataSet.setColor(getResources().getColor(android.R.color.holo_red_dark));
                break;
            case "Wind Speed (km/h)":
                dataSet.setColor(getResources().getColor(android.R.color.holo_orange_dark));
                break;
        }

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // Atur X-Axis menggunakan timeLabels
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < timeLabels.size()) {
                    return timeLabels.get(index);
                }
                return "";
            }
        });
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        // Desain umum grafik
        chart.getAxisRight().setEnabled(false); // Nonaktifkan sumbu kanan
        chart.getAxisLeft().setDrawGridLines(false); // Hilangkan garis grid horizontal
        chart.setTouchEnabled(true); // Aktifkan interaksi sentuh
        chart.setPinchZoom(true); // Aktifkan pinch-to-zoom
        chart.getDescription().setEnabled(false); // Hilangkan deskripsi default
        chart.invalidate(); // Refresh grafik
    }
}
