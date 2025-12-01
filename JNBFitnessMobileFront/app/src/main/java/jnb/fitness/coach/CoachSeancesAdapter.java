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

public class CoachSeancesAdapter extends RecyclerView.Adapter<CoachSeancesAdapter.VH> {
    public interface Callbacks { void onParticipants(JSONObject seance); }
    private final List<JSONObject> data; private final Callbacks cb;
    public CoachSeancesAdapter(List<JSONObject> data, Callbacks cb){ this.data=data; this.cb=cb; }
    static class VH extends RecyclerView.ViewHolder { TextView title, date, places, statut; Button participants; VH(View v){ super(v); title=v.findViewById(R.id.title); date=v.findViewById(R.id.date); places=v.findViewById(R.id.places); statut=v.findViewById(R.id.statut); participants=v.findViewById(R.id.btn_participants);} }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent,int viewType){ View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coach_seance, parent, false); return new VH(v);} 
    @Override public void onBindViewHolder(@NonNull VH h,int i){ JSONObject s=data.get(i); h.title.setText(s.optString("coursNom","Cours")); h.date.setText(s.optString("dateSeance","")); h.places.setText("Places: "+s.optInt("placesDisponibles",0)); h.statut.setText(s.optBoolean("annulee",false)? "ANNULÉE":"DISPONIBLE"); h.participants.setOnClickListener(v->{ if(cb!=null) cb.onParticipants(s); }); }
    @Override public int getItemCount(){ return data.size(); }
}

