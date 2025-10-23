package com.example.myapplication;

import android.os.Bundle;
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

public class ActivityVerReseñas extends AppCompatActivity {

    private ListView lvReseñas;
    private ArrayList<Reseña> reseñasList;
    private ReseñaAdapter reseñaAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_resenas);

        lvReseñas = findViewById(R.id.lvReseñas);
        reseñasList = new ArrayList<>();
        reseñaAdapter = new ReseñaAdapter(this, reseñasList);
        lvReseñas.setAdapter(reseñaAdapter);

        // Obtener el ID del doctor desde el Intent
        int doctorId = getIntent().getIntExtra("doctorId", -1);

        if (doctorId == -1) {
            Toast.makeText(this, "Error al obtener el ID del doctor", Toast.LENGTH_SHORT).show();
            return;
        }

        cargarReseñas(doctorId);
    }

    private void cargarReseñas(int doctorId) {
        String url = "http://192.168.100.115/psicapp/ver_resenas.php?doctor_id=" + doctorId;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    reseñasList.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject reseña = response.getJSONObject(i);
                            String pacienteNombre = reseña.getString("paciente_nombre");
                            int calificacion = reseña.getInt("calificacion");
                            String comentario = reseña.getString("comentario");

                            reseñasList.add(new Reseña(pacienteNombre, calificacion, comentario));
                        }
                        reseñaAdapter.notifyDataSetChanged();
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
