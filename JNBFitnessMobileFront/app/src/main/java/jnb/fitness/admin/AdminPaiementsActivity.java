package jnb.fitness.admin;
import jnb.fitness.R;
import jnb.fitness.ApiClient;
import jnb.fitness.SessionManager;
import jnb.fitness.LandingActivity;
import jnb.fitness.SimpleTextAdapter;

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

public class AdminPaiementsActivity extends AppCompatActivity {
    private RecyclerView list; private ProgressBar progress; private TextView empty; private EditText search; private final ArrayList<JSONObject> data = new ArrayList<>(); private final ArrayList<JSONObject> display = new ArrayList<>();
    @Override protected void onCreate(Bundle savedInstanceState){ super.onCreate(savedInstanceState); setContentView(R.layout.activity_drawer_admin); android.view.ViewGroup content=findViewById(R.id.content); android.view.LayoutInflater.from(this).inflate(R.layout.activity_admin_paiements, content, true); list=content.findViewById(R.id.list); progress=content.findViewById(R.id.progress); empty=content.findViewById(R.id.empty); search=content.findViewById(R.id.search); list.setLayoutManager(new LinearLayoutManager(this)); list.setAdapter(new AdminPaiementsAdapter(display)); com.google.android.material.appbar.MaterialToolbar toolbar=findViewById(R.id.toolbar); setSupportActionBar(toolbar); androidx.drawerlayout.widget.DrawerLayout drawer=findViewById(R.id.drawer); androidx.appcompat.app.ActionBarDrawerToggle toggle=new androidx.appcompat.app.ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name); drawer.addDrawerListener(toggle); toggle.syncState(); com.google.android.material.navigation.NavigationView nav=findViewById(R.id.nav); nav.setNavigationItemSelectedListener(item->{ drawer.closeDrawers(); route(item.getItemId()); return true;}); search.addTextChangedListener(new android.text.TextWatcher(){ public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int b,int c){ filter(s.toString()); } public void afterTextChanged(android.text.Editable e){} }); load(); }
    private void route(int id){ if(id==R.id.nav_dashboard){ startActivity(new android.content.Intent(this, AdminDashboardActivity.class)); finish(); } else if(id==R.id.nav_paiements){} else if(id==R.id.nav_articles){ startActivity(new android.content.Intent(this, AdminArticlesActivity.class)); finish(); } else if(id==R.id.nav_abonnements){ startActivity(new android.content.Intent(this, AdminAbonnementsActivity.class)); finish(); } else if(id==R.id.nav_cours){ startActivity(new android.content.Intent(this, AdminCoursActivity.class)); finish(); } else if(id==R.id.nav_cartes){ startActivity(new android.content.Intent(this, AdminCartesActivity.class)); finish(); } else if(id==R.id.nav_produits){ startActivity(new android.content.Intent(this, AdminProduitsActivity.class)); finish(); } else if(id==R.id.nav_users){ startActivity(new android.content.Intent(this, AdminUsersActivity.class)); finish(); } else if(id==R.id.nav_logout){ new SessionManager(this).logout(); startActivity(new android.content.Intent(this, LandingActivity.class)); finish(); } }
    private void load(){ progress.setVisibility(View.VISIBLE); empty.setVisibility(View.GONE); Executors.newSingleThreadExecutor().execute(() -> { try { ApiClient api=new ApiClient(this); JSONArray arr=api.getAuthArray("api/Paiements"); data.clear(); for(int i=0;i<arr.length();i++){ JSONObject p=arr.getJSONObject(i); long clientId=p.optLong("clientId"); try{ JSONObject c=api.getAuthJson("api/Clients/"+clientId); p.put("clientNom", c.optString("nom")); p.put("clientPrenom", c.optString("prenom")); } catch(Exception ignored){} data.add(p); } runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(data.isEmpty()?View.VISIBLE:View.GONE); filter(search.getText().toString()); }); } catch(Exception e){ runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(View.VISIBLE); }); } }); }

    private void filter(String term){ String t=term==null?"":term.trim().toLowerCase(); int oldSize=display.size(); display.clear(); if(t.isEmpty()){ display.addAll(data); } else { for(JSONObject o: data){ String nom=(o.optString("clientNom","")+" "+o.optString("clientPrenom"," ")).toLowerCase(); String abo=o.optString("abonnementNom","" ).toLowerCase(); String statut=o.optString("statut","" ).toLowerCase(); if(nom.contains(t) || abo.contains(t) || statut.contains(t)) display.add(o); } } RecyclerView.Adapter<?> a=list.getAdapter(); if(a!=null){ if(oldSize>0) a.notifyItemRangeRemoved(0, oldSize); int newSize=display.size(); if(newSize>0) a.notifyItemRangeInserted(0, newSize); } }
}
