package com.apps.tedrecomendation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ListAdapter2 extends BaseAdapter {

    Context context;
    ArrayList<CharlaRecomendada> lst;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    public ListAdapter2(Context context, ArrayList<CharlaRecomendada> lst){
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
        TextView id = view.findViewById(R.id.puntuacionrecomendacion);


        nombre.setText(String.valueOf(c.getTitulo()));
        id.setText("Id: " + String.valueOf(c.getId()));


        return view;
    }
}
