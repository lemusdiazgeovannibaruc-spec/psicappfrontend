package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
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

public class ActivityMensajeriaDoctor extends AppCompatActivity {

    private Spinner spinnerPacientes;
    private ListView listViewMensajes;
    private ArrayList<String> pacientesList;
    private ArrayList<Integer> pacientesIds;
    private ArrayAdapter<String> pacientesAdapter;

    private ArrayList<String> mensajesList;
    private ArrayAdapter<String> mensajesAdapter;

    private int doctorId;
    private int selectedPacienteId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajeria_doctor);

        spinnerPacientes = findViewById(R.id.spinnerPacientes);
        listViewMensajes = findViewById(R.id.listViewMensajes);

        pacientesList = new ArrayList<>();
        pacientesIds = new ArrayList<>();
        pacientesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, pacientesList);
        pacientesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPacientes.setAdapter(pacientesAdapter);

        mensajesList = new ArrayList<>();
        mensajesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mensajesList);
        listViewMensajes.setAdapter(mensajesAdapter);

        doctorId = getSharedPreferences("app_session", MODE_PRIVATE).getInt("doctor_id", -1);

        if (doctorId == -1) {
            Toast.makeText(this, "No se pudo obtener el ID del doctor", Toast.LENGTH_SHORT).show();
            Log.e("Mensajería", "ID del doctor no encontrado en SharedPreferences");
            return;
        }

        cargarPacientes();

        spinnerPacientes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!pacientesIds.isEmpty()) {
                    selectedPacienteId = pacientesIds.get(position);
                    cargarMensajes();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });

        listViewMensajes.setOnItemClickListener((parent, view, position, id) -> {
            if (selectedPacienteId != 0) {
                Intent intent = new Intent(ActivityMensajeriaDoctor.this, ActivityConversacion.class);
                intent.putExtra("doctor_id", doctorId);
                intent.putExtra("paciente_id", selectedPacienteId);
                intent.putExtra("paciente_nombre", pacientesList.get(spinnerPacientes.getSelectedItemPosition()));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Por favor selecciona un paciente", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarPacientes() {
        String url = "http://192.168.100.115/psicapp/obtener_pacientes_doctor.php?doctor_id=" + doctorId;
        Log.d("cargarPacientes", "URL: " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.d("cargarPacientes", "Respuesta: " + response.toString());
                        boolean success = response.getBoolean("success");
                        if (success) {
                            pacientesList.clear();
                            pacientesIds.clear();
                            JSONArray pacientesArray = response.getJSONArray("pacientes");
                            for (int i = 0; i < pacientesArray.length(); i++) {
                                JSONObject paciente = pacientesArray.getJSONObject(i);
                                int id = paciente.getInt("id");
                                String nombre = paciente.getString("nombre");

                                pacientesIds.add(id);
                                pacientesList.add(nombre);
                            }
                            pacientesAdapter.notifyDataSetChanged();

                            if (!pacientesIds.isEmpty()) {
                                selectedPacienteId = pacientesIds.get(0);
                                cargarMensajes();
                            }
                        } else {
                            Toast.makeText(this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar pacientes", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("cargarPacientes", "Error: " + error.toString());
                    Toast.makeText(this, "Error al cargar pacientes", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void cargarMensajes() {
        if (selectedPacienteId == 0) return;

        String url = "http://192.168.100.115/psicapp/obtener_mensajes.php?doctor_id=" + doctorId + "&paciente_id=" + selectedPacienteId;
        Log.d("cargarMensajes", "URL: " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.d("cargarMensajes", "Respuesta: " + response.toString());
                        boolean success = response.getBoolean("success");
                        if (success) {
                            mensajesList.clear();
                            JSONArray mensajesArray = response.getJSONArray("mensajes");
                            for (int i = 0; i < mensajesArray.length(); i++) {
                                JSONObject mensaje = mensajesArray.getJSONObject(i);
                                String contenido = mensaje.getString("contenido");
                                String fecha = mensaje.getString("fecha");

                                mensajesList.add(fecha + ": " + contenido);
                            }
                            mensajesAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "No se pudieron cargar los mensajes", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar los mensajes", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("cargarMensajes", "Error: " + error.toString());
                    Toast.makeText(this, "Error al cargar los mensajes", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}
