package jnb.fitness.client;
import jnb.fitness.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.List;

public class ClientAbonnementsAdapter extends RecyclerView.Adapter<ClientAbonnementsAdapter.VH> {
    private final List<JSONObject> data;
    public ClientAbonnementsAdapter(List<JSONObject> data){ this.data = data; }

    static class VH extends RecyclerView.ViewHolder {
        TextView title; TextView dates; TextView meta; TextView statut; TextView referral;
        VH(View v){ super(v); title=v.findViewById(R.id.title); dates=v.findViewById(R.id.dates); meta=v.findViewById(R.id.meta); statut=v.findViewById(R.id.statut); referral=v.findViewById(R.id.badgeParrainage); }
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent,int viewType){ View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_abonnement, parent, false); return new VH(v);} 

    @Override public void onBindViewHolder(@NonNull VH h,int i){ JSONObject o=data.get(i); String nom=o.optJSONObject("typeAbonnement")!=null?o.optJSONObject("typeAbonnement").optString("nom"):o.optString("typeNom", o.optString("nom")); String debut=o.optString("dateDebut"); String fin=o.optString("dateFin"); String statut=o.optString("statut"); int seances=o.optInt("seancesRestantes", -1); boolean offrPar=o.optBoolean("offrirParParrainage", false); h.title.setText(nom); h.dates.setText("Du "+formatDate(debut)+(fin==null||fin.isEmpty()||"null".equals(fin)? "" : " au "+formatDate(fin))); h.meta.setText(seances>-1? ("Séances restantes: "+seances) : ""); h.meta.setVisibility(seances>-1? View.VISIBLE: View.GONE); h.statut.setText(statut); int color = android.graphics.Color.parseColor(statut.equalsIgnoreCase("ACTIF")?"#2e7d32":"#616161"); h.statut.setBackgroundColor(color); if(h.referral!=null){ h.referral.setVisibility(offrPar? View.VISIBLE: View.GONE); } }

    @Override public int getItemCount(){ return data.size(); }

    private String formatDate(String s){ try{ if(s==null||s.isEmpty()||"null".equals(s)) return ""; java.text.DateFormat in = s.contains("T")? new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()) : new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()); java.util.Date d=in.parse(s); return new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(d);} catch(Exception ignored){} return s; }
}

