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

public class ClientReferralsActivity extends AppCompatActivity {
    private RecyclerView list; private ProgressBar progress; private TextView empty; private TextView code; private android.widget.Button copy; private TextView rewardLabel; private android.widget.ProgressBar rewardProgress; private final ArrayList<JSONObject> data = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_client);
        android.view.ViewGroup content = findViewById(R.id.content);
        android.view.View page = android.view.LayoutInflater.from(this).inflate(R.layout.activity_client_referrals, content, false);
        content.addView(page);
        list = page.findViewById(R.id.list);
        progress = page.findViewById(R.id.progress);
        empty = page.findViewById(R.id.empty);
        code = page.findViewById(R.id.code);
        copy = page.findViewById(R.id.btn_copy);
        rewardLabel = page.findViewById(R.id.reward_label);
        rewardProgress = page.findViewById(R.id.reward_progress);
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
        list.setAdapter(new ClientReferralsAdapter(data));
        copy.setOnClickListener(v -> copyCode());
        load();
    }
    private void route(int id){ if(id==R.id.nav_dashboard){ startActivity(new android.content.Intent(this, ClientActivity.class)); finish(); } else if(id==R.id.nav_profil){ startActivity(new android.content.Intent(this, ClientProfileActivity.class)); finish(); } else if(id==R.id.nav_abonnements){ startActivity(new android.content.Intent(this, ClientAbonnementsActivity.class)); finish(); } else if(id==R.id.nav_carte){ startActivity(new android.content.Intent(this, ClientCartesActivity.class)); finish(); } else if(id==R.id.nav_coaching){ startActivity(new android.content.Intent(this, ClientCoachingActivity.class)); finish(); } else if(id==R.id.nav_cours){ startActivity(new android.content.Intent(this, ClientCollectiveClassesActivity.class)); finish(); } else if(id==R.id.nav_articles){ startActivity(new android.content.Intent(this, ClientArticlesActivity.class)); finish(); } else if(id==R.id.nav_produits){ startActivity(new android.content.Intent(this, ClientProductsActivity.class)); finish(); } else if(id==R.id.nav_parrainage){} else if(id==R.id.nav_notifications){ startActivity(new android.content.Intent(this, ClientNotificationsActivity.class)); finish(); } else if(id==R.id.nav_logout){ new SessionManager(this).logout(); startActivity(new android.content.Intent(this, LandingActivity.class)); finish(); } }
    private void load(){ progress.setVisibility(View.VISIBLE); empty.setVisibility(View.GONE); Executors.newSingleThreadExecutor().execute(() -> { try { ApiClient api=new ApiClient(this); long clientId=new SessionManager(this).getUserId(); String codeText=api.getAuthText("api/Parrainages/client/"+clientId+"/code"); String countText=api.getAuthText("api/Parrainages/parrain/"+clientId+"/count"); int countVal=0; try{ countVal=Integer.parseInt(countText.trim()); }catch(Exception ignored){} JSONArray arr=api.getAuthArray("api/Parrainages/parrain/"+clientId); int oldSize=data.size(); data.clear(); for(int i=0;i<arr.length();i++) data.add(arr.getJSONObject(i)); final int pv=countVal; final String cc=codeText; runOnUiThread(() -> { code.setText(cc!=null? cc: ""); rewardProgress.setMax(5); rewardProgress.setProgress(Math.max(0, Math.min(5, pv))); rewardLabel.setText("Progression " + pv + "/5"); progress.setVisibility(View.GONE); empty.setVisibility(data.isEmpty()?View.VISIBLE:View.GONE); RecyclerView.Adapter<?> a=list.getAdapter(); if(a!=null){ if(oldSize>0) a.notifyItemRangeRemoved(0, oldSize); int newSize=data.size(); if(newSize>0) a.notifyItemRangeInserted(0, newSize); } }); } catch(Exception e){ runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(View.VISIBLE); }); } }); }
    private void copyCode(){ try{ String c=code.getText().toString(); android.content.ClipboardManager cm=(android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE); cm.setPrimaryClip(android.content.ClipData.newPlainText("code", c)); android.widget.Toast.makeText(this, "Code copié", android.widget.Toast.LENGTH_SHORT).show(); } catch(Exception ignored){} }
}
