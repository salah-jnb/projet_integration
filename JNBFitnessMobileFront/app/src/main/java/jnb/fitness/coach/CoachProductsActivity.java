package jnb.fitness.coach;
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

public class CoachProductsActivity extends AppCompatActivity implements CoachProductsAdapter.Callbacks {
    private RecyclerView list; private ProgressBar progress; private View empty; private final ArrayList<JSONObject> data = new ArrayList<>(); private final ArrayList<JSONObject> all = new ArrayList<>(); private android.widget.EditText search;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_coach);
        android.view.ViewGroup content = findViewById(R.id.content);
        android.view.View page = android.view.LayoutInflater.from(this).inflate(R.layout.activity_coach_products, content, false);
        content.addView(page);
        list = page.findViewById(R.id.list);
        progress = page.findViewById(R.id.progress);
        empty = page.findViewById(R.id.empty);
        search = page.findViewById(R.id.search_input);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        androidx.drawerlayout.widget.DrawerLayout drawer = findViewById(R.id.drawer);
        androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        com.google.android.material.navigation.NavigationView nav = findViewById(R.id.nav);
        nav.setNavigationItemSelectedListener(item -> { drawer.closeDrawers(); route(item.getItemId()); return true; });
        jnb.fitness.NavigationHeaderUtil.applyForCoach(this);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(new CoachProductsAdapter(data, true, null));
        android.widget.Button btnCreate = page.findViewById(R.id.btn_create);
        if(btnCreate!=null){ btnCreate.setVisibility(View.GONE); }
        if(search!=null){ search.addTextChangedListener(new android.text.TextWatcher(){ public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int b,int c){ applyFilter(String.valueOf(s)); } public void afterTextChanged(android.text.Editable s){} }); }
        load();
    }
    private void route(int id){ if(id==R.id.nav_dashboard){ startActivity(new android.content.Intent(this, CoachActivity.class)); finish(); } else if(id==R.id.nav_profil){ startActivity(new android.content.Intent(this, CoachProfileActivity.class)); finish(); } else if(id==R.id.nav_disponibilites){ startActivity(new android.content.Intent(this, CoachDisponibilitesActivity.class)); finish(); } else if(id==R.id.nav_reservations){ startActivity(new android.content.Intent(this, CoachReservationsActivity.class)); finish(); } else if(id==R.id.nav_cours){ startActivity(new android.content.Intent(this, CoachCollectiveClassesActivity.class)); finish(); } else if(id==R.id.nav_articles){ startActivity(new android.content.Intent(this, CoachArticlesActivity.class)); finish(); } else if(id==R.id.nav_produits){} else if(id==R.id.nav_notifications){ startActivity(new android.content.Intent(this, CoachNotificationsActivity.class)); finish(); } else if(id==R.id.nav_logout){ new SessionManager(this).logout(); startActivity(new android.content.Intent(this, LandingActivity.class)); finish(); } }
    private void load(){ progress.setVisibility(View.VISIBLE); empty.setVisibility(View.GONE); Executors.newSingleThreadExecutor().execute(() -> { try { JSONArray arr=new ApiClient(this).getAuthArray("api/Produits"); all.clear(); for(int i=0;i<arr.length();i++){ all.add(arr.getJSONObject(i)); } runOnUiThread(() -> { applyFilter(search!=null? String.valueOf(search.getText()): ""); progress.setVisibility(View.GONE); empty.setVisibility(data.isEmpty()?View.VISIBLE:View.GONE); }); } catch(Exception e){ runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(View.VISIBLE); }); } }); }

    private void applyFilter(String q){ String qq=q==null? "": q.trim().toLowerCase(java.util.Locale.getDefault()); data.clear(); if(qq.isEmpty()){ data.addAll(all); } else { for(JSONObject o: all){ String nom=o.optString("nom",""), cat=o.optString("categorie",""), desc=o.optString("description","" ); String h=(nom+" "+cat+" "+desc).toLowerCase(java.util.Locale.getDefault()); if(h.contains(qq)) data.add(o); } } RecyclerView.Adapter<?> a=list.getAdapter(); if(a!=null) a.notifyDataSetChanged(); }
    @Override public void onDelete(long id){}
    @Override public void onEdit(JSONObject p){}
}
