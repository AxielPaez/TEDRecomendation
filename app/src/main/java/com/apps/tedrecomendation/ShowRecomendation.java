package com.apps.tedrecomendation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.apps.tedrecomendation.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class ShowRecomendation extends AppCompatActivity {

    private ListView listView;
    List<CharlaRecomendada> tmp = new ArrayList<CharlaRecomendada>();
    ArrayList<CharlaRecomendada> recomendacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_recomendation);

        System.out.println("###########################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################");

        listView = findViewById(R.id.listaReco);
        Intent intent = getIntent();
        recomendacion = (ArrayList<CharlaRecomendada>) intent.getSerializableExtra("recomendacion");
        System.out.println("Recomend:");
        System.out.println(recomendacion.get(0).toString());

        ListAdapter listAdapter = new ListAdapter(this, recomendacion);
        listView.setAdapter(listAdapter);
    }
}