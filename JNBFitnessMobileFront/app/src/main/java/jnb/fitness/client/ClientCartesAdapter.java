package jnb.fitness.client;
import jnb.fitness.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.List;

public class ClientCartesAdapter extends RecyclerView.Adapter<ClientCartesAdapter.VH> {
    public interface Callbacks { void onRecharger(long carteId, int montant); }
    private final List<JSONObject> data; private final Callbacks cb;
    public ClientCartesAdapter(List<JSONObject> data, Callbacks cb){ this.data=data; this.cb=cb; }
    static class VH extends RecyclerView.ViewHolder { TextView title, solde, numero, devise; EditText montant; Button recharger; VH(View v){ super(v); title=v.findViewById(R.id.title); solde=v.findViewById(R.id.solde); numero=v.findViewById(R.id.numero); devise=v.findViewById(R.id.devise); montant=v.findViewById(R.id.input_montant); recharger=v.findViewById(R.id.btn_recharger);} }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent,int viewType){ View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_carte, parent, false); return new VH(v);} 
    @Override public void onBindViewHolder(@NonNull VH h,int i){ JSONObject o=data.get(i); long id=o.optLong("id"); String lib=o.optString("libelle"); String nom=o.optString("nom"); String title=!lib.isEmpty()? lib : (!nom.isEmpty()? nom : ("Carte "+id)); double s=o.optDouble("soldeCent", o.optDouble("solde", 0)); String dev=o.optString("devise","TND"); String num=o.optString("numero", o.optString("numeroCarte","")); h.title.setText(title); h.solde.setText(String.format(java.util.Locale.getDefault(), "%.0f %s", s, dev)); h.numero.setText(num.isEmpty()? "" : ("Carte #"+num)); h.devise.setText(dev); h.recharger.setOnClickListener(v->{ int m=parse(h.montant.getText().toString()); if(m>0) cb.onRecharger(id,m); }); }
    private int parse(String s){ try{ return Integer.parseInt(s.trim()); } catch(Exception e){ return 0; } }
    @Override public int getItemCount(){ return data.size(); }
}
