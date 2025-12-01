package jnb.fitness.coach;
import jnb.fitness.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.List;

public class CoachReservationsAdapter extends RecyclerView.Adapter<CoachReservationsAdapter.VH> {
    public interface Callbacks { void onCancel(long id); void onComplete(long id); void onConfirm(long id); }
    private final List<JSONObject> data; private final Callbacks cb;
    public CoachReservationsAdapter(List<JSONObject> data, Callbacks cb) { this.data = data; this.cb = cb; }
    static class VH extends RecyclerView.ViewHolder { TextView date, statut, clientName, sessionType, duration; Button cancel, complete, confirm; VH(View v){ super(v); date=v.findViewById(R.id.date); statut=v.findViewById(R.id.statut); clientName=v.findViewById(R.id.client_name); sessionType=v.findViewById(R.id.session_type); duration=v.findViewById(R.id.duration); cancel=v.findViewById(R.id.btn_cancel); complete=v.findViewById(R.id.btn_complete); confirm=v.findViewById(R.id.btn_confirm);} }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent,int viewType){ View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coach_reservation, parent, false); return new VH(v);} 
    @Override public void onBindViewHolder(@NonNull VH h,int i){ JSONObject o=data.get(i); long id=o.optLong("id"); String d=o.optString("dateSeance"); String s=o.optString("statut"); h.date.setText(d); h.statut.setText(s);
        String nom=o.optString("clientNom", ""); String prenom=o.optString("clientPrenom", ""); String fullName=(prenom+" "+nom).trim(); if(fullName.isEmpty()){ JSONObject client=o.optJSONObject("client"); if(client!=null){ JSONObject u=client.optJSONObject("utilisateur"); if(u!=null){ fullName=(u.optString("prenom","")+" "+u.optString("nom","")); } } }
        h.clientName.setText(fullName);
        String type=o.optString("typeSeance", ""); h.sessionType.setText(type);
        int duree=o.optInt("dureeMinutes", 60); h.duration.setText(duree+" minutes");
        h.cancel.setOnClickListener(v->cb.onCancel(id));
        h.complete.setOnClickListener(v->cb.onComplete(id));
        h.confirm.setOnClickListener(v->cb.onConfirm(id));
        h.confirm.setVisibility(View.GONE); h.cancel.setVisibility(View.GONE); h.complete.setVisibility(View.GONE);
        if("EN_ATTENTE".equalsIgnoreCase(s)){
            h.confirm.setVisibility(View.VISIBLE);
            h.cancel.setVisibility(View.VISIBLE);
        } else if("CONFIRMEE".equalsIgnoreCase(s)){
            h.complete.setVisibility(View.VISIBLE);
            try{ java.text.SimpleDateFormat f=new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()); java.util.Date start=f.parse(d); boolean enable=start!=null && new java.util.Date().getTime()>=start.getTime(); h.complete.setEnabled(enable); } catch(Exception e){ h.complete.setEnabled(true);} 
        }
    }
    @Override public int getItemCount(){ return data.size(); }
}
