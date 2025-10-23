package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

public class ActivityMensajeriaPaciente extends AppCompatActivity {

    private ListView listViewDoctores;
    private ArrayList<String> doctoresList;
    private ArrayList<Integer> doctoresIds;
    private ArrayAdapter<String> doctoresAdapter;

    private int pacienteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajeria_paciente);

        listViewDoctores = findViewById(R.id.listViewDoctores);

        doctoresList = new ArrayList<>();
        doctoresIds = new ArrayList<>();
        doctoresAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, doctoresList);
        listViewDoctores.setAdapter(doctoresAdapter);

        pacienteId = getSharedPreferences("app_session", MODE_PRIVATE).getInt("user_id", -1);

        if (pacienteId == -1) {
            Toast.makeText(this, "No se pudo obtener el ID del paciente", Toast.LENGTH_SHORT).show();
            Log.e("MensajeríaPaciente", "ID del paciente no encontrado en SharedPreferences");
            return;
        }

        cargarDoctores();

        listViewDoctores.setOnItemClickListener((parent, view, position, id) -> {
            int doctorId = doctoresIds.get(position);
            String doctorNombre = doctoresList.get(position);

            Intent intent = new Intent(ActivityMensajeriaPaciente.this, ActivityConversacionPaciente.class);
            intent.putExtra("paciente_id", pacienteId);
            intent.putExtra("doctor_id", doctorId);
            intent.putExtra("doctor_nombre", doctorNombre);
            startActivity(intent);
        });
    }

    private void cargarDoctores() {
        String url = "http://192.168.100.115/psicapp/obtener_doctores_paciente.php?paciente_id=" + pacienteId;
        Log.d("cargarDoctores", "URL: " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.d("cargarDoctores", "Respuesta: " + response.toString());
                        boolean success = response.getBoolean("success");
                        if (success) {
                            doctoresList.clear();
                            doctoresIds.clear();
                            JSONArray doctoresArray = response.getJSONArray("doctores");
                            for (int i = 0; i < doctoresArray.length(); i++) {
                                JSONObject doctor = doctoresArray.getJSONObject(i);
                                int id = doctor.getInt("id");
                                String nombre = doctor.getString("nombre");

                                doctoresIds.add(id);
                                doctoresList.add(nombre);
                            }
                            doctoresAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "Error al cargar la lista de doctores", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar la lista de doctores", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("cargarDoctores", "Error: " + error.toString());
                    Toast.makeText(this, "Error al cargar doctores", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}
