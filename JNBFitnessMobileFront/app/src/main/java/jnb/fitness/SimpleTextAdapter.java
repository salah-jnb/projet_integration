package jnb.fitness;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.List;

public class SimpleTextAdapter extends RecyclerView.Adapter<SimpleTextAdapter.VH> {
    private final List<JSONObject> data;
    public SimpleTextAdapter(List<JSONObject> data){ this.data=data; }
    static class VH extends RecyclerView.ViewHolder { TextView title, subtitle; VH(View v){ super(v); title=v.findViewById(R.id.title); subtitle=v.findViewById(R.id.subtitle);} }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent,int viewType){ View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_text, parent, false); return new VH(v);} 
    @Override public void onBindViewHolder(@NonNull VH h,int i){ JSONObject o=data.get(i); String t=o.optString("reference", o.optString("nom","")); String s=o.optString("statut", o.optString("montant","")); h.title.setText(t); h.subtitle.setText(s); }
    @Override public int getItemCount(){ return data.size(); }
}