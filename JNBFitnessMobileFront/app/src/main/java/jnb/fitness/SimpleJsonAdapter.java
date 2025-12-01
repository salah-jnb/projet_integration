package jnb.fitness;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.List;

public class SimpleJsonAdapter extends RecyclerView.Adapter<SimpleJsonAdapter.VH> {
    private final List<JSONObject> data; private final int layout; private final int[] textViews;
    public SimpleJsonAdapter(List<JSONObject> data, int layout, int[] textViews){ this.data=data; this.layout=layout; this.textViews=textViews; }
    static class VH extends RecyclerView.ViewHolder { TextView[] tvs; VH(View v, int[] ids){ super(v); tvs=new TextView[ids.length]; for(int i=0;i<ids.length;i++) tvs[i]=v.findViewById(ids[i]); } }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent,int viewType){ View v= LayoutInflater.from(parent.getContext()).inflate(layout,parent,false); return new VH(v, textViews);} 
    @Override public void onBindViewHolder(@NonNull VH h,int i){ JSONObject o=data.get(i); String nom=o.optJSONObject("typeAbonnement")!=null?o.optJSONObject("typeAbonnement").optString("nom"):o.optString("nom"); String debut=o.optString("dateDebut"); String fin=o.optString("dateFin"); String statut=o.optString("statut"); if(h.tvs.length>0) h.tvs[0].setText(nom); if(h.tvs.length>1) h.tvs[1].setText("Du "+debut+" au "+fin); if(h.tvs.length>2) h.tvs[2].setText(statut); }
    @Override public int getItemCount(){ return data.size(); }
}