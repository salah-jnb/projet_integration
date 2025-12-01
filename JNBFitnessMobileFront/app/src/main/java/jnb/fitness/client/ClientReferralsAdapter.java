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

public class ClientReferralsAdapter extends RecyclerView.Adapter<ClientReferralsAdapter.VH> {
    private final List<JSONObject> data;
    public ClientReferralsAdapter(List<JSONObject> data){ this.data=data; }
    static class VH extends RecyclerView.ViewHolder { TextView title, statut, meta; VH(View v){ super(v); title=v.findViewById(R.id.title); statut=v.findViewById(R.id.statut); meta=v.findViewById(R.id.meta);} }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent,int viewType){ View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_referral, parent, false); return new VH(v);} 
    @Override public void onBindViewHolder(@NonNull VH h,int i){ JSONObject o=data.get(i); String nom=(o.optString("filleulPrenom","")+" "+o.optString("filleulNom"," ")).trim(); boolean valide=o.optBoolean("valide", false); String statut=valide? "VALIDE": "EN_ATTENTE"; String dateIns=o.optString("dateInscriptionFilleul"); String dateVal=o.optString("dateValidation"); String meta=""; if(dateIns!=null && dateIns.length()>=10){ meta+=dateIns.substring(0,10); } if(valide && dateVal!=null && dateVal.length()>=10){ meta+="\nValidé le "+dateVal.substring(0,10); } if(o.optBoolean("moisGratuitAttribue", false)){ meta+="\nMois gratuit attribué"; } h.title.setText(nom); h.statut.setText(statut); h.meta.setText(meta); }
    @Override public int getItemCount(){ return data.size(); }
}
