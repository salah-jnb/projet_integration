package jnb.fitness.coach;
import jnb.fitness.R;
import jnb.fitness.UrlConfig;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executors;

public class CoachProductsAdapter extends RecyclerView.Adapter<CoachProductsAdapter.VH> {
    public interface Callbacks { void onDelete(long id); void onEdit(JSONObject product); }
    private final List<JSONObject> data; private final Callbacks cb; private final boolean readOnly;
    public CoachProductsAdapter(List<JSONObject> data, Callbacks cb){ this(data, false, cb); }
    public CoachProductsAdapter(List<JSONObject> data, boolean readOnly, Callbacks cb){ this.data=data; this.readOnly=readOnly; this.cb=cb; }
    static class VH extends RecyclerView.ViewHolder { TextView title, price, category, desc; ImageView image; Button edit, delete; VH(View v){ super(v); title=v.findViewById(R.id.title); price=v.findViewById(R.id.price); category=v.findViewById(R.id.category); desc=v.findViewById(R.id.description); image=v.findViewById(R.id.image); edit=v.findViewById(R.id.btn_edit); delete=v.findViewById(R.id.btn_delete);} }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent,int viewType){ View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coach_product, parent, false); return new VH(v);} 
    @Override public void onBindViewHolder(@NonNull VH h,int i){ JSONObject o=data.get(i); long id=o.optLong("id"); String nom=o.optString("nom","Produit"); double prix=o.optDouble("prix",0); String cat=o.optString("categorie",""), d=o.optString("description",""), img=o.optString("imageUrl",""); h.title.setText(nom); h.price.setText(String.format(java.util.Locale.getDefault(), "%.2f DT", prix)); h.category.setText(cat); h.desc.setText(d); h.edit.setVisibility(readOnly? View.GONE: View.VISIBLE); h.delete.setVisibility(readOnly? View.GONE: View.VISIBLE); h.edit.setOnClickListener(v->{ if(!readOnly && cb!=null) cb.onEdit(o); }); h.delete.setOnClickListener(v->{ if(!readOnly && cb!=null) cb.onDelete(id); }); if(img==null || img.isEmpty()){ h.image.setVisibility(View.GONE); } else { h.image.setVisibility(View.VISIBLE); if(img.startsWith("data:image")){ try{ int comma=img.indexOf(','); String b64 = comma>=0? img.substring(comma+1): img; byte[] bytes = Base64.decode(b64, Base64.DEFAULT); Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length); h.image.setImageBitmap(bm); } catch(Exception e){ h.image.setVisibility(View.GONE); } } else { String base = UrlConfig.getApiBaseUrl(h.image.getContext()); if(base.endsWith("/")) base = base.substring(0, base.length()-1); final String fullUrl = img.startsWith("http")? img : (base + (img.startsWith("/")? img : ("/"+img))); Executors.newSingleThreadExecutor().execute(() -> { try { URL url = new URL(fullUrl); HttpURLConnection conn = (HttpURLConnection) url.openConnection(); conn.setConnectTimeout(8000); conn.setReadTimeout(15000); conn.setRequestMethod("GET"); conn.setRequestProperty("Accept", "image/*"); if(fullUrl.contains("/api/")){ String token = new jnb.fitness.SessionManager(h.image.getContext()).getToken(); if(token!=null){ conn.setRequestProperty("Authorization", "Bearer "+token); } } Bitmap bm = BitmapFactory.decodeStream(conn.getInputStream()); conn.disconnect(); if(bm!=null){ h.image.post(() -> h.image.setImageBitmap(bm)); } else { h.image.post(() -> h.image.setVisibility(View.GONE)); } } catch(Exception e){ h.image.post(() -> h.image.setVisibility(View.GONE)); } }); } } }
    @Override public int getItemCount(){ return data.size(); }
}
