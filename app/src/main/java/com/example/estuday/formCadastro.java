package com.example.estuday;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class formCadastro extends AppCompatActivity {

    private EditText emailField, passwordField, nameField;
    private Button registerButton;
    private TextView loginLink;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;  // Instância do Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_cadastro);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Inicializando Firestore

        iniciarComponentes();

        loginLink.setOnClickListener(view -> {
            Intent intent = new Intent(formCadastro.this, FormLogin.class);
            startActivity(intent);
            finish();
        });

        registerButton.setOnClickListener(view -> {
            String name = nameField.getText().toString().trim();
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(formCadastro.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            } else {
                registrarUsuario(email, password, name);
            }
        });
    }

    private void registrarUsuario(String email, String password, String name) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Salvando o usuário no Firestore com o nome
                            salvarUsuarioNoFirestore(user, name);

                            Toast.makeText(formCadastro.this, "Registro bem-sucedido", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(formCadastro.this, chat.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(formCadastro.this, "Erro no registro: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void salvarUsuarioNoFirestore(FirebaseUser user, String nome) {
        String userId = user.getUid();
        String email = user.getEmail();

        // Criação do mapa de dados do usuário
        Map<String, Object> dadosUsuario = new HashMap<>();
        dadosUsuario.put("nome", nome);
        dadosUsuario.put("email", email);

        // Salvando o documento no Firestore
        db.collection("usuarios").document(userId)
                .set(dadosUsuario)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(formCadastro.this, "Usuário salvo no Firestore!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(formCadastro.this, "Erro ao salvar no Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void iniciarComponentes() {
        nameField = findViewById(R.id.name);  // Campo de nome
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        registerButton = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);
    }
}
