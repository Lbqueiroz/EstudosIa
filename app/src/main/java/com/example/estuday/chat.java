package com.example.estuday;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class chat extends AppCompatActivity {

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private ImageView attachIcon;
    private ImageButton okButton, buttonHelp, buttonCreateChat, button_scroll_to_bottom;
    private EditText inputField;
    private TextView themeTextView, textHistory;
    private RecyclerView chatRecyclerView;
    private Button profileButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String temaAtual = "";
    private long startTime, chatDuration;
    private boolean temaDefinido = false;

    private ChatAdapter adapter;
    private List<String> chatList = new ArrayList<>();
    private OkHttpClient client = new OkHttpClient();

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        iniciarComponentes();

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(chatList);
        chatRecyclerView.setAdapter(adapter);
        profileButton.setOnClickListener(view -> verificarLogin());

        chatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) chatRecyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
                    if (lastVisiblePosition < chatList.size() - 1) {
                        button_scroll_to_bottom.setVisibility(View.VISIBLE);
                    } else {
                        button_scroll_to_bottom.setVisibility(View.GONE);
                    }
                }
            }
        });

        inputField.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !temaDefinido) {
                solicitarTema();
            }
        });

        inputField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER &&
                            event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {
                String conteudo = inputField.getText().toString().trim();
                if (!conteudo.isEmpty()) {
                    adicionarMensagem("Você: " + conteudo);
                    callAPI(conteudo);
                } else {
                    Toast.makeText(this, "Digite uma mensagem", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });

        okButton.setOnClickListener(view -> {
            String conteudo = inputField.getText().toString().trim();
            if (!conteudo.isEmpty()) {
                adicionarMensagem("Você: " + conteudo);
                callAPI(conteudo);
            } else {
                Toast.makeText(this, "Digite uma mensagem", Toast.LENGTH_SHORT).show();
            }
        });

        button_scroll_to_bottom.setOnClickListener(view -> {
            if (!chatList.isEmpty()) {
                chatRecyclerView.scrollToPosition(chatList.size() - 1);
            }
        });

        buttonCreateChat.setOnClickListener(view -> {
            if (temaDefinido) {
                long endTime = SystemClock.elapsedRealtime();
                chatDuration = endTime - startTime;
                salvarDuracaoNoFirestore(temaAtual,chatDuration);
            }
            chatList.clear();
            adapter.notifyDataSetChanged();
            temaAtual = "";
            themeTextView.setText("");
            temaDefinido = false;
        });


        buttonHelp.setOnClickListener(view -> {
            Intent intent = new Intent(chat.this, helpButton.class);
            startActivity(intent);
            finish();
        });

        textHistory.setOnClickListener(v -> {
            Intent i = new Intent(chat.this, revisao.class);
            startActivity(i);
        });

        attachIcon.setOnClickListener(view -> Toast.makeText(this, "Funcionalidade de anexar não implementada", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (temaDefinido) {
            long endTime = SystemClock.elapsedRealtime();
            chatDuration = endTime - startTime;
            salvarDuracaoNoFirestore(temaAtual, chatDuration);
        }
    }

    private void callAPI(String question) {
        JSONObject jsonBody = new JSONObject();
        try {
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content", "Você é um assistente de estudos que foca em temas educacionais para estudos."));
            messages.put(new JSONObject().put("role", "user").put("content", "Tema atual: " + temaAtual + ". " + question));

            jsonBody.put("model", "gpt-3.5-turbo");
            jsonBody.put("messages", messages);
            jsonBody.put("max_tokens", 300);
            jsonBody.put("temperature", 0.7);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder().url("https://api.openai.com/v1/chat/completions").header("Authorization", "").header("Content-Type", "application/json").post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(chat.this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "Corpo da resposta é nulo";

                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        JSONArray choices = jsonObject.getJSONArray("choices");
                        String result = choices.getJSONObject(0).getJSONObject("message").getString("content").trim();
                        runOnUiThread(() -> adicionarResposta(result));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(chat.this, "Erro no JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                } else {
                    String errorMessage = response.message();
                    runOnUiThread(() -> Toast.makeText(chat.this, "Erro na resposta: " + errorMessage, Toast.LENGTH_SHORT).show());

                    System.out.println("Erro na resposta: Código " + response.code() + " - " + errorMessage);
                }
            }
        });
    }


    private void solicitarTema() {
        if (temaDefinido) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escolha um tema para estudar");

        final EditText input = new EditText(this);
        input.setHint("Digite o tema");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            temaAtual = input.getText().toString().trim();
            if (!temaAtual.isEmpty()) {
                themeTextView.setText("Tema atual: " + temaAtual);
                temaDefinido = true;
                chatList.clear();
                adapter.notifyDataSetChanged();
                iniciarNovoChat(temaAtual);
                salvarTemaNoFirestore(temaAtual);

                callAPI("Inicie um estudo sobre " + temaAtual);
            } else {
                Toast.makeText(this, "Por favor, digite um tema válido.", Toast.LENGTH_SHORT).show();
                solicitarTema();
            }
        });

        builder.setCancelable(false);
        builder.show();
    }

    private void iniciarNovoChat(String tema) {
        startTime = SystemClock.elapsedRealtime();
    }

    private void adicionarMensagem(String mensagem) {
        if (mensagem != null && !mensagem.isEmpty()) {
            chatList.add(mensagem.trim());
            adapter.notifyDataSetChanged();
            inputField.setText("");
            chatRecyclerView.scrollToPosition(chatList.size() - 1);
            salvarMensagemNoFirestore("mensagem", "user");
        }
    }

    private void adicionarResposta(String resposta) {
        chatList.add("Bot: " + resposta);
        adapter.notifyDataSetChanged();
        chatRecyclerView.scrollToPosition(chatList.size() - 1);
        salvarMensagemNoFirestore("mensagem", "Bot: " + resposta);
    }

    private void salvarMensagemNoFirestore(String mensagem, String remetente) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            String chatSessionId = "defaultSession";

            Map<String, Object> messageData = new HashMap<>();
            messageData.put("timestamp", new Date());
            messageData.put("sender", remetente);
            messageData.put("content", mensagem);

            db.collection("users").document(userId).collection("chats").document(chatSessionId)
                    .collection("messages").add(messageData)
                    .addOnSuccessListener(documentReference -> {})
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erro ao salvar mensagem: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void salvarTemaNoFirestore(String tema) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            Map<String, Object> temaData = new HashMap<>();
            temaData.put("tema", tema);
            temaData.put("timestamp", new Date());

            db.collection("users").document(userId).collection("temas").add(temaData)
                    .addOnSuccessListener(documentReference ->
                            Toast.makeText(this, "Tema salvo com sucesso", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erro ao salvar o tema: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void salvarDuracaoNoFirestore(String tema, long duracao) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            Map<String, Object> duracaoData = new HashMap<>();
            duracaoData.put("tema", tema);
            duracaoData.put("duracao", duracao / 1000 + " segundos");
            duracaoData.put("timestamp", new Date());

            db.collection("users").document(userId).collection("temas").document(tema)
                    .set(duracaoData)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Duração do chat salva com sucesso", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erro ao salvar a duração: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }


    private void verificarLogin() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(this, minha_conta.class));
        } else {
            if (temaDefinido) {
                long endTime = SystemClock.elapsedRealtime();
                chatDuration = endTime - startTime;
                salvarDuracaoNoFirestore(temaAtual, chatDuration);
            }
            new AlertDialog.Builder(this).setMessage("Você não está logado. Deseja fazer login?").setPositiveButton("Sim", (dialog, which) -> {
                startActivity(new Intent(this, FormLogin.class));
                finish();
            }).setNegativeButton("Não", (dialog, which) -> dialog.dismiss()).show();
        }
    }


    private void iniciarComponentes() {
        attachIcon = findViewById(R.id.attachIcon);
        okButton = findViewById(R.id.okButton);
        inputField = findViewById(R.id.inputField);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        profileButton = findViewById(R.id.profileButton);
        buttonCreateChat = findViewById(R.id.button_create_chat);
        buttonHelp = findViewById(R.id.button_help);
        themeTextView = findViewById(R.id.themeTextView);
        button_scroll_to_bottom = findViewById(R.id.button_scroll_to_bottom);
        textHistory = findViewById(R.id.textHistory);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
