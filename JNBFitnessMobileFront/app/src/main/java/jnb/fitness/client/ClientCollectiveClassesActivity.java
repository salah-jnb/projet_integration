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
import java.util.HashMap;
import java.util.concurrent.Executors;

public class ClientCollectiveClassesActivity extends AppCompatActivity implements ClientSeancesAdapter.Callbacks {
    private RecyclerView list; private ProgressBar progress; private TextView empty; private final ArrayList<JSONObject> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_client);
        android.view.ViewGroup content = findViewById(R.id.content);
        android.view.View page = android.view.LayoutInflater.from(this).inflate(R.layout.activity_client_collective_classes, content, false);
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
        list.setAdapter(new ClientSeancesAdapter(data, this));
        load();
    }

    private void route(int id){ if(id==R.id.nav_dashboard){ startActivity(new android.content.Intent(this, ClientActivity.class)); finish(); } else if(id==R.id.nav_profil){ startActivity(new android.content.Intent(this, ClientProfileActivity.class)); finish(); } else if(id==R.id.nav_abonnements){ startActivity(new android.content.Intent(this, ClientAbonnementsActivity.class)); finish(); } else if(id==R.id.nav_carte){ startActivity(new android.content.Intent(this, ClientCartesActivity.class)); finish(); } else if(id==R.id.nav_cours){} else if(id==R.id.nav_coaching){ startActivity(new android.content.Intent(this, ClientCoachingActivity.class)); finish(); } else if(id==R.id.nav_articles){ startActivity(new android.content.Intent(this, ClientArticlesActivity.class)); finish(); } else if(id==R.id.nav_produits){ startActivity(new android.content.Intent(this, ClientProductsActivity.class)); finish(); } else if(id==R.id.nav_parrainage){ startActivity(new android.content.Intent(this, ClientReferralsActivity.class)); finish(); } else if(id==R.id.nav_notifications){ startActivity(new android.content.Intent(this, ClientNotificationsActivity.class)); finish(); } else if(id==R.id.nav_logout){ new SessionManager(this).logout(); startActivity(new android.content.Intent(this, LandingActivity.class)); finish(); } }

    private void load(){ progress.setVisibility(View.VISIBLE); empty.setVisibility(View.GONE); Executors.newSingleThreadExecutor().execute(() -> { try { ApiClient api=new ApiClient(this); long clientId=new SessionManager(this).getUserId(); JSONArray seances=api.getAuthArray("api/CoursCollectifs/seances/disponibles"); JSONArray myRes=api.getAuthArray("api/ReservationsCours/client/"+clientId); HashMap<Long, JSONObject> bySeance=new HashMap<>(); for(int i=0;i<myRes.length();i++){ JSONObject r=myRes.getJSONObject(i); if(!"ANNULEE".equals(r.optString("statut"))) bySeance.put(r.optLong("seanceCoursCollectifId"), r); } int oldSize=data.size(); data.clear(); for(int i=0;i<seances.length();i++){ JSONObject s=seances.getJSONObject(i); JSONObject r=bySeance.get(s.optLong("id")); if(r!=null){ s.put("reserved", true); s.put("reservationId", r.optLong("id")); s.put("statut", r.optString("statut")); } else { s.put("reserved", false); } data.add(s); } runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(data.isEmpty()?View.VISIBLE:View.GONE); RecyclerView.Adapter<?> a=list.getAdapter(); if(a!=null){ if(oldSize>0) a.notifyItemRangeRemoved(0, oldSize); int newSize=data.size(); if(newSize>0) a.notifyItemRangeInserted(0, newSize); } }); } catch(Exception e){ runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(View.VISIBLE); }); } }); }

    @Override public void onReserve(long seanceId){ Executors.newSingleThreadExecutor().execute(() -> { try { long clientId=new SessionManager(this).getUserId(); JSONObject body=new JSONObject().put("clientId", clientId).put("seanceCoursCollectifId", seanceId).put("delaiAnnulationHeures", 24); new ApiClient(this).postAuthJson("api/ReservationsCours", body); load(); } catch(Exception ignored){} }); }
    @Override public void onCancel(long reservationId){ Executors.newSingleThreadExecutor().execute(() -> { try { new ApiClient(this).deleteAuth("api/ReservationsCours/"+reservationId); load(); } catch(Exception ignored){} }); }
}
