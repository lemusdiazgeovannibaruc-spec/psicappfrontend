package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.psicapp.R;

import java.util.List;

public class CitasAdapter extends ArrayAdapter<Cita> {
    private Context context;
    private List<Cita> citas;

    public CitasAdapter(Context context, List<Cita> citas) {
        super(context, R.layout.item_cita, citas);
        this.context = context;
        this.citas = citas;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_cita, parent, false);
        }

        Cita cita = citas.get(position);

        TextView tvFecha = convertView.findViewById(R.id.tvFecha);
        TextView tvHora = convertView.findViewById(R.id.tvHora);
        TextView tvPaciente = convertView.findViewById(R.id.tvPaciente);

        tvFecha.setText("Fecha: " + cita.getFecha());
        tvHora.setText("Hora: " + cita.getHoraInicio() + " - " + cita.getHoraFin());
        tvPaciente.setText("Paciente: " + cita.getPacienteNombre());

        return convertView;
    }
}
