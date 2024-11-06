package com.example.estuday;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Rect;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
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
    private ImageButton okButton, buttonHelp, buttonCreateChat;
    private EditText inputField;
    private TextView themeTextView;
    private RecyclerView chatRecyclerView;
    private Button profileButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String temaAtual = "";

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

        buttonCreateChat.setOnClickListener(view -> {
            chatList.clear();
            adapter.notifyDataSetChanged();
            solicitarTema();
        });

        buttonHelp.setOnClickListener(view -> {
            Intent intent = new Intent(chat.this, helpButton.class);
            startActivity(intent);
            finish();
        });

        attachIcon.setOnClickListener(view ->
                Toast.makeText(this, "Funcionalidade de anexar não implementada", Toast.LENGTH_SHORT).show());

        okButton.setOnClickListener(view -> {
            String conteudo = inputField.getText().toString().trim();
            if (!conteudo.isEmpty()) {
                if (conteudo.equalsIgnoreCase("trocar tema")) {
                    solicitarTema();
                } else {
                    adicionarMensagem("Você: " + conteudo);
                    callAPI(conteudo);
                }
            } else {
                Toast.makeText(this, "Digite uma mensagem", Toast.LENGTH_SHORT).show();
            }
        });

        carregarMensagensAnteriores(() -> {
            if (chatList.isEmpty()) {
                solicitarTema();
            } else {
                mostrarDialogoTema();
            }
        });
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void mostrarDialogoTema() {
        new AlertDialog.Builder(this)
                .setTitle("Tema existente encontrado")
                .setMessage("Deseja manter o tema antigo ou iniciar um novo?")
                .setPositiveButton("Novo Tema", (dialog, which) -> solicitarTema())
                .setNegativeButton("Manter Tema", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view != null && (view instanceof EditText)) {
                Rect outRect = new Rect();
                view.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    view.clearFocus();
                    hideKeyboard(view);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void adicionarMensagem(String mensagem) {
        if (mensagem != null && !mensagem.isEmpty()) {
            chatList.add(mensagem.trim());
            adapter.notifyDataSetChanged();
            inputField.setText("");
            chatRecyclerView.scrollToPosition(chatList.size() - 1);
        }

        salvarMensagemNoFirestore(mensagem, "user");
    }

    private void adicionarResposta(String resposta) {
        chatList.add("Bot: " + resposta);
        adapter.notifyDataSetChanged();
        chatRecyclerView.scrollToPosition(chatList.size() - 1);

        salvarMensagemNoFirestore("Bot: " + resposta, "bot");
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
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "")
                .header("Content-Type", "application/json")
                .post(body)
                .build();

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
                        runOnUiThread(() ->
                                Toast.makeText(chat.this, "Erro no JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                } else {
                    String errorMessage = response.message();
                    runOnUiThread(() ->
                            Toast.makeText(chat.this, "Erro na resposta: " + errorMessage, Toast.LENGTH_SHORT).show());

                    System.out.println("Erro na resposta: Código " + response.code() + " - " + errorMessage);
                }
            }
        });
    }

    private void carregarMensagensAnteriores(Runnable callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).collection("chats").document("defaultSession")
                    .collection("messages").orderBy("timestamp").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                String conteudo = document.getString("content");
                                chatList.add(conteudo);
                            }
                            adapter.notifyDataSetChanged();
                        }
                        callback.run();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erro ao carregar mensagens: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        callback.run();
                    });
        } else {
            callback.run();
        }
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

    private void solicitarTema() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escolha um tema para estudar");

        final EditText input = new EditText(this);
        input.setHint("Digite o tema");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            temaAtual = input.getText().toString().trim();
            if (!temaAtual.isEmpty()) {
                themeTextView.setText("Tema atual: " + temaAtual);
                callAPI("Fale sobre " + temaAtual);
                salvarTemaNoFirestore(temaAtual);
            } else {
                Toast.makeText(this, "Por favor, digite um tema válido.", Toast.LENGTH_SHORT).show();
                solicitarTema();
            }
        });

        builder.setCancelable(false);
        builder.show();
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

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}

