package jnb.fitness.client;
import jnb.fitness.R;
import jnb.fitness.ApiClient;
import jnb.fitness.SessionManager;
import jnb.fitness.LandingActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class ClientArticlesActivity extends AppCompatActivity implements ClientArticlesAdapter.Callbacks {
    private RecyclerView list; private ProgressBar progress; private TextView empty; private final ArrayList<JSONObject> data = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_client);
        android.view.ViewGroup content = findViewById(R.id.content);
        android.view.View page = android.view.LayoutInflater.from(this).inflate(R.layout.activity_client_articles, content, false);
        content.addView(page);
        list = page.findViewById(R.id.list);
        progress = page.findViewById(R.id.progress);
        empty = page.findViewById(R.id.empty);
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        androidx.drawerlayout.widget.DrawerLayout drawer = findViewById(R.id.drawer);
        androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        com.google.android.material.navigation.NavigationView nav = findViewById(R.id.nav);
        nav.setNavigationItemSelectedListener(item -> { drawer.closeDrawers(); route(item.getItemId()); return true; });
        jnb.fitness.NavigationHeaderUtil.applyForClient(this);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(new ClientArticlesAdapter(data, this));
        load();
    }
    private void route(int id){ if(id==R.id.nav_dashboard){ startActivity(new android.content.Intent(this, ClientActivity.class)); finish(); } else if(id==R.id.nav_articles){} else if(id==R.id.nav_abonnements){ startActivity(new android.content.Intent(this, ClientAbonnementsActivity.class)); finish(); } else if(id==R.id.nav_carte){ startActivity(new android.content.Intent(this, ClientCartesActivity.class)); finish(); } else if(id==R.id.nav_coaching){ startActivity(new android.content.Intent(this, ClientCoachingActivity.class)); finish(); } else if(id==R.id.nav_cours){ startActivity(new android.content.Intent(this, ClientCollectiveClassesActivity.class)); finish(); } else if(id==R.id.nav_parrainage){ startActivity(new android.content.Intent(this, ClientReferralsActivity.class)); finish(); } else if(id==R.id.nav_notifications){ startActivity(new android.content.Intent(this, ClientNotificationsActivity.class)); finish(); } else if(id==R.id.nav_logout){ new SessionManager(this).logout(); startActivity(new android.content.Intent(this, LandingActivity.class)); finish(); } }
    private void load(){ progress.setVisibility(View.VISIBLE); empty.setVisibility(View.GONE); Executors.newSingleThreadExecutor().execute(() -> { try { ApiClient api=new ApiClient(this); JSONArray arr=api.getAuthArray("api/Articles"); data.clear(); for(int i=0;i<arr.length();i++) data.add(arr.getJSONObject(i)); runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(data.isEmpty()?View.VISIBLE:View.GONE); RecyclerView.Adapter<?> a=list.getAdapter(); if(a!=null){ a.notifyDataSetChanged(); } }); } catch(Exception e){ runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(View.VISIBLE); }); } }); }
    @Override public void onView(long id){ Executors.newSingleThreadExecutor().execute(() -> { try { JSONObject art=new ApiClient(this).getAuthJson("api/Articles/"+id); runOnUiThread(() -> { android.widget.ScrollView sv=new android.widget.ScrollView(this); android.widget.LinearLayout ll=new android.widget.LinearLayout(this); ll.setOrientation(android.widget.LinearLayout.VERTICAL); ll.setPadding(32,32,32,0); android.widget.TextView t=new android.widget.TextView(this); t.setText(art.optString("titre")); t.setTextColor(getResources().getColor(R.color.white)); t.setTextSize(18); android.widget.TextView meta=new android.widget.TextView(this); meta.setText("Par "+art.optString("coachPrenom"," ")+" "+art.optString("coachNom","")); meta.setTextColor(getResources().getColor(R.color.white)); android.widget.ImageView img=new android.widget.ImageView(this); String imageUrl=art.optString("imageUrl"); if(imageUrl!=null && !imageUrl.isEmpty()){ img.setAdjustViewBounds(true); img.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP); if(imageUrl.startsWith("http")){ String full=imageUrl; loadImageInto(img, full); } else { String full = jnb.fitness.UrlConfig.getApiBaseUrl(this)+(imageUrl.startsWith("/")? imageUrl : ("/"+imageUrl)); loadImageInto(img, full); } ll.addView(img); } android.widget.TextView c=new android.widget.TextView(this); c.setText(art.optString("contenu")); c.setTextColor(getResources().getColor(R.color.white)); ll.addView(t); ll.addView(meta); ll.addView(c); sv.addView(ll); new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Article").setView(sv).setPositiveButton("Fermer", (d,w)-> d.dismiss()).show(); }); } catch(Exception ignored){} }); }

    private void loadImageInto(android.widget.ImageView view,String url){ Executors.newSingleThreadExecutor().execute(() -> { try { java.net.URL u=new java.net.URL(url); java.net.HttpURLConnection conn=(java.net.HttpURLConnection) u.openConnection(); conn.setConnectTimeout(8000); conn.setReadTimeout(15000); conn.setRequestMethod("GET"); String token=new SessionManager(this).getToken(); if(token!=null){ conn.setRequestProperty("Authorization", "Bearer "+token); } android.graphics.Bitmap bm=android.graphics.BitmapFactory.decodeStream(conn.getInputStream()); conn.disconnect(); if(bm!=null){ view.post(() -> view.setImageBitmap(bm)); } } catch(Exception ignored){} }); }
}
