package jnb.fitness.client;
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

public class ClientSeancesAdapter extends RecyclerView.Adapter<ClientSeancesAdapter.VH> {
    public interface Callbacks { void onReserve(long seanceId); void onCancel(long reservationId); }
    private final List<JSONObject> data; private final Callbacks cb;
    public ClientSeancesAdapter(List<JSONObject> data, Callbacks cb){ this.data=data; this.cb=cb; }
    static class VH extends RecyclerView.ViewHolder { TextView title, metaDate, metaTime, places, statut, coach, salle, type, duree; Button action; VH(View v){ super(v); title=v.findViewById(R.id.title); metaDate=v.findViewById(R.id.meta_date); metaTime=v.findViewById(R.id.meta_time); places=v.findViewById(R.id.places); statut=v.findViewById(R.id.statut); coach=v.findViewById(R.id.coach); salle=v.findViewById(R.id.salle); type=v.findViewById(R.id.type); duree=v.findViewById(R.id.duree); action=v.findViewById(R.id.btn_action);} }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent,int viewType){ View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_seance, parent, false); return new VH(v);} 
    @Override public void onBindViewHolder(@NonNull VH h,int i){ JSONObject o=data.get(i); long id=o.optLong("id"); String nom=o.optString("coursNom", o.optString("nom")); String date=o.optString("date"); String heure=o.optString("heure", o.optString("heureDebut","")); int dispo=o.optInt("placesDisponibles", o.optInt("places", 0)); boolean reserved=o.optBoolean("reserved", false); long reservationId=o.optLong("reservationId"); String s=o.optString("statut"); String coach=o.optString("coachNom", o.optString("coach","")); String salle=o.optString("salleNom", o.optString("salle","")); String type=o.optString("type", o.optString("typeSeance","")); int duree=o.optInt("dureeMinutes", 0);
        h.title.setText(nom==null?"":nom);
        if(h.metaDate!=null){ if(date==null || date.isEmpty()){ h.metaDate.setVisibility(View.GONE); } else { h.metaDate.setVisibility(View.VISIBLE); h.metaDate.setText(date); } }
        if(h.metaTime!=null){ if(heure==null || heure.isEmpty()){ h.metaTime.setVisibility(View.GONE); } else { h.metaTime.setVisibility(View.VISIBLE); h.metaTime.setText(heure.length()>=5? heure.substring(0,5): heure); } }
        h.places.setText("Places restantes: "+dispo);
        if(reserved){ h.statut.setText(s==null||s.isEmpty()?"RESERVÉE":s); } else { h.statut.setText("DISPONIBLE"); }
        if(h.coach!=null){ if(coach.isEmpty()){ h.coach.setVisibility(View.GONE);} else { h.coach.setVisibility(View.VISIBLE); h.coach.setText("Coach: "+coach);} }
        if(h.salle!=null){ if(salle.isEmpty()){ h.salle.setVisibility(View.GONE);} else { h.salle.setVisibility(View.VISIBLE); h.salle.setText("Salle: "+salle);} }
        if(h.type!=null){ if(type.isEmpty()){ h.type.setVisibility(View.GONE);} else { h.type.setVisibility(View.VISIBLE); h.type.setText("Type: "+type);} }
        if(h.duree!=null){ if(duree<=0){ h.duree.setVisibility(View.GONE);} else { h.duree.setVisibility(View.VISIBLE); h.duree.setText("Durée: "+duree+" min"); } }
        if(reserved){ h.action.setText("Annuler"); h.action.setOnClickListener(v->cb.onCancel(reservationId)); } else { h.action.setText("Réserver"); h.action.setOnClickListener(v->cb.onReserve(id)); }
    }
    @Override public int getItemCount(){ return data.size(); }
}
