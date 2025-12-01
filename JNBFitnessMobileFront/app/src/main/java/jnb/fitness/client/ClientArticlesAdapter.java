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

public class ClientArticlesAdapter extends RecyclerView.Adapter<ClientArticlesAdapter.VH> {
    public interface Callbacks { void onView(long id); }
    private final List<JSONObject> data; private final Callbacks cb;
    public ClientArticlesAdapter(List<JSONObject> data, Callbacks cb){ this.data=data; this.cb=cb; }
    static class VH extends RecyclerView.ViewHolder { TextView title, author, status; android.widget.ImageView cover; Button view; VH(View v){ super(v); title=v.findViewById(R.id.title); author=v.findViewById(R.id.author); status=v.findViewById(R.id.status); cover=v.findViewById(R.id.cover); view=v.findViewById(R.id.btn_view);} }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent,int viewType){ View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_article, parent, false); return new VH(v);} 
    @Override public void onBindViewHolder(@NonNull VH h,int i){ JSONObject o=data.get(i); long id=o.optLong("id"); String titre=o.optString("titre"); String auteur=(o.optString("coachPrenom"," ")+" "+o.optString("coachNom"," ")).trim(); String statut=o.optString("statut"); String imageUrl=o.optString("imageUrl"); h.title.setText(titre); h.author.setText(auteur); h.status.setText(statut); if(imageUrl!=null && !imageUrl.isEmpty()){ h.cover.setVisibility(View.VISIBLE); if(imageUrl.startsWith("data:image")){ try{ int comma=imageUrl.indexOf(','); String b64 = comma>=0? imageUrl.substring(comma+1): imageUrl; byte[] bytes = android.util.Base64.decode(b64, android.util.Base64.DEFAULT); android.graphics.Bitmap bm = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length); h.cover.setImageBitmap(bm);} catch(Exception e){ h.cover.setVisibility(View.GONE);} } else { final String fullUrl = imageUrl.startsWith("http")? imageUrl : jnb.fitness.UrlConfig.getApiBaseUrl(h.itemView.getContext()) + (imageUrl.startsWith("/")? imageUrl : ("/"+imageUrl)); java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> { try { java.net.URL url = new java.net.URL(fullUrl); java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection(); conn.setConnectTimeout(8000); conn.setReadTimeout(15000); conn.setRequestMethod("GET"); String token = new jnb.fitness.SessionManager(h.itemView.getContext()).getToken(); if(token!=null){ conn.setRequestProperty("Authorization", "Bearer "+token); } android.graphics.Bitmap bm = android.graphics.BitmapFactory.decodeStream(conn.getInputStream()); conn.disconnect(); if(bm!=null){ h.cover.post(() -> h.cover.setImageBitmap(bm)); } else { h.cover.post(() -> h.cover.setVisibility(View.GONE)); } } catch(Exception e){ h.cover.post(() -> h.cover.setVisibility(View.GONE)); } }); } } else { h.cover.setVisibility(View.GONE);} h.view.setOnClickListener(v->cb.onView(id)); }
    @Override public int getItemCount(){ return data.size(); }
}
