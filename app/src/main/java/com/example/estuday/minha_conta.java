package com.example.estuday;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class minha_conta extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView voltar, logOff, nomeUsuario, emailUsuario;
    private ImageView iconPerfil;
    private Button btnRedefinirSenha;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_minha_conta);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        IniciarComponentes();
        carregarDadosUsuario();

        btnRedefinirSenha.setOnClickListener(view -> {
            Intent intent = new Intent( minha_conta.this, RedefinirSenha.class);
            startActivity(intent);
        });

        voltar.setOnClickListener(view -> finish());

        logOff.setOnClickListener(view -> exibirDialogoLogoff());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void IniciarComponentes() {
        voltar = findViewById(R.id.voltar);
        logOff = findViewById(R.id.LogOff);
        nomeUsuario = findViewById(R.id.nomeUsuario);
        emailUsuario = findViewById(R.id.emailUsuario);
        iconPerfil = findViewById(R.id.iconPerfil);
        btnRedefinirSenha = findViewById(R.id.btnRedefinirSenha);
    }

    private void carregarDadosUsuario() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Acessa o Firestore e recupera os dados do usuário
            db.collection("usuarios").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String nome = document.getString("nome");
                                String email = document.getString("email");

                                // Atualiza os TextViews com os dados carregados
                                nomeUsuario.setText(nome != null ? nome : "Nome indisponível");
                                emailUsuario.setText(email != null ? email : "Email indisponível");
                            } else {
                                Toast.makeText(this, "Usuário não encontrado no Firestore.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Erro ao carregar dados: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            iconPerfil.setImageURI(imageUri);
        }
    }

    private void exibirDialogoLogoff() {
        new AlertDialog.Builder(this)
                .setMessage("Deseja realmente sair?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(minha_conta.this, chat.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Não", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
