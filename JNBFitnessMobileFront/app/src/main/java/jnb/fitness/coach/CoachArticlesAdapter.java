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

public class CoachArticlesAdapter extends RecyclerView.Adapter<CoachArticlesAdapter.VH> {
    public interface Callbacks { void onPublish(long id); void onDelete(long id); void onEdit(JSONObject article); }
    private final List<JSONObject> data; private final Callbacks cb;
    public CoachArticlesAdapter(List<JSONObject> data, Callbacks cb){ this.data=data; this.cb=cb; }
    static class VH extends RecyclerView.ViewHolder { TextView title, statut, contenu, dates; ImageView image; Button publish, edit, delete; VH(View v){ super(v); title=v.findViewById(R.id.title); statut=v.findViewById(R.id.statut); contenu=v.findViewById(R.id.contenu); dates=v.findViewById(R.id.dates); image=v.findViewById(R.id.image); publish=v.findViewById(R.id.btn_publish); edit=v.findViewById(R.id.btn_edit); delete=v.findViewById(R.id.btn_delete);} }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent,int viewType){ View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coach_article, parent, false); return new VH(v);} 
    @Override public void onBindViewHolder(@NonNull VH h,int i){
        JSONObject a = data.get(i);
        long id = a.optLong("id");
        String titre = a.optString("titre","Article");
        String statut = a.optString("statut","BROUILLON");
        String contenu = a.optString("contenu","");
        String img = a.optString("imageUrl","");
        String created = a.optString("dateCreation","");
        String pub = a.optString("datePublication","");
        h.title.setText(titre);
        h.statut.setText(statut.replace("_"," "));
        h.contenu.setText(contenu);
        h.dates.setText((created.isEmpty()?"":"Créé le "+created)+(pub.isEmpty()?"":" • Publié le "+pub));
        h.publish.setVisibility("BROUILLON".equals(statut)? View.VISIBLE: View.GONE);
        h.publish.setOnClickListener(v->{ if(cb!=null) cb.onPublish(id); });
        h.edit.setOnClickListener(v->{ if(cb!=null) cb.onEdit(a); });
        h.delete.setOnClickListener(v->{ if(cb!=null) cb.onDelete(id); });
        if(img==null || img.isEmpty()){
            h.image.setVisibility(View.GONE);
        } else {
            h.image.setVisibility(View.VISIBLE);
            if(img.startsWith("data:image")){
                try{
                    int comma = img.indexOf(',');
                    String b64 = comma>=0? img.substring(comma+1): img;
                    byte[] bytes = Base64.decode(b64, Base64.DEFAULT);
                    Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    h.image.setImageBitmap(bm);
                } catch(Exception e){
                    h.image.setVisibility(View.GONE);
                }
            } else {
                String base = UrlConfig.getApiBaseUrl(h.image.getContext());
                if(base.endsWith("/")) base = base.substring(0, base.length()-1);
                final String fullUrl = img.startsWith("http") ? img : (base + (img.startsWith("/")? img : ("/"+img)));
                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        URL url = new URL(fullUrl);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(8000);
                        conn.setReadTimeout(15000);
                        conn.setRequestMethod("GET");
                        conn.setRequestProperty("Accept", "image/*");
                        if(fullUrl.contains("/api/")){
                            String token = new jnb.fitness.SessionManager(h.image.getContext()).getToken();
                            if(token!=null){ conn.setRequestProperty("Authorization", "Bearer "+token); }
                        }
                        Bitmap bm = BitmapFactory.decodeStream(conn.getInputStream());
                        conn.disconnect();
                        if(bm!=null){ h.image.post(() -> h.image.setImageBitmap(bm)); } else { h.image.post(() -> h.image.setVisibility(View.GONE)); }
                    } catch(Exception e){ h.image.post(() -> h.image.setVisibility(View.GONE)); }
                });
            }
        }
    }
    @Override public int getItemCount(){ return data.size(); }
}
