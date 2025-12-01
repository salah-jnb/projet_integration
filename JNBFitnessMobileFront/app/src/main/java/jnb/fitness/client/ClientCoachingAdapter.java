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

public class ClientCoachingAdapter extends RecyclerView.Adapter<ClientCoachingAdapter.VH> {
    public interface Callbacks { void onCancel(long id); void onRate(long reservationId, long coachId, int note, String commentaire); void onRatePrompt(long reservationId, long coachId); }
    private final List<JSONObject> data; private final Callbacks cb;
    public ClientCoachingAdapter(List<JSONObject> data, Callbacks cb){ this.data=data; this.cb=cb; }
    static class VH extends RecyclerView.ViewHolder { TextView title, date, statut; Button cancel, rate; VH(View v){ super(v); title=v.findViewById(R.id.title); date=v.findViewById(R.id.date); statut=v.findViewById(R.id.statut); cancel=v.findViewById(R.id.btn_cancel); rate=v.findViewById(R.id.btn_rate);} }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent,int viewType){ View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_coaching, parent, false); return new VH(v);} 
    @Override public void onBindViewHolder(@NonNull VH h,int i){ JSONObject o=data.get(i); long id=o.optLong("id"); long coachId=o.optLong("coachId"); String coachName=(o.optString("coachPrenom","")+" "+o.optString("coachNom",""))
            .trim(); String d=o.optString("dateSeance"); String s=o.optString("statut"); h.title.setText(coachName.isEmpty()?"Coach "+coachId:coachName); h.date.setText(d); h.statut.setText(s);
        boolean canCancel = "EN_ATTENTE".equalsIgnoreCase(s) || "CONFIRMEE".equalsIgnoreCase(s);
        boolean canRate = "TERMINEE".equalsIgnoreCase(s);
        h.cancel.setVisibility(canCancel? View.VISIBLE: View.GONE);
        h.rate.setVisibility(canRate? View.VISIBLE: View.GONE);
        h.cancel.setOnClickListener(v->cb.onCancel(id));
        h.rate.setOnClickListener(v->{ cb.onRatePrompt(id, coachId); }); }
    @Override public int getItemCount(){ return data.size(); }
}
