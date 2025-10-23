package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.example.psicapp.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.Manifest;


public class RegistroActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 101;

    private EditText editNombre, editEmail, editTelefono, editPassword, editDireccion, editLicencia;
    private Button btnFechaNacimiento, btnObtenerUbicacion, btnRegistrar, btnSeleccionarFoto;
    private CheckBox checkMasculino, checkFemenino, checkOtro;
    private ImageView imgFotoPerfil;
    private Spinner spinnerRol, spinnerEspecialidad;

    private String fechaNacimiento = "";
    private String genero = "";
    private String rolSeleccionado = "paciente";
    private String especialidadSeleccionada = "";
    private String ubicacion = "";
    private String fotoPerfilBase64 = "";

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Enlazar vistas
        editNombre = findViewById(R.id.editNombre);
        editEmail = findViewById(R.id.editEmail);
        editTelefono = findViewById(R.id.editTelefono);
        editPassword = findViewById(R.id.editPassword);
        editDireccion = findViewById(R.id.editDireccion);
        btnFechaNacimiento = findViewById(R.id.btnFechaNacimiento);
        btnObtenerUbicacion = findViewById(R.id.btnObtenerUbicacion);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
        editLicencia = findViewById(R.id.editLicencia);
        spinnerRol = findViewById(R.id.spinnerRol);
        spinnerEspecialidad = findViewById(R.id.spinnerEspecialidad);
        checkMasculino = findViewById(R.id.checkMasculino);
        checkFemenino = findViewById(R.id.checkFemenino);
        checkOtro = findViewById(R.id.checkOtro);

        // Configurar Spinner de Roles
        ArrayAdapter<CharSequence> rolAdapter = ArrayAdapter.createFromResource(this,
                R.array.roles_array, android.R.layout.simple_spinner_item);
        rolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRol.setAdapter(rolAdapter);

        spinnerRol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                rolSeleccionado = parent.getItemAtPosition(position).toString();
                if (rolSeleccionado.equals("doctor")) {
                    spinnerEspecialidad.setVisibility(View.VISIBLE);
                    editLicencia.setVisibility(View.VISIBLE);
                } else {
                    spinnerEspecialidad.setVisibility(View.GONE);
                    editLicencia.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Configurar Spinner de Especialidades
        ArrayAdapter<CharSequence> especialidadAdapter = ArrayAdapter.createFromResource(this,
                R.array.especialidades_array, android.R.layout.simple_spinner_item);
        especialidadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEspecialidad.setAdapter(especialidadAdapter);
        spinnerEspecialidad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                especialidadSeleccionada = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Configurar Fecha de Nacimiento
        btnFechaNacimiento.setOnClickListener(v -> {
            Calendar calendario = Calendar.getInstance();
            int anio = calendario.get(Calendar.YEAR);
            int mes = calendario.get(Calendar.MONTH);
            int dia = calendario.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(RegistroActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        fechaNacimiento = year + "-" + (month + 1) + "-" + dayOfMonth;
                        btnFechaNacimiento.setText(fechaNacimiento);
                    }, anio, mes, dia);
            datePickerDialog.show();
        });

        // Configurar CheckBoxes para género
        checkMasculino.setOnClickListener(v -> {
            genero = "masculino";
            checkFemenino.setChecked(false);
            checkOtro.setChecked(false);
        });
        checkFemenino.setOnClickListener(v -> {
            genero = "femenino";
            checkMasculino.setChecked(false);
            checkOtro.setChecked(false);
        });
        checkOtro.setOnClickListener(v -> {
            genero = "otro";
            checkMasculino.setChecked(false);
            checkFemenino.setChecked(false);
        });

        // Configurar Ubicación
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        btnObtenerUbicacion.setOnClickListener(v -> obtenerUbicacion());

        // Configurar Selección de Foto
        btnSeleccionarFoto.setOnClickListener(v -> seleccionarFoto());

        // Manejar Registro
        btnRegistrar.setOnClickListener(v -> {
            // Validar campos obligatorios
            if (editNombre.getText().toString().trim().isEmpty() ||
                    editEmail.getText().toString().trim().isEmpty() ||
                    !Patterns.EMAIL_ADDRESS.matcher(editEmail.getText().toString().trim()).matches() || // Validar formato del email
                    editTelefono.getText().toString().trim().isEmpty() ||
                    editPassword.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos correctamente.", Toast.LENGTH_SHORT).show();

                return;
            }

            registrarUsuario();
        });

    }

    private void seleccionarFoto() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imgFotoPerfil.setImageBitmap(bitmap);
                fotoPerfilBase64 = convertirBitmapABase64(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al seleccionar la foto", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String convertirBitmapABase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double latitud = location.getLatitude();
                    double longitud = location.getLongitude();

                    try {
                        Geocoder geocoder = new Geocoder(RegistroActivity.this, Locale.getDefault());
                        List<Address> direcciones = geocoder.getFromLocation(latitud, longitud, 1);

                        if (direcciones != null && !direcciones.isEmpty()) {
                            Address direccion = direcciones.get(0);
                            ubicacion = direccion.getAddressLine(0);
                            editDireccion.setText(ubicacion);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    locationManager.removeUpdates(this);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {}
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void registrarUsuario() {
        String url = "http://192.168.100.115/psicapp/registrar_usuario.php";

        // Crear los parámetros para la solicitud
        Map<String, String> params = new HashMap<>();
        params.put("nombre", editNombre.getText().toString().trim());
        params.put("email", editEmail.getText().toString().trim());
        params.put("telefono", editTelefono.getText().toString().trim());
        params.put("contraseña", editPassword.getText().toString().trim());
        params.put("fecha_nacimiento", fechaNacimiento);
        params.put("genero", genero);
        params.put("direccion", editDireccion.getText().toString().trim());
        params.put("rol", rolSeleccionado);

        if (rolSeleccionado.equals("doctor")) {
            params.put("especialidad", especialidadSeleccionada);
            params.put("licencia", editLicencia.getText().toString().trim());
        }

        // Agregar la foto en Base64
        if (!fotoPerfilBase64.isEmpty()) {
            params.put("foto_perfil_base64", fotoPerfilBase64);
        }

        // Realizar la solicitud
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("RegistroRespuesta", "Respuesta del servidor: " + response);
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegistroActivity.this, MainActivity.class));
                    finish();
                },
                error -> {
                    Log.e("RegistroError", "Error al registrar: " + error.getMessage());
                    Toast.makeText(this, "Error al registrar", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        ApiHandler.getInstance(this).addToRequestQueue(stringRequest);
    }


}
