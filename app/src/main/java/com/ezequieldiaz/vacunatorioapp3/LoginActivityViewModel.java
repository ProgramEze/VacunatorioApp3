package com.ezequieldiaz.vacunatorioapp3;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ezequieldiaz.vacunatorioapp3.model.Agente;
import com.ezequieldiaz.vacunatorioapp3.request.ApiClient;

import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivityViewModel extends AndroidViewModel {
    private SharedPreferences sharedPreferences;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private float acelVal;
    private float acelLast;
    private float shake;
    private static final String SHAKE_ACTION = "com.ezediaz.inmobiliaria.SHAKE_DETECTED";
    private Activity activity;

    public LoginActivityViewModel(@NonNull Application application) {
        super(application);
        sharedPreferences = application.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        executor = ContextCompat.getMainExecutor(application);
        acelVal = SensorManager.GRAVITY_EARTH;
        acelLast = SensorManager.GRAVITY_EARTH;
        shake = 0.00f;
    }

    public void onSensorChanged(SensorEvent event, Context context) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        acelLast = acelVal;
        acelVal = (float) Math.sqrt((double) (x * x + y * y + z * z));
        float delta = acelVal - acelLast;
        shake = shake * 0.9f + delta;
        if (shake > 12) {
            Intent intent = new Intent(SHAKE_ACTION);
            LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
        }
    }

    public void iniciarAutenticacionBiometrica(Activity activity) {
        this.activity = activity;
        biometricPrompt = new BiometricPrompt((FragmentActivity) activity, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.e("BiometricPrompt", "Authentication error: " + errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.d("BiometricPrompt", "Authentication succeeded");
                String matricula = "34229421";
                String password = "1234";
                logueo(matricula, password);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticación biométrica")
                .setSubtitle("Inicia sesión usando tu huella digital")
                .setNegativeButtonText("Usar contraseña")
                .build();
        biometricPrompt.authenticate(promptInfo);
    }

    public void logueo(String usuario, String clave) {
        ApiClient.MisEndPoints api = ApiClient.getEndPoints();
        Call<String> call = api.login(usuario, clave);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    String token = response.body();
                    guardarToken("Bearer " + token);
                    iniciarMainActivity();
                } else {
                    Toast.makeText(getApplication(), "Matricula o contraseña incorrecta", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable throwable) {
                Toast.makeText(getApplication(), "Falla en el inicio de sesión", Toast.LENGTH_LONG).show();
                Log.d("Login", "Falla en el inicio de sesión: " + throwable.getMessage());
            }
        });
    }

    public void enviarEmail(String email){
        ApiClient.MisEndPoints api = ApiClient.getEndPoints();
        if(!email.isEmpty()){
            Call<Void> call = api.enviarEmail(email);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getApplication(), "Email enviado a su correo para recuperar la contraseña", Toast.LENGTH_LONG).show();
                    } else {
                        Log.d("salida", response.message());
                        Toast.makeText(getApplication(), "Email incorrecto o no está registrado", Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable throwable) {
                    Toast.makeText(getApplication(), "Falla en la recuperación del email", Toast.LENGTH_LONG).show();
                    Log.d("salida", throwable.getMessage());
                }
            });
        } else {
            Toast.makeText(getApplication(), "Por favor ingrese un email para recuperar la contraseña", Toast.LENGTH_LONG).show();
        }
    }

    private void guardarToken(String token) {
        ApiClient.guardarToken(token, getApplication());
    }

    private void iniciarMainActivity() {
        sharedPreferences = getApplication().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        ApiClient.MisEndPoints api = ApiClient.getEndPoints();
        String token = ApiClient.leerToken(getApplication());
        Call<Agente> call = api.miPerfil(token);
        call.enqueue(new Callback<Agente>() {
            @Override
            public void onResponse(Call<Agente> call, Response<Agente> response) {
                if (response.isSuccessful()) {
                    editor.putString("nombre completo", response.body().toString());
                    editor.putString("email", response.body().getEmail());
                    editor.apply();
                } else {
                    Log.d("salida", response.message());
                }
            }

            @Override
            public void onFailure(Call<Agente> call, Throwable throwable) {
                Log.d("salida", throwable.getMessage());
            }
        });
        Intent intent = new Intent(getApplication(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpiar la pila de actividades
        getApplication().startActivity(intent);
    }

    public void handleBiometricAuthenticationSuccess() {
        logueo("diazezequiel777@gmail.com", "1234");
    }
}
