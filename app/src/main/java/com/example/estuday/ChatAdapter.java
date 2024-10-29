package com.example.estuday;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TIPO_USUARIO = 0;
    private static final int TIPO_IA = 1;
    private List<Map<String, Object>> mensagens;

    public ChatAdapter(List<Map<String, Object>> mensagens) {
        this.mensagens = mensagens;
    }

    @Override
    public int getItemViewType(int position) {
        Map<String, Object> mensagem = mensagens.get(position);
        String autor = (String) mensagem.get("autor");
        return "IA".equals(autor) ? TIPO_IA : TIPO_USUARIO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TIPO_USUARIO) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_user, parent, false);
            return new UsuarioViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_ai, parent, false);
            return new IAViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Map<String, Object> mensagem = mensagens.get(position);
        String conteudo = (String) mensagem.get("conteudo");

        if (holder instanceof UsuarioViewHolder) {
            ((UsuarioViewHolder) holder).textMensagemUsuario.setText(conteudo);
        } else {
            ((IAViewHolder) holder).textMensagemIA.setText(conteudo);
        }
    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }

    static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView textMensagemUsuario;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            textMensagemUsuario = itemView.findViewById(R.id.textMensagemUsuario);
        }
    }

    static class IAViewHolder extends RecyclerView.ViewHolder {
        TextView textMensagemIA;

        public IAViewHolder(@NonNull View itemView) {
            super(itemView);
            textMensagemIA = itemView.findViewById(R.id.textMensagemIA);
        }
    }
}
