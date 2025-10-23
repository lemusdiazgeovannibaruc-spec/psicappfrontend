package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
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

public class ActivityConversacion extends AppCompatActivity {

    private int doctorId;
    private int pacienteId;
    private String pacienteNombre;

    private ListView listViewConversacion;
    private EditText editTextMensaje;
    private Button buttonEnviar;

    private ArrayList<String> mensajesList;
    private ArrayAdapter<String> mensajesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversacion);

        listViewConversacion = findViewById(R.id.listViewConversacion);
        editTextMensaje = findViewById(R.id.editTextMensaje);
        buttonEnviar = findViewById(R.id.buttonEnviar);

        mensajesList = new ArrayList<>();
        mensajesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mensajesList);
        listViewConversacion.setAdapter(mensajesAdapter);

        doctorId = getIntent().getIntExtra("doctor_id", -1);
        pacienteId = getIntent().getIntExtra("paciente_id", -1);
        pacienteNombre = getIntent().getStringExtra("paciente_nombre");

        if (doctorId == -1 || pacienteId == -1) {
            Toast.makeText(this, "Error al cargar la conversación", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setTitle("Conversación con " + pacienteNombre);

        cargarConversacion();

        buttonEnviar.setOnClickListener(v -> enviarMensaje());
    }

    private void cargarConversacion() {
        String url = "http://192.168.100.115/psicapp/obtener_mensajes.php?doctor_id=" + doctorId + "&paciente_id=" + pacienteId;
        Log.d("cargarConversacion", "URL: " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.d("cargarConversacion", "Respuesta: " + response.toString());
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
                    Log.e("cargarConversacion", "Error: " + error.toString());
                    Toast.makeText(this, "Error al cargar la conversación", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void enviarMensaje() {
        String mensaje = editTextMensaje.getText().toString().trim();

        if (mensaje.isEmpty()) {
            Toast.makeText(this, "Escribe un mensaje antes de enviar", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://192.168.100.115/psicapp/enviar_mensaje.php";
        Log.d("enviarMensaje", "URL: " + url);

        JSONObject parametros = new JSONObject();
        try {
            parametros.put("doctor_id", doctorId);
            parametros.put("paciente_id", pacienteId);
            parametros.put("contenido", mensaje);
            Log.d("enviarMensaje", "Parámetros: " + parametros.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al crear la solicitud", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                parametros,
                response -> {
                    try {
                        Log.d("enviarMensaje", "Respuesta: " + response.toString());
                        boolean success = response.getBoolean("success");

                        if (success) {
                            editTextMensaje.setText("");
                            cargarConversacion();
                        } else {
                            Toast.makeText(this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("enviarMensaje", "Error: " + error.toString());
                    Toast.makeText(this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}
