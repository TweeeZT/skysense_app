package com.example.skysense_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editName, editEmail, editUsername, editPassword;
    private Button saveButton, deleteButton;
    private String currentUsername;
    private SharedPreferences sharedPreferences;
    private static final String BASE_URL = "http://10.0.2.2/skysense/"; // Ganti dengan domain Anda

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences("SkySensePrefs", Context.MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", null);

        // Inisialisasi view
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);

        // Redirect jika username tidak ditemukan
        if (currentUsername == null) {
            Toast.makeText(this, "Invalid session. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load user data
        loadUserData();

        deleteButton.setOnClickListener(view -> deleteAccount());

        // Tombol simpan perubahan
        saveButton.setOnClickListener(view -> saveChanges());
    }

    private void loadUserData() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, BASE_URL + "get_user.php",
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            JSONObject userData = jsonResponse.getJSONObject("user");

                            editName.setText(userData.getString("name"));
                            editEmail.setText(userData.getString("email"));
                            editUsername.setText(userData.getString("username"));
                            editPassword.setText(""); // Password field kosong untuk keamanan
                        } else {
                            Toast.makeText(EditProfileActivity.this,
                                    jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } catch (Exception e) {
                        Toast.makeText(EditProfileActivity.this,
                                "Error parsing data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                },
                error -> {
                    Toast.makeText(EditProfileActivity.this,
                            "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", currentUsername);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void saveChanges() {
        String newName = editName.getText().toString();
        String newEmail = editEmail.getText().toString();
        String newUsername = editUsername.getText().toString();
        String newPassword = editPassword.getText().toString();

        if (newName.isEmpty() || newEmail.isEmpty() || newUsername.isEmpty()) {
            Toast.makeText(this, "All fields except password are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, BASE_URL + "update_user.php",
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            // Update SharedPreferences jika username berubah
                            if (!currentUsername.equals(newUsername)) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("username", newUsername);
                                editor.apply();
                            }

                            Toast.makeText(EditProfileActivity.this,
                                    "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EditProfileActivity.this,
                                    jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(EditProfileActivity.this,
                                "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(EditProfileActivity.this,
                        "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("current_username", currentUsername);
                params.put("name", newName);
                params.put("email", newEmail);
                params.put("username", newUsername);
                if (!newPassword.isEmpty()) {
                    params.put("password", newPassword);
                }
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void deleteAccount() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, BASE_URL + "delete_user.php",
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            Toast.makeText(EditProfileActivity.this,
                                    "Account deleted successfully", Toast.LENGTH_SHORT).show();

                            // Clear SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.apply();

                            // Redirect ke LoginActivity
                            Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish(); // Tutup EditProfileActivity
                        } else {
                            Toast.makeText(EditProfileActivity.this,
                                    jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(EditProfileActivity.this,
                                "Error deleting account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(EditProfileActivity.this,
                        "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", currentUsername);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

}