package com.example.estuday;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoricoMensagensActivity extends AppCompatActivity {

    private RecyclerView rvMensagens;
    private ChatAdapter adapter;
    private List<Map<String, String>> mensagemList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico_mensagens);

        rvMensagens = findViewById(R.id.rvMensagens);
        rvMensagens.setLayoutManager(new LinearLayoutManager(this));

        TextView tvTemaAtual = findViewById(R.id.tvTemaAtual);
        TextView voltar = findViewById(R.id.voltar);

        voltar.setOnClickListener(v -> finish());

        mensagemList = new ArrayList<>();
        adapter = new ChatAdapter(mensagemList);
        rvMensagens.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        String tema = getIntent().getStringExtra("tema");
        String userId = getIntent().getStringExtra("userId");

        if (tema != null && userId != null) {
            tvTemaAtual.setText(tema);
            carregarMensagensAntigas(userId, tema);
        } else {
            Toast.makeText(this, "Tema ou usuário não encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    private void carregarMensagensAntigas(String userId, String tema) {
        db.collection("users").document(userId).collection("temas").document(tema).collection("messages")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    mensagemList.clear(); // Limpa a lista para evitar duplicatas
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String mensagem = document.getString("content");
                        String sender = document.getString("sender");
                        if (mensagem != null && sender != null) {
                            Map<String, String> messageData = new HashMap<>();
                            messageData.put("content", mensagem);
                            messageData.put("sender", sender);
                            mensagemList.add(messageData);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    rvMensagens.scrollToPosition(mensagemList.size() - 1);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar mensagens: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
