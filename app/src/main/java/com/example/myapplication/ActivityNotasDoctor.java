package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.psicapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ActivityNotasDoctor extends AppCompatActivity {

    private ListView listViewNotas;
    private ArrayList<String> notasList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notas_doctor);

        listViewNotas = findViewById(R.id.listViewNotas);
        notasList = new ArrayList<>();

        // Configurar el adaptador para el ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notasList);
        listViewNotas.setAdapter(adapter);

        // Cargar las notas del paciente
        cargarNotas();
    }

    private void cargarNotas() {
        // Obtener el ID del paciente desde SharedPreferences
        SharedPreferences preferences = getSharedPreferences("app_session", MODE_PRIVATE);
        int pacienteId = preferences.getInt("user" +
                "_id", -1);

        // Verificar si el ID del paciente está disponible
        if (pacienteId == -1) {
            Toast.makeText(this, "No se pudo obtener el ID del paciente", Toast.LENGTH_SHORT).show();
            Log.e("Error", "paciente_id no encontrado en SharedPreferences");
            return;
        }

        // URL del endpoint para obtener las notas
        String url = "http://192.168.100.115/psicapp/obtener_notas_paciente.php?paciente_id=" + pacienteId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            JSONArray notasArray = response.getJSONArray("notas");

                            notasList.clear();
                            for (int i = 0; i < notasArray.length(); i++) {
                                JSONObject nota = notasArray.getJSONObject(i);
                                String notaTexto = nota.getString("nota");
                                String fecha = nota.getString("fecha_creacion");

                                notasList.add("Fecha: " + fecha + "\nNota: " + notaTexto);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            String message = response.getString("message");
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("Error", "Error procesando la respuesta JSON");
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error al cargar las notas", Toast.LENGTH_SHORT).show();
                }
        );

        // Añadir la solicitud a la cola de Volley
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }


}
