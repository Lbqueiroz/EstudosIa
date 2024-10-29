package com.example.estuday;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private ChatAdapter adapter;
    private List<String> chatList = new ArrayList<>();
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        iniciarComponentes();

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(chatList);
        chatRecyclerView.setAdapter(adapter);

        attachIcon.setOnClickListener(view ->
                Toast.makeText(this, "Funcionalidade de anexar não implementada", Toast.LENGTH_SHORT).show());

        okButton.setOnClickListener(view -> {
            String conteudo = inputField.getText().toString().trim();
            if (!conteudo.isEmpty()) {
                adicionarMensagem("Você: " + conteudo);
                callAPI(conteudo);
            } else {
                Toast.makeText(this, "Digite uma mensagem", Toast.LENGTH_SHORT).show();
            }
        });
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
            jsonBody.put("model", "gpt-3.5-turbo");
            jsonBody.put("messages", new JSONArray().put(
                    new JSONObject().put("role", "user").put("content", question)
            ));
            jsonBody.put("max_tokens", 100);
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

    private void iniciarComponentes() {
        attachIcon = findViewById(R.id.attachIcon);
        okButton = findViewById(R.id.okButton);
        inputField = findViewById(R.id.inputField);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
