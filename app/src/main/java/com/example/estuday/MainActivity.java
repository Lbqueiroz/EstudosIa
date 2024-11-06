package com.example.estuday;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, chat.class);
            startActivity(intent);
            finish(); //
        }, 3000);
    }
    //ESTUDANTES:
    // Claudio Roberto Junio de Oliveira de Moraes
    //Lucas Brenio de Queiroz Oliveira
    //Lucas Ferreira Rodriguez
    //Luiz Felipe de Oliveira Araujo
    //Marcos Gabriel Pereira dos Santos
    //Pedro Victor Monteiro Fidelis
    //Talles Silva de Morais
    //Thiago da Silva Borges
}
