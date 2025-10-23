package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.psicapp.R;

import java.util.ArrayList;

public class DoctorAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Doctor> doctorList;

    public DoctorAdapter(Context context, ArrayList<Doctor> doctorList) {
        this.context = context;
        this.doctorList = doctorList;
    }

    @Override
    public int getCount() {
        return doctorList.size();
    }

    @Override
    public Object getItem(int position) {
        return doctorList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_doctor, parent, false);
        } else {
            view = convertView;
        }

        Doctor doctor = doctorList.get(position);

        TextView tvNombre = view.findViewById(R.id.tvDoctorNombre);
        TextView tvEspecialidad = view.findViewById(R.id.tvEspecialidad);
        TextView tvUbicacion = view.findViewById(R.id.tvUbicacion);
        Button btnAgendarCita = view.findViewById(R.id.btnAgendarCita);
        Button btnVerReseñas = view.findViewById(R.id.btnVerReseñas);

        tvNombre.setText(doctor.getNombre());
        tvEspecialidad.setText(doctor.getEspecialidad());
        tvUbicacion.setText(doctor.getUbicacion());

        btnAgendarCita.setOnClickListener(v -> {
            Intent intent = new Intent(context, ActivityGenerarCita.class);
            intent.putExtra("doctorId", doctor.getId());
            intent.putExtra("doctorNombre", doctor.getNombre());
            context.startActivity(intent);
        });

        btnVerReseñas.setOnClickListener(v -> {
            Intent intent = new Intent(context, ActivityVerReseñas.class);
            intent.putExtra("doctorId", doctor.getId());
            context.startActivity(intent);
        });

        return view;
    }
}
