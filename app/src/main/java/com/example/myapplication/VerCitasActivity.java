package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.psicapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VerCitasActivity extends AppCompatActivity {

    private ListView lvCitas;
    private CitasAdapter citasAdapter;
    private ArrayList<Cita> citasList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_citas);

        lvCitas = findViewById(R.id.lvCitas);
        citasList = new ArrayList<>();
        citasAdapter = new CitasAdapter(this, citasList);
        lvCitas.setAdapter(citasAdapter);

        cargarCitasAgendadas();
    }

    private void cargarCitasAgendadas() {
        // Obtener el ID del doctor de SharedPreferences
        SharedPreferences preferences = getSharedPreferences("app_session", MODE_PRIVATE);
        int doctorId = preferences.getInt("doctor_id", -1);

        if (doctorId == -1) {
            Toast.makeText(this, "Error al obtener el ID del doctor", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://192.168.100.115/psicapp/listar_citas.php?doctor_id=" + doctorId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            citasList.clear();
                            JSONArray citas = response.getJSONArray("citas");
                            for (int i = 0; i < citas.length(); i++) {
                                JSONObject cita = citas.getJSONObject(i);
                                String fecha = cita.getString("fecha");
                                String horaInicio = cita.getString("hora_inicio");
                                String horaFin = cita.getString("hora_fin");
                                String pacienteNombre = cita.getString("paciente_nombre");

                                citasList.add(new Cita(fecha, horaInicio, horaFin, pacienteNombre));
                            }
                            citasAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "No hay citas agendadas", Toast.LENGTH_SHORT).show();
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
