package com.ezequieldiaz.vacunatorioapp3.request;
import android.content.Context;
import android.content.SharedPreferences;

import com.ezequieldiaz.vacunatorioapp3.model.Agente;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public class ApiClient {
    public static final String URL = "http://192.168.1.13:5000/";
    private static MisEndPoints mep;

    public static MisEndPoints getEndPoints(){
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        mep = retrofit.create(MisEndPoints.class);
        return mep;
    }

    public interface MisEndPoints {
        @FormUrlEncoded
        @POST("Agentes/login")
        Call<String> login(@Field("Matricula") String u, @Field("Clave") String c);

        @GET("Agentes")
        Call<Agente> miPerfil(@Header("Authorization") String token);

        @PUT("Agentes")
        Call<String> modificarUsuario(@Header("Authorization") String token, @Body Agente agente);

        @FormUrlEncoded
        @PUT("Agentes/cambiarviejacontraseña")
        Call<Void> cambiarPassword(@Header("Authorization") String token, @Field("ClaveVieja") String claveVieja, @Field("ClaveNueva") String claveNueva, @Field("RepetirClaveNueva") String repetirClaveNueva);

        @FormUrlEncoded
        @POST("Agentes/olvidecontraseña")
        Call<Void> enviarEmail(@Field("email") String email);
    }

    public static void guardarToken(String token, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.apply();
    }

    public static String leerToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("token",null);
    }

    public static void eliminarToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", "");
        editor.apply();
    }
}
