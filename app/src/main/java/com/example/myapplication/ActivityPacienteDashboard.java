package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class ActivityPacienteDashboard extends AppCompatActivity {

    private EditText etBuscarDoctor;
    private Button btnBuscar;
    private ListView lvDoctores;
    private DoctorAdapter doctorAdapter;
    private ArrayList<Doctor> doctorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paciente_dashboard);

        etBuscarDoctor = findViewById(R.id.etBuscarDoctor);
        btnBuscar = findViewById(R.id.btnBuscar);
        lvDoctores = findViewById(R.id.lvDoctores);

        doctorList = new ArrayList<>();
        doctorAdapter = new DoctorAdapter(this, doctorList);
        lvDoctores.setAdapter(doctorAdapter);

        btnBuscar.setOnClickListener(v -> buscarDoctores());
    }

    private void buscarDoctores() {
        String search = etBuscarDoctor.getText().toString().trim();
        if (search.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa un término de búsqueda", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://192.168.100.115/psicapp/buscar_doctores.php?search=" + search;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            doctorList.clear();
                            JSONArray doctores = response.getJSONArray("doctores");
                            for (int i = 0; i < doctores.length(); i++) {
                                JSONObject doctor = doctores.getJSONObject(i);
                                doctorList.add(new Doctor(
                                        doctor.getInt("id"),
                                        doctor.getString("nombre"),
                                        doctor.getString("especialidad"),
                                        doctor.getString("ubicacion")
                                ));
                            }
                            doctorAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "No se encontraron resultados", Toast.LENGTH_SHORT).show();
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
}
