package com.apps.tedrecomendation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ShowRecomendation extends AppCompatActivity {

    private ListView listView;
    String[] tmp = {"Ejemplo1","Ejemplo2","Ejemplo3","Ejemplo4","Ejemplo5","Ejemplo6","Ejemplo7","Ejemplo8","Ejemplo9","Ejemplo10"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_recomendation);

        //Intent intent = getIntent();
        //CharlaRecomendada[] recomendacion = (CharlaRecomendada[]) intent.getSerializableExtra("recomendacion");

        listView = findViewById(R.id.listView);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tmp);
        listView.setAdapter(adapter);
    }
}