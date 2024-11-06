package com.example.estuday;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class helpButton extends AppCompatActivity {

    private ImageView imageView;
    private ImageButton buttonLeft, buttonRight;
    private int currentImageIndex = 0;
    private int[] imageResources = {
            R.drawable.historicodeestudos,
            R.drawable.login,
            R.drawable.minhaconta,
            R.drawable.reedefinirsenha,
            R.drawable.registrar,
            R.drawable.telainicial,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_button);

        imageView = findViewById(R.id.imageView);
        buttonLeft = findViewById(R.id.button_left);
        buttonRight = findViewById(R.id.button_right);

        imageView.setImageResource(imageResources[currentImageIndex]);

        buttonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentImageIndex > 0) {
                    currentImageIndex--;
                } else {
                    currentImageIndex = imageResources.length - 1; // Vai para a última imagem
                }
                imageView.setImageResource(imageResources[currentImageIndex]);
            }
        });

        buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentImageIndex < imageResources.length - 1) {
                    currentImageIndex++;
                } else {
                    currentImageIndex = 0; // Volta para a primeira imagem
                }
                imageView.setImageResource(imageResources[currentImageIndex]);
            }
        });
    }
}