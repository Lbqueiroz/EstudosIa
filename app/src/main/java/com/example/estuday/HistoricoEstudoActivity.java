package com.example.estuday;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HistoricoEstudoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button voltarButton;
    private FirebaseFirestore db;
    private ChatAdapter adapter;
    private List<Map<String, Object>> historicoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico_estudo);

        db = FirebaseFirestore.getInstance(); // Inicializa Firestore
        historicoList = new ArrayList<>(); // Inicializa a lista

        iniciarComponentes();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(historicoList);
        recyclerView.setAdapter(adapter);

        carregarHistorico();

        voltarButton.setOnClickListener(view -> finish()); // Volta para a tela anterior
    }

    private void iniciarComponentes() {
        recyclerView = findViewById(R.id.recyclerViewHistorico);
        voltarButton = findViewById(R.id.voltarButton);
    }

    private void carregarHistorico() {
        // Busca o histórico do Firestore
        db.collection("chats").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        historicoList.clear(); // Limpa a lista para recarregar
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            historicoList.add(document.getData()); // Adiciona cada chat na lista
                        }
                        adapter.notifyDataSetChanged(); // Atualiza a RecyclerView
                    } else {
                        Toast.makeText(this, "Erro ao carregar histórico", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
