package com.example.estuday;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class chat extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;

    private ImageView attachIcon;
    private Button okButton, profileButton;
    private EditText inputField;
    private TextView textHistory;
    private RecyclerView chatRecyclerView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ChatAdapter adapter;
    private List<Map<String, Object>> chatList = new ArrayList<>();

    private String chatId = "matematica"; // Tema ou ID do chat
    private CollectionReference mensagensRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Inicializando Firebase e componentes
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        iniciarComponentes();

        // Configurando RecyclerView
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(chatList);
        chatRecyclerView.setAdapter(adapter);

        textHistory.setOnClickListener(view -> {
            Intent intent = new Intent(chat.this, HistoricoEstudoActivity.class);
            startActivity(intent);
        });

        // Definindo referência da subcoleção "mensagens"
        mensagensRef = db.collection("chats").document(chatId).collection("mensagens");

        carregarMensagens(); // Carrega as mensagens salvas

        profileButton.setOnClickListener(view -> verificarLogin());
        attachIcon.setOnClickListener(view -> abrirSeletorDeArquivos());
        okButton.setOnClickListener(view -> {
            String conteudo = inputField.getText().toString().trim();

            if (!conteudo.isEmpty()) {
                enviarMensagem(conteudo, "Usuário");
            } else {
                Toast.makeText(this, "Digite uma mensagem", Toast.LENGTH_SHORT).show();
            }
        });    }

    private void enviarMensagem(String conteudo, String autor) {
        Map<String, Object> mensagem = new HashMap<>();
        mensagem.put("conteudo", conteudo);
        mensagem.put("autor", autor);
        mensagem.put("data", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));

        mensagensRef.add(mensagem)
                .addOnSuccessListener(documentReference -> inputField.setText(""))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao enviar mensagem: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }


    private void carregarMensagens() {
        // Listener para carregar as mensagens em tempo real
        mensagensRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Erro ao carregar mensagens", Toast.LENGTH_SHORT).show();
                return;
            }

            chatList.clear(); // Limpa a lista atual

            for (QueryDocumentSnapshot document : value) {
                chatList.add(document.getData()); // Adiciona cada mensagem na lista
            }

            adapter.notifyDataSetChanged(); // Atualiza a RecyclerView
        });
    }

    private void abrirSeletorDeArquivos() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Selecione um arquivo"), PICK_FILE_REQUEST);
    }

    private void verificarLogin() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(this, minha_conta.class));
        } else {
            new AlertDialog.Builder(this)
                    .setMessage("Você não está logado. Deseja fazer login?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        startActivity(new Intent(this, FormLogin.class));
                        finish();
                    })
                    .setNegativeButton("Não", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    private void iniciarComponentes() {
        attachIcon = findViewById(R.id.attachIcon);
        okButton = findViewById(R.id.okButton);
        profileButton = findViewById(R.id.profileButton);
        inputField = findViewById(R.id.inputField);
        textHistory = findViewById(R.id.textHistory);

        chatRecyclerView = findViewById(R.id.chatRecyclerView); // Inicializa o RecyclerView

        // Configura o LayoutManager para o RecyclerView
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
