package jnb.fitness;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.List;

public class SimpleDeleteAdapter extends RecyclerView.Adapter<SimpleDeleteAdapter.VH> {
    public interface Callbacks { void onDelete(long id); void onEdit(JSONObject item); }
    private final List<JSONObject> data; private final boolean showEdit; private Callbacks cb;
    public SimpleDeleteAdapter(List<JSONObject> data){ this(data, false); }
    public SimpleDeleteAdapter(List<JSONObject> data, boolean showEdit){ this.data=data; this.showEdit=showEdit; }
    public void setCallbacks(Callbacks cb){ this.cb=cb; }
    static class VH extends RecyclerView.ViewHolder { TextView title, subtitle; Button edit, delete; VH(View v){ super(v); title=v.findViewById(R.id.title); subtitle=v.findViewById(R.id.subtitle); edit=v.findViewById(R.id.btn_edit); delete=v.findViewById(R.id.btn_delete);} }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent,int viewType){ View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_delete, parent, false); return new VH(v);} 
    @Override public void onBindViewHolder(@NonNull VH h,int i){ JSONObject o=data.get(i); long id=o.optLong("id"); String titre=o.optString("nom", o.optString("titre","")); String desc=o.optString("description", ""); h.title.setText(titre); h.subtitle.setText(desc); h.edit.setVisibility(showEdit? View.VISIBLE: View.GONE); h.edit.setOnClickListener(v->{ if(cb!=null) cb.onEdit(o); }); h.delete.setOnClickListener(v->{ if(cb!=null) cb.onDelete(id); }); }
    @Override public int getItemCount(){ return data.size(); }
}