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

public class ClientNotificationsActivity extends AppCompatActivity {
    private RecyclerView list; private ProgressBar progress; private TextView empty; private final ArrayList<JSONObject> data = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_client);
        android.view.ViewGroup content = findViewById(R.id.content);
        android.view.View page = android.view.LayoutInflater.from(this).inflate(R.layout.activity_client_notifications, content, false);
        content.addView(page);
        list = page.findViewById(R.id.list);
        progress = page.findViewById(R.id.progress);
        empty = page.findViewById(R.id.empty);
        com.google.android.material.button.MaterialButton btnAll = page.findViewById(R.id.btn_mark_all);
        TextView unreadCount = page.findViewById(R.id.unread_count);
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
        list.setAdapter(new ClientNotificationsAdapter(data, new ClientNotificationsAdapter.Callbacks(){ @Override public void onClick(long id){ markAsRead(id); } @Override public void onMarkRead(long id){ markAsRead(id);} }));
        if(btnAll!=null){ btnAll.setOnClickListener(v -> markAllAsRead()); }
        this.unreadLabel = unreadCount;
        load();
    }
    private void route(int id){ if(id==R.id.nav_dashboard){ startActivity(new android.content.Intent(this, ClientActivity.class)); finish(); } else if(id==R.id.nav_profil){ startActivity(new android.content.Intent(this, ClientProfileActivity.class)); finish(); } else if(id==R.id.nav_notifications){} else if(id==R.id.nav_abonnements){ startActivity(new android.content.Intent(this, ClientAbonnementsActivity.class)); finish(); } else if(id==R.id.nav_carte){ startActivity(new android.content.Intent(this, ClientCartesActivity.class)); finish(); } else if(id==R.id.nav_coaching){ startActivity(new android.content.Intent(this, ClientCoachingActivity.class)); finish(); } else if(id==R.id.nav_cours){ startActivity(new android.content.Intent(this, ClientCollectiveClassesActivity.class)); finish(); } else if(id==R.id.nav_articles){ startActivity(new android.content.Intent(this, ClientArticlesActivity.class)); finish(); } else if(id==R.id.nav_produits){ startActivity(new android.content.Intent(this, ClientProductsActivity.class)); finish(); } else if(id==R.id.nav_parrainage){ startActivity(new android.content.Intent(this, ClientReferralsActivity.class)); finish(); } else if(id==R.id.nav_logout){ new SessionManager(this).logout(); startActivity(new android.content.Intent(this, LandingActivity.class)); finish(); } }
    private void load(){ progress.setVisibility(View.VISIBLE); empty.setVisibility(View.GONE); Executors.newSingleThreadExecutor().execute(() -> { try { ApiClient api=new ApiClient(this); long userId=new SessionManager(this).getUserId(); JSONArray arr=api.getAuthArray("api/Notifications/utilisateur/"+userId+"/non-lues"); data.clear(); for(int i=0;i<arr.length();i++) data.add(arr.getJSONObject(i)); runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(data.isEmpty()?View.VISIBLE:View.GONE); if(unreadLabel!=null){ unreadLabel.setText(data.isEmpty()? "Aucune notification": (data.size()+" non lue(s)")); } RecyclerView.Adapter a=list.getAdapter(); if(a!=null){ a.notifyDataSetChanged(); } }); } catch(Exception e){ runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(View.VISIBLE); }); } }); }
    private TextView unreadLabel;
    private void restart(){ finish(); startActivity(getIntent()); overridePendingTransition(0,0); }
    private void markAllAsRead(){ Executors.newSingleThreadExecutor().execute(() -> { try { for(org.json.JSONObject n: data){ long nid=n.optLong("id"); new ApiClient(this).putAuthJson("api/Notifications/"+nid+"/lire", new org.json.JSONObject()); } runOnUiThread(this::restart); } catch(Exception ignored){} }); }
    private void markAsRead(long id){ Executors.newSingleThreadExecutor().execute(() -> { try { new ApiClient(this).putAuthJson("api/Notifications/"+id+"/lire", new org.json.JSONObject()); runOnUiThread(this::restart); } catch(Exception ignored){} }); }
}
