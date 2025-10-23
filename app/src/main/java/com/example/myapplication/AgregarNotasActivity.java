package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.psicapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AgregarNotasActivity extends AppCompatActivity {

    private Spinner spinnerPacientes;
    private EditText etNota;
    private Button btnGuardarNota;
    private ArrayList<String> pacientesList;
    private ArrayList<Integer> pacienteIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_notas);

        spinnerPacientes = findViewById(R.id.spinnerPacientes);
        etNota = findViewById(R.id.etNota);
        btnGuardarNota = findViewById(R.id.btnGuardarNota);

        pacientesList = new ArrayList<>();
        pacienteIds = new ArrayList<>();

        cargarPacientesRelacionados();

        btnGuardarNota.setOnClickListener(v -> guardarNota());
    }

    private void cargarPacientesRelacionados() {
        SharedPreferences preferences = getSharedPreferences("app_session", MODE_PRIVATE);
        int doctorId = preferences.getInt("doctor_id", -1);

        if (doctorId == -1) {
            Toast.makeText(this, "No se pudo obtener el ID del doctor", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://192.168.100.115/psicapp/listar_pacientes_doctor.php?doctor_id=" + doctorId;


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            JSONArray pacientesArray = response.getJSONArray("pacientes");

                            // Limpiar listas para evitar duplicados
                            pacientesList.clear();
                            pacienteIds.clear();

                            // Procesar cada paciente del JSONArray
                            for (int i = 0; i < pacientesArray.length(); i++) {
                                JSONObject paciente = pacientesArray.getJSONObject(i);

                                int pacienteId = paciente.getInt("paciente_id");
                                String nombre = paciente.getString("paciente_nombre");

                                // Añadir a las listas
                                pacienteIds.add(pacienteId);
                                pacientesList.add(nombre);
                            }

                            // Configurar el adaptador del Spinner
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    this,
                                    android.R.layout.simple_spinner_item,
                                    pacientesList
                            );
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerPacientes.setAdapter(adapter);

                        } else {
                            String message = response.getString("message");
                            Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error procesando la respuesta JSON", Toast.LENGTH_SHORT).show();
                    }

                },
                error -> {
                    error.printStackTrace();
                    Log.e("Error", "Error en la solicitud: " + error.getMessage());
                }
        );

// Añadir la solicitud a la cola de Volley
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        requestQueue.add(jsonObjectRequest);

    }

    private void guardarNota() {
        // Obtener el ID del doctor desde SharedPreferences
        SharedPreferences preferences = getSharedPreferences("app_session", MODE_PRIVATE);
        int doctorId = preferences.getInt("user_id", -1);

        if (doctorId == -1) {
            Toast.makeText(this, "No se pudo obtener el ID del doctor", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener el ID del paciente seleccionado y la nota escrita
        int pacienteId = pacienteIds.get(spinnerPacientes.getSelectedItemPosition());
        String nota = etNota.getText().toString().trim();

        if (nota.isEmpty()) {
            Toast.makeText(this, "Por favor, escribe una nota", Toast.LENGTH_SHORT).show();
            return;
        }

        // Configurar URL del servidor
        String url = "http://192.168.100.115/psicapp/guardar_nota.php";

        // Crear la solicitud POST
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                response -> Toast.makeText(this, "Nota guardada exitosamente", Toast.LENGTH_SHORT).show(),
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error al guardar la nota", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("paciente_id", String.valueOf(pacienteId)); // ID del paciente
                params.put("doctor_id", String.valueOf(doctorId));    // ID del doctor
                params.put("nota", nota);                             // Contenido de la nota
                return params;
            }
        };

        // Enviar la solicitud a través de Volley
        Volley.newRequestQueue(this).add(stringRequest);
    }


}
