package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.psicapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ActivityGenerarCita extends AppCompatActivity {

    private TextView tvDoctorNombre;
    private ListView lvHorariosDisponibles;
    private ArrayAdapter<String> horariosAdapter;
    private ArrayList<String> horariosList;
    private Button btnConfirmarCita;

    private int doctorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generar_cita);

        tvDoctorNombre = findViewById(R.id.tvDoctorNombre);
        lvHorariosDisponibles = findViewById(R.id.lvHorariosDisponibles);
        btnConfirmarCita = findViewById(R.id.btnConfirmarCita);

        horariosList = new ArrayList<>();
        horariosAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, horariosList);
        lvHorariosDisponibles.setAdapter(horariosAdapter);
        lvHorariosDisponibles.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        Intent intent = getIntent();
        doctorId = intent.getIntExtra("doctorId", -1);
        String doctorNombre = intent.getStringExtra("doctorNombre");

        tvDoctorNombre.setText("Doctor: " + doctorNombre);

        cargarHorariosDisponibles();

        btnConfirmarCita.setOnClickListener(v -> confirmarCita());
    }

    private void cargarHorariosDisponibles() {
        String url = "http://192.168.100.115/psicapp/listar_horarios_disponibles.php?doctor_id=" + doctorId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            horariosList.clear();
                            JSONArray horarios = response.getJSONArray("horarios");
                            for (int i = 0; i < horarios.length(); i++) {
                                JSONObject horario = horarios.getJSONObject(i);
                                String fecha = horario.getString("fecha");
                                String horaInicio = horario.getString("hora_inicio");
                                String horaFin = horario.getString("hora_fin");

                                horariosList.add(fecha + " | " + horaInicio + " - " + horaFin);
                            }
                            horariosAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "No hay horarios disponibles", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void confirmarCita() {
        int selectedPosition = lvHorariosDisponibles.getCheckedItemPosition();
        if (selectedPosition == -1) {
            Toast.makeText(this, "Por favor, selecciona un horario", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences preferences = getSharedPreferences("app_session", MODE_PRIVATE);
        int pacienteId = preferences.getInt("user_id", -1);

        if (pacienteId == -1) {
            Toast.makeText(this, "Error al obtener el ID del paciente. Inicia sesión nuevamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        String horarioSeleccionado = horariosList.get(selectedPosition);
        String[] parts = horarioSeleccionado.split(" \\| ");
        String fecha = parts[0];
        String[] horas = parts[1].split(" - ");
        String horaInicio = horas[0];
        String horaFin = horas[1];

        String url = "http://192.168.100.115/psicapp/crear_cita.php";

        JSONObject params = new JSONObject();
        try {
            params.put("doctor_id", doctorId);
            params.put("fecha", fecha);
            params.put("hora_inicio", horaInicio);
            params.put("hora_fin", horaFin);
            params.put("paciente_id", pacienteId); // Usa el ID del paciente desde SharedPreferences
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al preparar los datos", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(this, "Cita creada exitosamente", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Error: " + response.getString("message"), Toast.LENGTH_SHORT).show();
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