package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.example.psicapp.R;

import org.json.JSONObject;

public class DoctorDashboardActivity extends AppCompatActivity {

    private ImageView imgFotoPerfilDoctor;
    private TextView txtBienvenidoDoctor;
    private Button btnGestionHorarios, btnVerCitas, btnAgregarNotas, btnMensajeria;
    private int doctorId; // ID del doctor obtenido del SharedPreferences

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);

        // Recuperar el ID del doctor desde SharedPreferences
        SharedPreferences preferences = getSharedPreferences("app_session", Context.MODE_PRIVATE);
        doctorId = preferences.getInt("user_id", -1); // Cambia "doctor_id" por la clave que usas para guardar el ID

        // Verificar si el ID es válido
        if (doctorId == -1) {
            Toast.makeText(this, "Error: Doctor no identificado. Inicia sesión nuevamente.", Toast.LENGTH_SHORT).show();
            finish(); // Finaliza la actividad si no se encuentra el ID
            return;
        }

        // Enlazar vistas
        imgFotoPerfilDoctor = findViewById(R.id.imgFotoPerfilDoctor);
        txtBienvenidoDoctor = findViewById(R.id.txtBienvenidoDoctor);
        btnGestionHorarios = findViewById(R.id.btnGestionHorarios);
        btnVerCitas = findViewById(R.id.btnVerCitas);
        btnAgregarNotas = findViewById(R.id.btnAgregarNotas);
        btnMensajeria = findViewById(R.id.btnMensajeria);

        // Cargar datos del doctor
        cargarDatosDoctor();

        // Configurar botones
        btnGestionHorarios.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorDashboardActivity.this, GestionHorariosActivity.class);
            startActivity(intent);
        });

        btnVerCitas.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorDashboardActivity.this, VerCitasActivity.class);
            startActivity(intent);
        });

        btnAgregarNotas.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorDashboardActivity.this, AgregarNotasActivity.class);
            startActivity(intent);
        });

        btnMensajeria.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorDashboardActivity.this, ActivityMensajeriaDoctor.class);
            startActivity(intent);
        });
    }

    private void cargarDatosDoctor() {
        String url = "http://192.168.100.115/psicapp/obtener_datos_doctor.php?doctor_id=" + doctorId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        Log.d("DoctorDashboard", "Respuesta del servidor: " + response);

                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            String nombre = jsonObject.getString("nombre");
                            String fotoPerfilBase64 = jsonObject.optString("foto_perfil", "");

                            // Actualizar texto de bienvenida
                            txtBienvenidoDoctor.setText("Bienvenido, Dr. " + nombre);

                            // Decodificar y mostrar la imagen
                            if (!fotoPerfilBase64.isEmpty()) {
                                try {
                                    byte[] decodedString = Base64.decode(fotoPerfilBase64, Base64.DEFAULT);
                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    imgFotoPerfilDoctor.setImageBitmap(decodedByte);
                                } catch (Exception e) {
                                    Log.e("DoctorDashboard", "Error al decodificar la imagen", e);
                                    imgFotoPerfilDoctor.setImageResource(R.drawable.ic_profile);
                                }
                            } else {
                                Log.e("DoctorDashboard", "La cadena Base64 está vacía");
                                imgFotoPerfilDoctor.setImageResource(R.drawable.ic_profile); // Placeholder
                            }
                        } else {
                            String errorMessage = jsonObject.optString("message", "Error desconocido");
                            Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                            imgFotoPerfilDoctor.setImageResource(R.drawable.ic_profile); // Placeholder
                        }
                    } catch (Exception e) {
                        Log.e("DoctorDashboard", "Error al procesar datos del servidor", e);
                        Toast.makeText(this, "Error al procesar datos", Toast.LENGTH_SHORT).show();
                        imgFotoPerfilDoctor.setImageResource(R.drawable.ic_profile); // Placeholder
                    }
                },
                error -> {
                    Log.e("DoctorDashboard", "Error al conectar con el servidor", error);
                    Toast.makeText(this, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show();
                });

        ApiHandler.getInstance(this).addToRequestQueue(stringRequest);
    }


    }
