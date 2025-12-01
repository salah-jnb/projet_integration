package jnb.fitness.admin;
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

public class AdminCoursAdapter extends RecyclerView.Adapter<AdminCoursAdapter.VH> {
    public interface Callbacks { void onDelete(long id); void onEdit(JSONObject course); void onCreateSeance(long courseId); void onViewSeances(long courseId); }
    private final List<JSONObject> data; private final Callbacks cb;
    public AdminCoursAdapter(List<JSONObject> data, Callbacks cb) { this.data = data; this.cb = cb; }
    static class VH extends RecyclerView.ViewHolder { TextView title, coach, horaires, capacite; Button viewSeances, actions; VH(View v){ super(v); title=v.findViewById(R.id.title); coach=v.findViewById(R.id.coach); horaires=v.findViewById(R.id.horaires); capacite=v.findViewById(R.id.capacite); viewSeances=v.findViewById(R.id.btn_view_seances); actions=v.findViewById(R.id.btn_actions);} }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent,int viewType){ View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_cours,parent,false); return new VH(v);}    
    @Override public void onBindViewHolder(@NonNull VH h,int i){ JSONObject o=data.get(i); long id=o.optLong("id"); String nom=o.optString("nom"); String coach=o.optString("coachNom"); String jour=o.optString("jourSemaine", o.optString("jour")); String debut=o.optString("heureDebut"); String fin=o.optString("heureFin"); int cap=o.optInt("capaciteMax", o.optInt("capacite")); h.title.setText(nom); h.coach.setText("Coach: "+coach); h.horaires.setText(jour+" " + debut + " - " + fin); h.capacite.setText("Capacité: "+cap); h.viewSeances.setOnClickListener(v->cb.onViewSeances(id)); h.actions.setOnClickListener(v->{ androidx.appcompat.widget.PopupMenu pm=new androidx.appcompat.widget.PopupMenu(v.getContext(), v); pm.getMenu().add("Modifier").setOnMenuItemClickListener(mi->{ cb.onEdit(o); return true;}); pm.getMenu().add("Créer séance").setOnMenuItemClickListener(mi->{ cb.onCreateSeance(id); return true;}); pm.getMenu().add("Supprimer").setOnMenuItemClickListener(mi->{ cb.onDelete(id); return true;}); pm.show(); }); }
    @Override public int getItemCount(){ return data.size(); }
}
