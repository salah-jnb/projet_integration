package jnb.fitness.client;
import jnb.fitness.R;
import jnb.fitness.ApiClient;
import jnb.fitness.SessionManager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.Executors;

public class ClientDashboardActivity extends AppCompatActivity {
    private TextView seances; private TextView coachingConfirmes; private TextView parrainages; private View progress; private TextView error;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_client);
        android.view.ViewGroup content = findViewById(R.id.content);
        android.view.LayoutInflater.from(this).inflate(R.layout.activity_client_dashboard, content, true);
        seances = content.findViewById(R.id.metric_seances);
        coachingConfirmes = content.findViewById(R.id.metric_coaching_confirmes);
        parrainages = content.findViewById(R.id.metric_parrainages);
        progress = content.findViewById(R.id.progress);
        error = content.findViewById(R.id.error);
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        androidx.drawerlayout.widget.DrawerLayout drawer = findViewById(R.id.drawer);
        androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        com.google.android.material.navigation.NavigationView nav = findViewById(R.id.nav);
        nav.setNavigationItemSelectedListener(item -> { drawer.closeDrawers(); route(item.getItemId()); return true; });
        jnb.fitness.NavigationHeaderUtil.applyForClient(this);
        load();
    }

    private void route(int id){
        if(id==R.id.nav_dashboard){}
        else if(id==R.id.nav_profil){ startActivity(new android.content.Intent(this, ClientProfileActivity.class)); finish(); }
        else if(id==R.id.nav_articles){ startActivity(new android.content.Intent(this, ClientArticlesActivity.class)); finish(); }
        else if(id==R.id.nav_abonnements){ startActivity(new android.content.Intent(this, ClientAbonnementsActivity.class)); finish(); }
        else if(id==R.id.nav_carte){ startActivity(new android.content.Intent(this, ClientCartesActivity.class)); finish(); }
        else if(id==R.id.nav_coaching){ startActivity(new android.content.Intent(this, ClientCoachingActivity.class)); finish(); }
        else if(id==R.id.nav_cours){ startActivity(new android.content.Intent(this, ClientCollectiveClassesActivity.class)); finish(); }
        else if(id==R.id.nav_produits){ startActivity(new android.content.Intent(this, ClientProductsActivity.class)); finish(); }
        else if(id==R.id.nav_parrainage){ startActivity(new android.content.Intent(this, ClientReferralsActivity.class)); finish(); }
        else if(id==R.id.nav_notifications){ startActivity(new android.content.Intent(this, ClientNotificationsActivity.class)); finish(); }
        else if(id==R.id.nav_logout){ new SessionManager(this).logout(); finish(); }
    }

    private void load(){
        progress.setVisibility(View.VISIBLE);
        error.setVisibility(View.GONE);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ApiClient api = new ApiClient(this);
                long clientId = new SessionManager(this).getUserId();

                JSONArray reservationsCours = api.getAuthArray("api/ReservationsCours/client/"+clientId);
                JSONArray reservationsCoaching = api.getAuthArray("api/ReservationsCoaching/client/"+clientId);
                String parrainagesCountText = api.getAuthText("api/Parrainages/parrain/"+clientId+"/count");

                int upcomingSeances = 0;
                int upcomingCoachingConfirmed = 0;

                long now = System.currentTimeMillis();
                for(int i=0;i<reservationsCours.length();i++){
                    JSONObject r = reservationsCours.getJSONObject(i);
                    String statut = r.optString("statut", "");
                    String dateStr = r.optString("dateSeance", "");
                    long ts = parseIsoToMillis(dateStr);
                    if(ts > now && ("EN_ATTENTE".equalsIgnoreCase(statut) || "CONFIRMEE".equalsIgnoreCase(statut))){
                        upcomingSeances++;
                    }
                }

                for(int i=0;i<reservationsCoaching.length();i++){
                    JSONObject r = reservationsCoaching.getJSONObject(i);
                    String statut = r.optString("statut", "");
                    String dateStr = r.optString("dateSeance", "");
                    long ts = parseIsoToMillis(dateStr);
                    if(ts > now && "CONFIRMEE".equalsIgnoreCase(statut)){
                        upcomingCoachingConfirmed++;
                    }
                }

                int parrainagesValides = 0;
                try { parrainagesValides = Integer.parseInt(parrainagesCountText.trim()); } catch(Exception ignored){}

                final int fUpcomingSeances = upcomingSeances;
                final int fUpcomingCoachingConfirmed = upcomingCoachingConfirmed;
                final int fParrainagesValides = parrainagesValides;

                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    seances.setText(String.valueOf(fUpcomingSeances));
                    coachingConfirmes.setText(String.valueOf(fUpcomingCoachingConfirmed));
                    parrainages.setText(String.valueOf(fParrainagesValides));
                });
            } catch(Exception e){
                runOnUiThread(() -> { progress.setVisibility(View.GONE); error.setText("Erreur de chargement"); error.setVisibility(View.VISIBLE); });
            }
        });
    }

    private long parseIsoToMillis(String iso){
        try{
            if(iso==null||iso.isEmpty()) return 0;
            java.text.SimpleDateFormat sdfUtc = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault());
            sdfUtc.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Date d = sdfUtc.parse(iso);
            return d!=null ? d.getTime() : 0;
        } catch(Exception e){
            try{
                java.text.SimpleDateFormat sdfLocal = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
                sdfLocal.setTimeZone(java.util.TimeZone.getDefault());
                String s = iso.replace('T',' ').substring(0, Math.min(19, iso.length()));
                java.util.Date d2 = sdfLocal.parse(s);
                return d2!=null ? d2.getTime() : 0;
            } catch(Exception ignored){ return 0; }
        }
    }
}
