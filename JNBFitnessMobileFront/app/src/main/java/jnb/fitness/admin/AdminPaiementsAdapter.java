package jnb.fitness.admin;
import jnb.fitness.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.List;

public class AdminPaiementsAdapter extends RecyclerView.Adapter<AdminPaiementsAdapter.VH> {
    private final List<JSONObject> data;
    public AdminPaiementsAdapter(List<JSONObject> data){ this.data=data; }
    static class VH extends RecyclerView.ViewHolder {
        TextView client;
        TextView abonnement;
        TextView meta;
        TextView montant;
        TextView statut;
        VH(View v){
            super(v);
            client = v.findViewById(R.id.client);
            abonnement = v.findViewById(R.id.abonnement);
            meta = v.findViewById(R.id.meta);
            montant = v.findViewById(R.id.montant);
            statut = v.findViewById(R.id.statut);
        }
    }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent,int viewType){ View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_paiement, parent, false); return new VH(v);} 
    @Override public void onBindViewHolder(@NonNull VH h,int i){ JSONObject o=data.get(i); String nom=o.optString("clientNom"); String prenom=o.optString("clientPrenom"); String abo=o.optString("abonnementNom"); String date=o.optString("datePaiement"); String methode=o.optString("methodePaiement"); double m=o.optDouble("montant", 0); String statut=o.optString("statut"); h.client.setText((prenom+" "+nom).trim()); h.abonnement.setText(abo); h.meta.setText((date.isEmpty()?"":date)+(!methode.isEmpty()?"  •  "+methode:"")); h.montant.setText(String.format(java.util.Locale.getDefault(), "%.0f TND", m)); h.statut.setText(statut); int color = android.graphics.Color.parseColor(statut.equalsIgnoreCase("VALIDE")?"#2e7d32": statut.equalsIgnoreCase("EN_ATTENTE")?"#f9a825":"#616161"); h.statut.setBackgroundColor(color); h.statut.setPadding(8,8,8,8); }
    @Override public int getItemCount(){ return data.size(); }
}
