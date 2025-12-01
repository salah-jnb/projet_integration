package jnb.fitness.client;
import jnb.fitness.R;
import jnb.fitness.ApiClient;
import jnb.fitness.SessionManager;
import jnb.fitness.LandingActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class ClientProductsActivity extends AppCompatActivity {
    private RecyclerView list; private ProgressBar progress; private TextView empty; private EditText search; private final ArrayList<JSONObject> data = new ArrayList<>(); private final ArrayList<JSONObject> display = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_client);
        android.view.ViewGroup content = findViewById(R.id.content);
        android.view.View page = android.view.LayoutInflater.from(this).inflate(R.layout.activity_client_products, content, false);
        content.addView(page);
        list = page.findViewById(R.id.list);
        progress = page.findViewById(R.id.progress);
        empty = page.findViewById(R.id.empty);
        search = page.findViewById(R.id.search);
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        androidx.drawerlayout.widget.DrawerLayout drawer = findViewById(R.id.drawer);
        androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        com.google.android.material.navigation.NavigationView nav = findViewById(R.id.nav);
        nav.setNavigationItemSelectedListener(item -> { drawer.closeDrawers(); route(item.getItemId()); return true; });
        jnb.fitness.NavigationHeaderUtil.applyForClient(this);
        list.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 1));
        list.setAdapter(new ClientProductsAdapter(display));
        search.addTextChangedListener(new android.text.TextWatcher(){ public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int b,int c){ filter(s.toString()); } public void afterTextChanged(android.text.Editable e){} });
        load();
    }
    private void route(int id){ if(id==R.id.nav_dashboard){ startActivity(new android.content.Intent(this, ClientActivity.class)); finish(); } else if(id==R.id.nav_profil){ startActivity(new android.content.Intent(this, ClientProfileActivity.class)); finish(); } else if(id==R.id.nav_produits){} else if(id==R.id.nav_abonnements){ startActivity(new android.content.Intent(this, ClientAbonnementsActivity.class)); finish(); } else if(id==R.id.nav_carte){ startActivity(new android.content.Intent(this, ClientCartesActivity.class)); finish(); } else if(id==R.id.nav_coaching){ startActivity(new android.content.Intent(this, ClientCoachingActivity.class)); finish(); } else if(id==R.id.nav_cours){ startActivity(new android.content.Intent(this, ClientCollectiveClassesActivity.class)); finish(); } else if(id==R.id.nav_articles){ startActivity(new android.content.Intent(this, ClientArticlesActivity.class)); finish(); } else if(id==R.id.nav_notifications){ startActivity(new android.content.Intent(this, ClientNotificationsActivity.class)); finish(); } else if(id==R.id.nav_parrainage){ startActivity(new android.content.Intent(this, ClientReferralsActivity.class)); finish(); } else if(id==R.id.nav_logout){ new SessionManager(this).logout(); startActivity(new android.content.Intent(this, LandingActivity.class)); finish(); } }
    private void load(){ progress.setVisibility(View.VISIBLE); empty.setVisibility(View.GONE); Executors.newSingleThreadExecutor().execute(() -> { try { ApiClient api=new ApiClient(this); JSONArray arr=api.getAuthArray("api/Produits"); data.clear(); for(int i=0;i<arr.length();i++) data.add(arr.getJSONObject(i)); runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(data.isEmpty()?View.VISIBLE:View.GONE); filter(search.getText().toString()); }); } catch(Exception e){ runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(View.VISIBLE); }); } }); }
    private void filter(String term){ String t=term==null?"":term.trim().toLowerCase(); int oldSize=display.size(); display.clear(); if(t.isEmpty()){ display.addAll(data); } else { for(JSONObject o: data){ String nom=o.optString("nom","").toLowerCase(); String cat=o.optString("categorie","").toLowerCase(); String desc=o.optString("description","").toLowerCase(); if(nom.contains(t)||cat.contains(t)||desc.contains(t)) display.add(o); } } RecyclerView.Adapter<?> a=list.getAdapter(); if(a!=null){ if(oldSize>0) a.notifyItemRangeRemoved(0, oldSize); int newSize=display.size(); if(newSize>0) a.notifyItemRangeInserted(0, newSize); } }
}
