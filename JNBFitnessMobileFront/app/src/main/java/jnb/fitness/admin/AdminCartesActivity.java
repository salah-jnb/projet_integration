package jnb.fitness.admin;
import jnb.fitness.R;
import jnb.fitness.ApiClient;
import jnb.fitness.SessionManager;
import jnb.fitness.LandingActivity;
import jnb.fitness.client.ClientCartesAdapter;

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

public class AdminCartesActivity extends AppCompatActivity implements ClientCartesAdapter.Callbacks {
    private RecyclerView list; private ProgressBar progress; private TextView empty; private final ArrayList<JSONObject> data = new ArrayList<>(); private final ArrayList<JSONObject> all = new ArrayList<>(); private android.widget.EditText search;
    @Override protected void onCreate(Bundle savedInstanceState){ super.onCreate(savedInstanceState); setContentView(R.layout.activity_drawer_admin); android.view.ViewGroup content=findViewById(R.id.content); android.view.LayoutInflater.from(this).inflate(R.layout.activity_admin_cartes, content, true); list=content.findViewById(R.id.list); progress=content.findViewById(R.id.progress); empty=content.findViewById(R.id.empty); search=content.findViewById(R.id.search); list.setLayoutManager(new LinearLayoutManager(this)); list.setAdapter(new ClientCartesAdapter(data, this)); com.google.android.material.appbar.MaterialToolbar toolbar=findViewById(R.id.toolbar); setSupportActionBar(toolbar); androidx.drawerlayout.widget.DrawerLayout drawer=findViewById(R.id.drawer); androidx.appcompat.app.ActionBarDrawerToggle toggle=new androidx.appcompat.app.ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name); drawer.addDrawerListener(toggle); toggle.syncState(); com.google.android.material.navigation.NavigationView nav=findViewById(R.id.nav); nav.setNavigationItemSelectedListener(item->{ drawer.closeDrawers(); route(item.getItemId()); return true;}); if(search!=null){ search.addTextChangedListener(new android.text.TextWatcher(){ public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int b,int c){ filter(s.toString()); } public void afterTextChanged(android.text.Editable e){} }); } load(); }
    private void route(int id){ if(id==R.id.nav_dashboard){ startActivity(new android.content.Intent(this, AdminDashboardActivity.class)); finish(); } else if(id==R.id.nav_cartes){} else if(id==R.id.nav_articles){ startActivity(new android.content.Intent(this, AdminArticlesActivity.class)); finish(); } else if(id==R.id.nav_abonnements){ startActivity(new android.content.Intent(this, AdminAbonnementsActivity.class)); finish(); } else if(id==R.id.nav_cours){ startActivity(new android.content.Intent(this, AdminCoursActivity.class)); finish(); } else if(id==R.id.nav_paiements){ startActivity(new android.content.Intent(this, AdminPaiementsActivity.class)); finish(); } else if(id==R.id.nav_produits){ startActivity(new android.content.Intent(this, AdminProduitsActivity.class)); finish(); } else if(id==R.id.nav_users){ startActivity(new android.content.Intent(this, AdminUsersActivity.class)); finish(); } else if(id==R.id.nav_logout){ new SessionManager(this).logout(); startActivity(new android.content.Intent(this, LandingActivity.class)); finish(); } }
    private void load(){ progress.setVisibility(View.VISIBLE); empty.setVisibility(View.GONE); Executors.newSingleThreadExecutor().execute(() -> { try { ApiClient api=new ApiClient(this); JSONArray arr=api.getAuthArray("api/Cartes"); all.clear(); for(int i=0;i<arr.length();i++) all.add(arr.getJSONObject(i)); runOnUiThread(() -> { progress.setVisibility(View.GONE); filter(search!=null? search.getText().toString(): ""); }); } catch(Exception e){ runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(View.VISIBLE); }); } }); }
    private void filter(String q){ data.clear(); String s=q==null? "": q.trim().toLowerCase(); for(JSONObject o: all){ String lib=o.optString("libelle",""); String num=o.optString("numero", o.optString("numeroCarte","")); String dev=o.optString("devise",""); if(s.isEmpty() || lib.toLowerCase().contains(s) || num.toLowerCase().contains(s) || dev.toLowerCase().contains(s)){ data.add(o); } } RecyclerView.Adapter<?> a=list.getAdapter(); if(a!=null) a.notifyDataSetChanged(); empty.setVisibility(data.isEmpty()? View.VISIBLE: View.GONE); }
    @Override public void onRecharger(long id,int montant){ Executors.newSingleThreadExecutor().execute(() -> { try { new ApiClient(this).postAuthRaw("api/Cartes/"+id+"/recharger", String.valueOf(montant)); load(); } catch(Exception ignored){} }); }
}
