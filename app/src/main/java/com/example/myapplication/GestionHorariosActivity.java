package com.example.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.psicapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class GestionHorariosActivity extends AppCompatActivity {

    private LinearLayout horariosLayout;
    private Button btnSelectDate, btnSelectStartTime, btnSelectEndTime, btnSaveSchedule;
    private TextView tvSelectedDate, tvSelectedStartTime, tvSelectedEndTime;

    private String selectedDate, startTime, endTime;
    private int doctorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_horarios);

        horariosLayout = findViewById(R.id.horariosLayout);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectStartTime = findViewById(R.id.btnSelectStartTime);
        btnSelectEndTime = findViewById(R.id.btnSelectEndTime);
        btnSaveSchedule = findViewById(R.id.btnSaveSchedule);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSelectedStartTime = findViewById(R.id.tvSelectedStartTime);
        tvSelectedEndTime = findViewById(R.id.tvSelectedEndTime);

        // Obtener doctor_id utilizando el correo
        getDoctorId();

        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectStartTime.setOnClickListener(v -> showTimePicker("start"));
        btnSelectEndTime.setOnClickListener(v -> showTimePicker("end"));

        btnSaveSchedule.setOnClickListener(v -> saveSchedule());
    }

    private void getDoctorId() {
        SharedPreferences preferences = getSharedPreferences("app_session", MODE_PRIVATE);
        String email = preferences.getString("email", null);

        Log.d("GestionHorariosActivity", "Correo recuperado: " + email);

        if (email == null) {
            Toast.makeText(this, "No se encontró el correo en la sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://192.168.100.115/psicapp/obtener_doctor_id.php?email=" + email;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d("GestionHorariosActivity", "Respuesta del servidor: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            doctorId = jsonResponse.getInt("doctor_id");
                            loadSchedules();
                        } else {
                            Toast.makeText(this, "Error: " + jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(stringRequest);
    }


    private void loadSchedules() {
        String url = "http://192.168.100.115/psicapp/listar_horarios.php?doctor_id=" + doctorId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d("GestionHorariosActivity", "Respuesta del servidor: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            JSONArray horarios = jsonResponse.getJSONArray("horarios");

                            // Limpiar elementos previos
                            horariosLayout.removeAllViews();

                            for (int i = 0; i < horarios.length(); i++) {
                                JSONObject horario = horarios.getJSONObject(i);
                                String fecha = horario.getString("fecha");
                                String horaInicio = horario.getString("hora_inicio");
                                String horaFin = horario.getString("hora_fin");
                                int id = horario.getInt("id");

                                // Contenedor para el horario
                                LinearLayout horarioContainer = new LinearLayout(this);
                                horarioContainer.setOrientation(LinearLayout.HORIZONTAL);
                                horarioContainer.setPadding(16, 16, 16, 16);
                                horarioContainer.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                ));

                                // TextView para mostrar la información del horario
                                TextView horarioView = new TextView(this);
                                horarioView.setText(
                                        "Fecha: " + fecha + "\n" +
                                                "Inicio: " + horaInicio + "\n" +
                                                "Fin: " + horaFin
                                );
                                horarioView.setLayoutParams(new LinearLayout.LayoutParams(
                                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 2
                                ));
                                horarioView.setTextSize(16); // Tamaño del texto
                                horarioView.setPadding(8, 8, 8, 8);
                                horarioContainer.addView(horarioView);

                                // Botón para editar
                                Button btnEditar = new Button(this);
                                btnEditar.setText("EDITAR");
                                btnEditar.setLayoutParams(new LinearLayout.LayoutParams(
                                        150, LinearLayout.LayoutParams.WRAP_CONTENT // Ancho fijo para consistencia
                                ));
                                btnEditar.setPadding(8, 8, 8, 8);
                                btnEditar.setOnClickListener(v -> openEditDialog(id, fecha, horaInicio, horaFin));
                                horarioContainer.addView(btnEditar);

                                // Botón para eliminar
                                Button btnEliminar = new Button(this);
                                btnEliminar.setText("ELIMINAR");
                                btnEliminar.setLayoutParams(new LinearLayout.LayoutParams(
                                        150, LinearLayout.LayoutParams.WRAP_CONTENT // Ancho fijo para consistencia
                                ));
                                btnEliminar.setPadding(8, 8, 8, 8);
                                btnEliminar.setOnClickListener(v -> deleteSchedule(id));
                                horarioContainer.addView(btnEliminar);

                                // Agregar el contenedor al layout principal
                                horariosLayout.addView(horarioContainer);

                                // Separador opcional entre los horarios
                                View separator = new View(this);
                                separator.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        2 // Altura del separador
                                ));
                                separator.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                                horariosLayout.addView(separator);
                            }
                        } else {
                            String message = jsonResponse.optString("message", "No se pudieron cargar los horarios");
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("GestionHorariosActivity", "Error al procesar la respuesta JSON", e);
                        Toast.makeText(this, "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("GestionHorariosActivity", "Error en la conexión", error);
                    Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void openEditDialog(int horarioId, String fecha, String horaInicio, String horaFin) {
        // Crear un cuadro de diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Horario");

        // Inflar el diseño personalizado del cuadro de diálogo
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_schedule, null);

        Button btnFecha = dialogView.findViewById(R.id.btnFecha);
        Button btnHoraInicio = dialogView.findViewById(R.id.btnHoraInicio);
        Button btnHoraFin = dialogView.findViewById(R.id.btnHoraFin);

        // Configurar los botones con los valores actuales
        btnFecha.setText(fecha);
        btnHoraInicio.setText(horaInicio);
        btnHoraFin.setText(horaFin);

        // Configurar el selector de fecha
        btnFecha.setOnClickListener(v -> {
            String[] fechaParts = fecha.split("-");
            int year = Integer.parseInt(fechaParts[0]);
            int month = Integer.parseInt(fechaParts[1]) - 1;
            int day = Integer.parseInt(fechaParts[2]);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
                String nuevaFecha = String.format("%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                btnFecha.setText(nuevaFecha);
            }, year, month, day);
            datePickerDialog.show();
        });

        // Configurar el selector de hora de inicio
        btnHoraInicio.setOnClickListener(v -> {
            String[] horaParts = horaInicio.split(":");
            int hour = Integer.parseInt(horaParts[0]);
            int minute = Integer.parseInt(horaParts[1]);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
                String nuevaHoraInicio = String.format("%02d:%02d:00", hourOfDay, minute1);
                btnHoraInicio.setText(nuevaHoraInicio);
            }, hour, minute, true);
            timePickerDialog.show();
        });

        // Configurar el selector de hora de fin
        btnHoraFin.setOnClickListener(v -> {
            String[] horaParts = horaFin.split(":");
            int hour = Integer.parseInt(horaParts[0]);
            int minute = Integer.parseInt(horaParts[1]);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
                String nuevaHoraFin = String.format("%02d:%02d:00", hourOfDay, minute1);
                btnHoraFin.setText(nuevaHoraFin);
            }, hour, minute, true);
            timePickerDialog.show();
        });

        builder.setView(dialogView);

        // Botón de guardar
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nuevaFecha = btnFecha.getText().toString();
            String nuevaHoraInicio = btnHoraInicio.getText().toString();
            String nuevaHoraFin = btnHoraFin.getText().toString();

            updateSchedule(horarioId, nuevaFecha, nuevaHoraInicio, nuevaHoraFin);
        });

        // Botón de cancelar
        builder.setNegativeButton("Cancelar", null);

        // Mostrar el diálogo
        builder.create().show();
    }

    private void updateSchedule(int horarioId, String nuevaFecha, String nuevaHoraInicio, String nuevaHoraFin) {
        String url = "http://192.168.100.115/psicapp/actualizar_horario.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            Toast.makeText(this, "Horario actualizado correctamente", Toast.LENGTH_SHORT).show();
                            loadSchedules(); // Recargar horarios
                        } else {
                            String message = jsonResponse.optString("message", "No se pudo actualizar el horario");
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("GestionHorariosActivity", "Error al procesar la respuesta JSON", e);
                        Toast.makeText(this, "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("GestionHorariosActivity", "Error en la conexión", error);
                    Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(horarioId));
                params.put("fecha", nuevaFecha);
                params.put("hora_inicio", nuevaHoraInicio);
                params.put("hora_fin", nuevaHoraFin);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void deleteSchedule(int horarioId) {
        String url = "http://192.168.100.115/psicapp/eliminar_horario.php?horario_id=" + horarioId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            Toast.makeText(this, "Horario eliminado correctamente", Toast.LENGTH_SHORT).show();
                            loadSchedules(); // Recargar horarios
                        } else {
                            String message = jsonResponse.optString("message", "No se pudo eliminar el horario");
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("GestionHorariosActivity", "Error al procesar la respuesta JSON", e);
                        Toast.makeText(this, "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("GestionHorariosActivity", "Error en la conexión", error);
                    Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(stringRequest);
    }



    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                    tvSelectedDate.setText("Fecha seleccionada: " + selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void showTimePicker(String type) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePicker = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    String time = String.format("%02d:%02d", hourOfDay, minute);
                    if (type.equals("start")) {
                        startTime = time;
                        tvSelectedStartTime.setText("Hora de Inicio: " + startTime);
                    } else {
                        endTime = time;
                        tvSelectedEndTime.setText("Hora de Fin: " + endTime);
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePicker.show();
    }

    private void saveSchedule() {
        if (selectedDate == null || startTime == null || endTime == null) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://192.168.100.115/psicapp/agregar_horario.php";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("doctor_id", doctorId);
            requestBody.put("fecha", selectedDate);
            requestBody.put("hora_inicio", startTime);
            requestBody.put("hora_fin", endTime);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al crear la solicitud", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        if (success) {
                            loadSchedules();
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
