package com.example.estuday;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
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
    private ImageButton okButton;
    private EditText inputField;
    private RecyclerView chatRecyclerView;
    private Button profileButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String temaAtual = "";

    private ChatAdapter adapter;
    private List<String> chatList = new ArrayList<>();
    private OkHttpClient client = new OkHttpClient();

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

        attachIcon.setOnClickListener(view ->
                Toast.makeText(this, "Funcionalidade de anexar não implementada", Toast.LENGTH_SHORT).show());

        okButton.setOnClickListener(view -> {
            String conteudo = inputField.getText().toString().trim();
            if (!conteudo.isEmpty()) {
                if (conteudo.equalsIgnoreCase("trocar tema")) {
                    solicitarTema(); // Solicita novo tema se o usuário quiser trocar
                } else {
                    adicionarMensagem("Você: " + conteudo);
                    callAPI(conteudo);
                }
            } else {
                Toast.makeText(this, "Digite uma mensagem", Toast.LENGTH_SHORT).show();
            }
        });
        solicitarTema();
    }

    private void adicionarMensagem(String mensagem) {
        chatList.add(mensagem);
        adapter.notifyDataSetChanged();
        inputField.setText("");
        chatRecyclerView.scrollToPosition(chatList.size() - 1);
    }

    private void adicionarResposta(String resposta) {
        chatList.add("Bot: " + resposta);
        adapter.notifyDataSetChanged();
        chatRecyclerView.scrollToPosition(chatList.size() - 1);
    }

    private void callAPI(String question) {
        JSONObject jsonBody = new JSONObject();
        try {
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system")
                    .put("content", "Você é um assistente de estudos que foca em temas educacionais e cria mapas mentais se pedidos."));
            messages.put(new JSONObject().put("role", "user")
                    .put("content", "Tema atual: " + temaAtual + ". " + question));

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
                runOnUiThread(() ->
                        Toast.makeText(chat.this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "Corpo da resposta é nulo";

                System.out.println("Resposta da API: " + responseBody);

                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        JSONArray choices = jsonObject.getJSONArray("choices");
                        String result = choices.getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content").trim();
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
                adicionarMensagem("Tema atual: " + temaAtual);
                callAPI("Fale sobre " + temaAtual);
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

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
