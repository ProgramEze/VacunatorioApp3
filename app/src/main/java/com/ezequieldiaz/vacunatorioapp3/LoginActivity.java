package com.ezequieldiaz.vacunatorioapp3;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ezequieldiaz.vacunatorioapp3.databinding.ActivityLoginBinding;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity implements SensorEventListener {
    private LoginActivityViewModel vm;
    private ActivityLoginBinding binding;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final String SHAKE_ACTION = "com.ezediaz.inmobiliaria.SHAKE_DETECTED";

    private BroadcastReceiver shakeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SHAKE_ACTION)) {
                makePhoneCall("2657312733");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        solicitarPermisos();
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        vm = new ViewModelProvider.AndroidViewModelFactory(getApplication()).create(LoginActivityViewModel.class);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        binding.btnLogin.setOnClickListener(view -> {
            String email = binding.etEmail.getText().toString();
            String password = binding.etPassword.getText().toString();
            vm.logueo(email, password);
            binding.etPassword.setText("");
        });
        binding.btnLogin.setOnClickListener(view -> vm.logueo(binding.etEmail.getText().toString(), binding.etPassword.getText().toString()));
        binding.btnFingerprint.setOnClickListener(v -> iniciarAutenticacionBiometrica());
        binding.tvCambiarPassword.setOnClickListener(v -> vm.enviarEmail(binding.etEmail.getText().toString()));
        executor = ContextCompat.getMainExecutor(this);
        vm.iniciarAutenticacionBiometrica(this);
        iniciarAutenticacionBiometrica();
    }


    // Método para llamar al método en el ViewModel
    private void iniciarAutenticacionBiometrica() {
        vm.iniciarAutenticacionBiometrica(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        IntentFilter filter = new IntentFilter(SHAKE_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(shakeReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(shakeReceiver);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        vm.onSensorChanged(event, this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No se necesita implementar
    }

    private void makePhoneCall(String phoneNumber) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1001);
            return;
        }
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(callIntent);
    }

    private void solicitarPermisos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }
    }
}

