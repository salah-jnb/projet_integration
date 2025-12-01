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

public class CoachNotificationsActivity extends AppCompatActivity implements CoachNotificationsAdapter.Callbacks {
    private RecyclerView list; private ProgressBar progress; private View empty; private final ArrayList<JSONObject> data = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_coach);
        android.view.ViewGroup content = findViewById(R.id.content);
        android.view.View page = android.view.LayoutInflater.from(this).inflate(R.layout.activity_coach_notifications, content, false);
        content.addView(page);
        list = page.findViewById(R.id.list);
        progress = page.findViewById(R.id.progress);
        empty = page.findViewById(R.id.empty);
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
        list.setAdapter(new CoachNotificationsAdapter(data, this));
        android.widget.Button btnMarkAll = page.findViewById(R.id.btn_mark_all);
        if(btnMarkAll!=null){ btnMarkAll.setOnClickListener(v-> markAllAsRead()); }
        load();
    }
    private void route(int id){ if(id==R.id.nav_dashboard){ startActivity(new android.content.Intent(this, CoachActivity.class)); finish(); } else if(id==R.id.nav_profil){ startActivity(new android.content.Intent(this, CoachProfileActivity.class)); finish(); } else if(id==R.id.nav_disponibilites){ startActivity(new android.content.Intent(this, CoachDisponibilitesActivity.class)); finish(); } else if(id==R.id.nav_reservations){ startActivity(new android.content.Intent(this, CoachReservationsActivity.class)); finish(); } else if(id==R.id.nav_cours){ startActivity(new android.content.Intent(this, CoachCollectiveClassesActivity.class)); finish(); } else if(id==R.id.nav_articles){ startActivity(new android.content.Intent(this, CoachArticlesActivity.class)); finish(); } else if(id==R.id.nav_produits){ startActivity(new android.content.Intent(this, CoachProductsActivity.class)); finish(); } else if(id==R.id.nav_notifications){} else if(id==R.id.nav_logout){ new SessionManager(this).logout(); startActivity(new android.content.Intent(this, LandingActivity.class)); finish(); } }
    private void load(){
        progress.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                long id=new SessionManager(this).getUserId();
                JSONArray arr=new ApiClient(this).getAuthArray("api/Notifications/utilisateur/"+id+"/non-lues");
                final ArrayList<JSONObject> tmp=new ArrayList<>();
                for(int i=0;i<arr.length();i++){ tmp.add(arr.getJSONObject(i)); }
                runOnUiThread(() -> {
                    data.clear();
                    data.addAll(tmp);
                    progress.setVisibility(View.GONE);
                    empty.setVisibility(data.isEmpty()?View.VISIBLE:View.GONE);
                    RecyclerView.Adapter<?> a=list.getAdapter();
                    if(a!=null){ a.notifyDataSetChanged(); }
                });
            } catch(Exception e){
                runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(View.VISIBLE); });
            }
        });
    }
    private void restart(){ finish(); startActivity(getIntent()); overridePendingTransition(0,0); }
    private void markAllAsRead(){ Executors.newSingleThreadExecutor().execute(() -> { try { for(JSONObject n: data){ long nid=n.optLong("id"); new ApiClient(this).putAuthJson("api/Notifications/"+nid+"/lire", new JSONObject()); } runOnUiThread(this::restart); } catch(Exception ignored){} }); }
    @Override public void onMarkRead(long id){ Executors.newSingleThreadExecutor().execute(() -> { try { new ApiClient(this).putAuthJson("api/Notifications/"+id+"/lire", new JSONObject()); runOnUiThread(this::restart);} catch(Exception ignored){} }); }
}
