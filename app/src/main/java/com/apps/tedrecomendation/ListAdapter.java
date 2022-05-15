package com.apps.tedrecomendation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends BaseAdapter {

    Context context;
    ArrayList<CharlaRecomendada> lst;

    public ListAdapter(Context context, ArrayList<CharlaRecomendada> lst){
        this.context = context;
        this.lst = lst;
    }

    @Override
    public int getCount() {
        return lst.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, @Nullable View view, @NonNull ViewGroup parent){

        CharlaRecomendada c = lst.get(i);

        view = LayoutInflater.from(context).inflate(R.layout.recomendation, null);


        TextView nombre = view.findViewById(R.id.nombrecharla);
        TextView puntuacion = view.findViewById(R.id.puntuacionrecomendacion);

        nombre.setText(String.valueOf(c.getId()));
        puntuacion.setText(String.valueOf(c.getPuntuacion()));


        return view;
    }
}
