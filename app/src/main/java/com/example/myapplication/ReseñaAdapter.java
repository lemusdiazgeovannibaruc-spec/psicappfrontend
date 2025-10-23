package com.example.myapplication;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.psicapp.R;

import java.util.ArrayList;

public class ReseñaAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Reseña> reseñasList;

    public ReseñaAdapter(Context context, ArrayList<Reseña> reseñasList) {
        this.context = context;
        this.reseñasList = reseñasList;
    }

    @Override
    public int getCount() {
        return reseñasList.size();
    }

    @Override
    public Object getItem(int position) {
        return reseñasList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_resena, parent, false);
        } else {
            view = convertView;
        }

        Reseña reseña = reseñasList.get(position);

        TextView tvPacienteNombre = view.findViewById(R.id.tvPacienteNombre);
        RatingBar rbCalificacion = view.findViewById(R.id.rbCalificacion);
        TextView tvComentario = view.findViewById(R.id.tvComentario);

        tvPacienteNombre.setText(reseña.getPacienteNombre());
        rbCalificacion.setRating(reseña.getCalificacion());
        tvComentario.setText(reseña.getComentario());

        return view;
    }
}
