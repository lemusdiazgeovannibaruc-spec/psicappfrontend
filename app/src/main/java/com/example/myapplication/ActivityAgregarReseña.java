package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.psicapp.R;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityAgregarReseña extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText etComentario;
    private Button btnGuardarReseña;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_resena);

        ratingBar = findViewById(R.id.ratingBar);
        etComentario = findViewById(R.id.etComentario);
        btnGuardarReseña = findViewById(R.id.btnGuardarReseña);

        btnGuardarReseña.setOnClickListener(v -> guardarReseña());
    }

    private void guardarReseña() {
        float calificacion = ratingBar.getRating();
        String comentario = etComentario.getText().toString().trim();

        if (calificacion == 0) {
            Toast.makeText(this, "Por favor selecciona una calificación", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener los IDs del paciente y doctor desde los extras o SharedPreferences
        SharedPreferences preferences = getSharedPreferences("app_session", MODE_PRIVATE);
        int pacienteId = preferences.getInt("user_id", -1);
        int doctorId = getIntent().getIntExtra("doctor_id", -1);

        if (doctorId == -1) {
            Toast.makeText(this, "No se pudo obtener el ID del doctor", Toast.LENGTH_SHORT).show();
            finish();
        }


        String url = "http://192.168.100.115/psicapp/guardar_reseña.php";

        JSONObject params = new JSONObject();
        try {
            params.put("paciente_id", pacienteId);
            params.put("doctor_id", doctorId);
            params.put("calificacion", calificacion);
            params.put("comentario", comentario);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al preparar los datos", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, params,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(this, "Reseña guardada exitosamente", Toast.LENGTH_SHORT).show();
                            finish(); // Cerrar la actividad
                        } else {
                            String message = response.getString("message");
                            Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}
