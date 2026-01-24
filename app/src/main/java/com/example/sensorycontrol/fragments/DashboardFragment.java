package com.example.sensorycontrol.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sensorycontrol.R;
import com.example.sensorycontrol.auth.AuthManager;
import com.google.firebase.auth.FirebaseUser;

/**
 * Dashboard - Shows overview of health data (placeholder for Phase 3)
 */
public class DashboardFragment extends Fragment {
    
    private TextView welcomeText;
    private TextView heartRateValue;
    private TextView spo2Value;
    private TextView temperatureValue;
    private TextView statusText;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        welcomeText = view.findViewById(R.id.welcome_text);
        heartRateValue = view.findViewById(R.id.heart_rate_value);
        spo2Value = view.findViewById(R.id.spo2_value);
        temperatureValue = view.findViewById(R.id.temperature_value);
        statusText = view.findViewById(R.id.status_text);
        
        // Load user info
        loadUserInfo();
        
        // Set placeholder values (Phase 3 will replace with real sensor data)
        heartRateValue.setText("--");
        spo2Value.setText("--");
        temperatureValue.setText("--");
        statusText.setText("No device connected");
    }
    
    private void loadUserInfo() {
        AuthManager authManager = AuthManager.getInstance();
        FirebaseUser user = authManager.getCurrentUser();
        
        if (user != null) {
            authManager.getUserProfile(new AuthManager.ProfileCallback() {
                @Override
                public void onSuccess(java.util.Map<String, Object> profile) {
                    String name = (String) profile.get("name");
                    if (name != null && !name.isEmpty()) {
                        welcomeText.setText("Welcome, " + name + "!");
                    }
                }
                
                @Override
                public void onFailure(String error) {
                    // Use email as fallback
                    if (user.getEmail() != null) {
                        welcomeText.setText("Welcome!");
                    }
                }
            });
        }
    }
    
    // TODO Phase 3: Add methods to receive BLE data
    // public void updateHeartRate(int bpm) { ... }
    // public void updateSpO2(int percentage) { ... }
    // public void updateTemperature(float celsius) { ... }
}
