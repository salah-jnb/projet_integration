package jnb.fitness.coach;
import jnb.fitness.R;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.List;

public class CoachNotificationsAdapter extends RecyclerView.Adapter<CoachNotificationsAdapter.VH> {
    public interface Callbacks { void onMarkRead(long id); }
    private final List<JSONObject> data; private final Callbacks cb;
    public CoachNotificationsAdapter(List<JSONObject> data, Callbacks cb){ this.data=data; this.cb=cb; }
    static class VH extends RecyclerView.ViewHolder { TextView title, type, message, date; Button mark; VH(View v){ super(v); title=v.findViewById(R.id.title); type=v.findViewById(R.id.type); message=v.findViewById(R.id.message); date=v.findViewById(R.id.date); mark=v.findViewById(R.id.btn_mark);} }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent,int viewType){ View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coach_notification, parent, false); return new VH(v);} 
    @Override public void onBindViewHolder(@NonNull VH h,int i){ JSONObject n=data.get(i); long id=n.optLong("id"); h.title.setText(n.optString("titre","Notification")); h.message.setText(n.optString("message","")); h.date.setText(n.optString("dateEnvoi","")); String t=n.optString("type","AUTRE"); h.type.setText(t); h.type.setBackgroundResource(R.drawable.badge_status); h.mark.setOnClickListener(v->{ if(cb!=null) cb.onMarkRead(id); }); }
    @Override public int getItemCount(){ return data.size(); }
    private int getTypeColor(String type){ return 0xFF616161; }
}
