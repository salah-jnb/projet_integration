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

public class ClientNotificationsAdapter extends RecyclerView.Adapter<ClientNotificationsAdapter.VH> {
    public interface Callbacks { void onClick(long id); void onMarkRead(long id); }
    private final List<JSONObject> data; private final Callbacks cb;
    public ClientNotificationsAdapter(List<JSONObject> data, Callbacks cb){ this.data=data; this.cb=cb; }
    static class VH extends RecyclerView.ViewHolder { TextView title, message, date, type; android.widget.Button btnMark; VH(View v){ super(v); title=v.findViewById(R.id.title); message=v.findViewById(R.id.message); date=v.findViewById(R.id.date); type=v.findViewById(R.id.type); btnMark=v.findViewById(R.id.btn_mark_read);} }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent,int viewType){ View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_notification, parent, false); return new VH(v);} 
    @Override public void onBindViewHolder(@NonNull VH h,int i){ JSONObject o=data.get(i); long id=o.optLong("id"); h.title.setText(o.optString("titre")); h.message.setText(o.optString("message")); h.date.setText(o.optString("dateEnvoi")); String tp=o.optString("type"); if(h.type!=null){ if(tp==null||tp.isEmpty()){ h.type.setVisibility(View.GONE);} else { h.type.setVisibility(View.VISIBLE); h.type.setText(tp); } } if(h.btnMark!=null){ h.btnMark.setOnClickListener(v-> cb.onMarkRead(id)); } h.itemView.setOnClickListener(v-> cb.onClick(id)); }
    @Override public int getItemCount(){ return data.size(); }
}
