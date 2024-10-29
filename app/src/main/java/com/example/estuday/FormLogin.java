package com.example.estuday;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class FormLogin extends AppCompatActivity {

    private EditText emailField, passwordField;
    private Button loginButton;
    private TextView registerLink, cancelButton, forgotPasswordLink;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_login);
        FirebaseApp.initializeApp(this);  // Inicializa o Firebase

        mAuth = FirebaseAuth.getInstance();

        iniciarComponentes();

        registerLink.setOnClickListener(view -> {
            Intent intent = new Intent(FormLogin.this, formCadastro.class);
            startActivity(intent);
        });

        cancelButton.setOnClickListener(view -> {
            Intent intent = new Intent(FormLogin.this, chat.class);
            startActivity(intent);
            finish();
        });

        forgotPasswordLink.setOnClickListener(view -> {
            Intent intent = new Intent( FormLogin.this, RedefinirSenha.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(view -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                realizarLogin(email, password);
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void realizarLogin(String email, String password) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Realizando login...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(FormLogin.this, "Login bem-sucedido", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(FormLogin.this, chat.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String mensagemErro;
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidUserException e) {
                            mensagemErro = "Usuário não encontrado. Verifique se o e-mail está correto.";
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            mensagemErro = "E-mail ou senha incorretos. Tente novamente.";
                        } catch (FirebaseNetworkException e) {
                            mensagemErro = "Falha de conexão. Verifique sua internet.";
                        } catch (Exception e) {
                            mensagemErro = "Erro desconhecido: " + e.getMessage();
                        }

                        Toast.makeText(FormLogin.this, "Erro no login: " + mensagemErro, Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void iniciarComponentes() {
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);
        cancelButton = findViewById(R.id.cancelButton);
        forgotPasswordLink = findViewById(R.id.forgotPasswordLink);
    }
}
