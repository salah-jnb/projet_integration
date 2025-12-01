package jnb.fitness.admin;
import jnb.fitness.R;
import jnb.fitness.UrlConfig;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.List;

public class AdminArticlesAdapter extends RecyclerView.Adapter<AdminArticlesAdapter.VH> {
    public interface Callbacks { void onValidate(long id); void onReject(long id); void onView(long id); }
    private final List<JSONObject> data; private final Callbacks cb;
    public AdminArticlesAdapter(List<JSONObject> data, Callbacks cb){ this.data=data; this.cb=cb; }
    static class VH extends RecyclerView.ViewHolder { android.widget.ImageView image; TextView title, author, content, statut; Button validate, reject; VH(View v){ super(v); image=v.findViewById(R.id.image); title=v.findViewById(R.id.title); author=v.findViewById(R.id.author); content=v.findViewById(R.id.content); statut=v.findViewById(R.id.statut); validate=v.findViewById(R.id.btn_validate); reject=v.findViewById(R.id.btn_reject);} }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent,int viewType){ View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_article, parent, false); return new VH(v);} 
    @Override public void onBindViewHolder(@NonNull VH h,int i){ JSONObject o=data.get(i); long id=o.optLong("id"); String titre=o.optString("titre"); String auteur=(o.optString("coachPrenom","")+" "+o.optString("coachNom","")).trim(); String statut=o.optString("statut","EN_ATTENTE_VALIDATION"); String contenu=o.optString("contenu"); h.title.setText(titre); h.author.setText("Par "+auteur); h.content.setText(contenu); h.statut.setText(statut);
        String imageUrlRaw = o.optString("imageUrl", "");
        String imageBase64 = imageUrlRaw.isEmpty() ? o.optString("imageBase64", o.optString("image", "")) : "";
        String resolved = null;
        if (!imageUrlRaw.isEmpty()) {
            String base = UrlConfig.getApiBaseUrl(h.image.getContext());
            if (imageUrlRaw.startsWith("http")) {
                resolved = imageUrlRaw;
            } else if (imageUrlRaw.startsWith("/")) {
                resolved = base + imageUrlRaw.substring(1);
            } else if (imageUrlRaw.contains("uploads/")) {
                resolved = base + (imageUrlRaw.startsWith("uploads/") ? imageUrlRaw : ("uploads/" + imageUrlRaw));
            } else {
                resolved = base + "uploads/articles/" + imageUrlRaw;
            }
        }
        if(resolved != null){
            try { com.bumptech.glide.Glide.with(h.image.getContext()).load(resolved).into(h.image); h.image.setVisibility(android.view.View.VISIBLE);} catch(Exception ignored){ h.image.setVisibility(android.view.View.GONE);} }
        else if(!imageBase64.isEmpty()){
            try { String data = imageBase64.startsWith("data:")? imageBase64.substring(imageBase64.indexOf(",")+1): imageBase64; byte[] bytes = android.util.Base64.decode(data, android.util.Base64.DEFAULT); android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(bytes,0,bytes.length); h.image.setImageBitmap(bmp); h.image.setVisibility(android.view.View.VISIBLE);} catch(Exception e){ h.image.setVisibility(android.view.View.GONE);} }
        else { h.image.setVisibility(android.view.View.GONE); }
        h.validate.setOnClickListener(v->cb.onValidate(id)); h.reject.setOnClickListener(v->cb.onReject(id)); }
    @Override public int getItemCount(){ return data.size(); }
}
