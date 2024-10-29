package com.example.estuday;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RedefinirSenha extends AppCompatActivity {

    private EditText emailField;
    private Button confirmEmailButton, cancelButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redefinir_trocar_senha);

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Inicializar componentes
        emailField = findViewById(R.id.emailField);
        confirmEmailButton = findViewById(R.id.confirmEmailButton);
        cancelButton = findViewById(R.id.cancelButton);

        // Configurações de clique
        confirmEmailButton.setOnClickListener(v -> enviarEmailRedefinicao());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void enviarEmailRedefinicao() {
        String email = emailField.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Por favor, insira seu email", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            exibirDialogoSucesso();
                        } else {
                            Toast.makeText(this, "Erro ao enviar email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void exibirDialogoSucesso() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Email de redefinição enviado com sucesso! Verifique sua caixa de entrada.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Redirecionar para a tela de login
                    Intent intent = new Intent(RedefinirSenha.this, FormLogin.class);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}
