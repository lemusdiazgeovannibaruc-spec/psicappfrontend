package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.psicapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ActivityReseñas extends AppCompatActivity {

    private ListView lvDoctores;
    private ArrayList<String> doctoresList;
    private ArrayList<Integer> doctorIds;
    private ArrayAdapter<String> doctoresAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resenas);

        lvDoctores = findViewById(R.id.lvDoctores);
        doctoresList = new ArrayList<>();
        doctorIds = new ArrayList<>();
        doctoresAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, doctoresList);
        lvDoctores.setAdapter(doctoresAdapter);

        cargarDoctoresRelacionados();

        lvDoctores.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            int doctorId = doctorIds.get(position);
            Intent intent = new Intent(ActivityReseñas.this, ActivityAgregarReseña.class);
            intent.putExtra("doctor_id", doctorId);
            startActivity(intent);
        });
    }

    private void cargarDoctoresRelacionados() {
        SharedPreferences preferences = getSharedPreferences("app_session", MODE_PRIVATE);
        int pacienteId = preferences.getInt("user_id", -1);

        if (pacienteId == -1) {
            Toast.makeText(this, "No se pudo obtener el ID del paciente", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://192.168.100.115/psicapp/listar_doctores_paciente.php?paciente_id=" + pacienteId;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    doctoresList.clear();
                    doctorIds.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject doctor = response.getJSONObject(i);
                            int doctorId = doctor.getInt("id");
                            String nombre = doctor.getString("nombre");

                            doctorIds.add(doctorId);
                            doctoresList.add(nombre);
                        }
                        doctoresAdapter.notifyDataSetChanged();
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

        Volley.newRequestQueue(this).add(jsonArrayRequest);
    }
}
