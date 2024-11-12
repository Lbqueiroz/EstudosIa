package com.example.estuday;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class revisao extends AppCompatActivity {

    private RecyclerView rvHistorico;
    private List<RevisaoItem> revisaoList;
    private TextView voltar, tvHistoricoEstudos, tvTemaRevisao;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_revisao);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        iniciarComponentes();

        voltar.setOnClickListener(view -> {
            Intent i = new Intent(revisao.this, chat.class);
            startActivity(i);
            finish();
        });

        rvHistorico = findViewById(R.id.rvHistorico);
        rvHistorico.setLayoutManager(new LinearLayoutManager(this));

        revisaoList = new ArrayList<>();
        carregarTemasEDuracao();
        buscarHistoricoEstudos();
    }

    private void carregarTemasEDuracao() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).collection("temas")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                String tema = document.getString("tema");
                                String duracao = document.getString("duracao"); // Certifique-se de que esse campo está presente no Firestore
                                if (tema != null && duracao != null) {
                                    tvTemaRevisao.setText("Tema: " + tema + " | Duração: " + duracao);
                                }
                            }
                        } else {
                            Toast.makeText(this, "Nenhum tema encontrado", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erro ao carregar temas: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }


    private void buscarHistoricoEstudos() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            db.collection("users").document(userId).collection("historico")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String tema = document.getString("tema");
                            String duracao = document.getString("duracao");
                            revisaoList.add(new RevisaoItem(tema, duracao));
                        }
                        RevisaoAdapter adapter = new RevisaoAdapter(revisaoList);
                        rvHistorico.setAdapter(adapter);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(revisao.this, "Erro ao buscar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
        }
    }

    private class RevisaoAdapter extends RecyclerView.Adapter<RevisaoAdapter.RevisaoViewHolder> {

        private final List<RevisaoItem> revisaoList;

        public RevisaoAdapter(List<RevisaoItem> revisaoList) {
            this.revisaoList = revisaoList;
        }

        @Override
        public RevisaoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_list, parent, false);
            return new RevisaoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RevisaoViewHolder holder, int position) {
            RevisaoItem item = revisaoList.get(position);
            holder.tvTemaRevisao.setText(item.getTema());
            holder.tvDuracaoRevisao.setText(item.getDuracao());
        }

        @Override
        public int getItemCount() {
            return revisaoList.size();
        }

        class RevisaoViewHolder extends RecyclerView.ViewHolder {
            TextView tvTemaRevisao, tvDuracaoRevisao;

            public RevisaoViewHolder(View itemView) {
                super(itemView);
                tvTemaRevisao = itemView.findViewById(R.id.tvTemaRevisao);
                tvDuracaoRevisao = itemView.findViewById(R.id.tvDuracaoRevisao);
            }
        }
    }

    private static class RevisaoItem {
        private final String tema;
        private final String duracao;

        public RevisaoItem(String tema, String duracao) {
            this.tema = tema;
            this.duracao = duracao;
        }

        public String getTema() {
            return tema;
        }

        public String getDuracao() {
            return duracao;
        }
    }

    private void iniciarComponentes() {
        voltar = findViewById(R.id.voltar);
        tvHistoricoEstudos = findViewById(R.id.tvHistoricoEstudos);
        tvTemaRevisao = findViewById(R.id.tvTemaRevisao);
    }
}
