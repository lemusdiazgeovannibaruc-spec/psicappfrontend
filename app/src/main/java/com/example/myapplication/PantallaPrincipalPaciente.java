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

public class PantallaPrincipalPaciente extends AppCompatActivity {

    private ImageView imgFotoPerfil;
    private TextView txtBienvenido;
    private Button btnAgendarCita, btnReseñas, btnNotasDoctor, btnMensajeria;
    private int userId; // ID del usuario obtenido del SharedPreferences

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_principal_paciente);

        // Recuperar el ID del usuario desde SharedPreferences
        SharedPreferences preferences = getSharedPreferences("app_session", Context.MODE_PRIVATE);
        userId = preferences.getInt("user_id", -1); // Cambia "user_id" por la clave que estás usando para guardar el ID

        // Verificar si el ID es válido
        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no identificado. Inicia sesión nuevamente.", Toast.LENGTH_SHORT).show();
            finish(); // Finaliza la actividad si no se encuentra el ID
            return;
        }

        // Enlazar vistas
        imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
        txtBienvenido = findViewById(R.id.txtBienvenido);
        btnAgendarCita = findViewById(R.id.btnAgendarCita);
        btnReseñas = findViewById(R.id.btnReseñas);
        btnNotasDoctor = findViewById(R.id.btnNotasDoctor);
        btnMensajeria = findViewById(R.id.btnMensajeria);

        // Cargar datos del usuario
        cargarDatosUsuario();

        // Configurar botones
        btnAgendarCita.setOnClickListener(v -> {
            Intent intent = new Intent(PantallaPrincipalPaciente.this, ActivityPacienteDashboard.class);
            startActivity(intent);
        });

        btnReseñas.setOnClickListener(v -> {
            Intent intent = new Intent(PantallaPrincipalPaciente.this, ActivityReseñas.class);
            startActivity(intent);
        });

        btnNotasDoctor.setOnClickListener(v -> {
            Intent intent = new Intent(PantallaPrincipalPaciente.this, ActivityNotasDoctor.class);
            startActivity(intent);
        });

        btnMensajeria.setOnClickListener(v -> {
            Intent intent = new Intent(PantallaPrincipalPaciente.this, ActivityMensajeriaPaciente.class);
            startActivity(intent);
        });
    }

    private void cargarDatosUsuario() {
        String url = "http://192.168.100.115/psicapp/obtener_datos_usuario.php?user_id=" + userId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        // Log para verificar la respuesta completa
                        Log.d("UsuarioDashboard", "Respuesta del servidor: " + response);

                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            String nombre = jsonObject.getString("nombre");
                            String fotoPerfilBase64 = jsonObject.getString("foto_perfil");

                            // Actualizar texto de bienvenida
                            txtBienvenido.setText("Bienvenido, " + nombre);

                            // Decodificar y cargar la imagen Base64
                            if (fotoPerfilBase64 != null && !fotoPerfilBase64.isEmpty()) {
                                try {
                                    byte[] decodedString = Base64.decode(fotoPerfilBase64, Base64.DEFAULT);
                                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    imgFotoPerfil.setImageBitmap(decodedBitmap);
                                    Log.d("UsuarioDashboard", "Imagen cargada correctamente.");
                                } catch (IllegalArgumentException e) {
                                    Log.e("UsuarioDashboard", "Error al decodificar Base64: " + e.getMessage());
                                    imgFotoPerfil.setImageResource(R.drawable.ic_profile); // Placeholder en caso de error
                                }
                            } else {
                                Log.e("UsuarioDashboard", "Foto de perfil vacía o no válida.");
                                imgFotoPerfil.setImageResource(R.drawable.ic_profile); // Placeholder
                            }
                        } else {
                            Toast.makeText(this, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show();
                            imgFotoPerfil.setImageResource(R.drawable.ic_profile); // Placeholder
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar datos del servidor", Toast.LENGTH_SHORT).show();
                        imgFotoPerfil.setImageResource(R.drawable.ic_profile); // Placeholder
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show();
                    imgFotoPerfil.setImageResource(R.drawable.ic_profile); // Placeholder
                });

        ApiHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

}
