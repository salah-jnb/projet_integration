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

public class CoachReservationsActivity extends AppCompatActivity implements CoachReservationsAdapter.Callbacks {
    private RecyclerView list;
    private ProgressBar progress;
    private View empty;
    private final ArrayList<JSONObject> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_coach);
        android.view.ViewGroup content=findViewById(R.id.content);
        android.view.View page = android.view.LayoutInflater.from(this).inflate(R.layout.activity_coach_reservations, content, false);
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
        list.setAdapter(new CoachReservationsAdapter(data, this));
        load();
    }

    private void route(int id) {
        if (id == R.id.nav_dashboard) {
            startActivity(new android.content.Intent(this, CoachActivity.class));
            finish();
        } else if (id == R.id.nav_profil) {
            startActivity(new android.content.Intent(this, CoachProfileActivity.class));
            finish();
        } else if (id == R.id.nav_disponibilites) {
            startActivity(new android.content.Intent(this, CoachDisponibilitesActivity.class));
            finish();
        } else if (id == R.id.nav_reservations) {
        } else if (id == R.id.nav_cours) {
            startActivity(new android.content.Intent(this, CoachCollectiveClassesActivity.class));
            finish();
        } else if (id == R.id.nav_articles) {
            startActivity(new android.content.Intent(this, CoachArticlesActivity.class));
            finish();
        } else if (id == R.id.nav_produits) {
            startActivity(new android.content.Intent(this, CoachProductsActivity.class));
            finish();
        } else if (id == R.id.nav_notifications) {
            startActivity(new android.content.Intent(this, CoachNotificationsActivity.class));
            finish();
        } else if (id == R.id.nav_logout) {
            new SessionManager(this).logout();
            startActivity(new android.content.Intent(this, LandingActivity.class));
            finish();
        }
    }

    private void load() {
        progress.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ApiClient api = new ApiClient(this);
                long coachId = new SessionManager(this).getUserId();
                JSONArray arr = api.getAuthArray("api/ReservationsCoaching/coach/" + coachId);
                data.clear();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    String st = o.optString("statut", "");
                    if ("EN_ATTENTE".equalsIgnoreCase(st) || "CONFIRMEE".equalsIgnoreCase(st)) {
                        long clientId = o.optLong("clientId");
                        try {
                            JSONObject client = api.getAuthJson("api/Utilisateurs/" + clientId);
                            if (client != null) {
                                o.put("clientNom", client.optString("nom", ""));
                                o.put("clientPrenom", client.optString("prenom", ""));
                            }
                        } catch (Exception ignored) {}
                        data.add(o);
                    }
                }
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    empty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                    RecyclerView.Adapter<?> a = list.getAdapter();
                    if (a != null) {
                        a.notifyDataSetChanged();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    empty.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    @Override
    public void onCancel(long id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try { new ApiClient(this).deleteAuth("api/ReservationsCoaching/" + id); runOnUiThread(this::restart); } catch (Exception ignored) {}
        });
    }

    @Override
    public void onComplete(long id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try { new ApiClient(this).putAuthJson("api/ReservationsCoaching/" + id + "/complete", new JSONObject()); runOnUiThread(this::restart); } catch (Exception ignored) {}
        });
    }

    @Override
    public void onConfirm(long id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try { new ApiClient(this).putAuthJson("api/ReservationsCoaching/" + id + "/confirmer", new JSONObject()); runOnUiThread(this::restart); } catch (Exception ignored) {}
        });
    }

    private void restart(){ finish(); startActivity(getIntent()); overridePendingTransition(0,0); }
}
