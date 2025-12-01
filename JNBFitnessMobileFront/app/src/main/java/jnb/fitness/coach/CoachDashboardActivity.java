package jnb.fitness.coach;
import jnb.fitness.R;
import jnb.fitness.ApiClient;
import jnb.fitness.SessionManager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

public class CoachDashboardActivity extends AppCompatActivity {
    private TextView note; private TextView articles; private TextView seances; private TextView reservations; private android.view.View progress; private TextView error;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_coach);
        android.view.ViewGroup content = findViewById(R.id.content);
        android.view.LayoutInflater.from(this).inflate(R.layout.activity_coach_dashboard, content, true);
        note = content.findViewById(R.id.metric_note);
        articles = content.findViewById(R.id.metric_articles);
        seances = content.findViewById(R.id.metric_seances);
        reservations = content.findViewById(R.id.metric_reservations);
        progress = content.findViewById(R.id.progress);
        error = content.findViewById(R.id.error);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        androidx.drawerlayout.widget.DrawerLayout drawer = findViewById(R.id.drawer);
        androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        com.google.android.material.navigation.NavigationView nav = findViewById(R.id.nav);
        nav.setNavigationItemSelectedListener(item -> { drawer.closeDrawers(); route(item.getItemId()); return true; });
        jnb.fitness.NavigationHeaderUtil.applyForCoach(this);
        load();
    }

    private void route(int id){
        if(id==R.id.nav_dashboard){}
        else if(id==R.id.nav_profil){ startActivity(new android.content.Intent(this, CoachProfileActivity.class)); finish(); }
        else if(id==R.id.nav_reservations){ startActivity(new android.content.Intent(this, CoachReservationsActivity.class)); finish(); }
        else if(id==R.id.nav_disponibilites){ startActivity(new android.content.Intent(this, CoachDisponibilitesActivity.class)); finish(); }
        else if(id==R.id.nav_cours){ startActivity(new android.content.Intent(this, CoachCollectiveClassesActivity.class)); finish(); }
        else if(id==R.id.nav_articles){ startActivity(new android.content.Intent(this, CoachArticlesActivity.class)); finish(); }
        else if(id==R.id.nav_produits){ startActivity(new android.content.Intent(this, CoachProductsActivity.class)); finish(); }
        else if(id==R.id.nav_notifications){ startActivity(new android.content.Intent(this, CoachNotificationsActivity.class)); finish(); }
        else if(id==R.id.nav_logout){ new SessionManager(this).logout(); finish(); }
    }

    private void load(){
        progress.setVisibility(View.VISIBLE);
        error.setVisibility(View.GONE);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ApiClient api = new ApiClient(this);
                long coachId = new SessionManager(this).getUserId();

                JSONObject coachObj = api.getAuthJson("api/Coachs/"+coachId);
                JSONArray seancesArr = api.getAuthArray("api/CoursCollectifs/coach/"+coachId+"/seances/disponibles");
                JSONArray reservationsArr = api.getAuthArray("api/ReservationsCoaching/coach/"+coachId);
                JSONArray articlesArr = api.getAuthArray("api/Articles/coach/"+coachId);

                double noteGlobale = coachObj.optDouble("noteGlobale", 0.0);
                int articlesCount = articlesArr.length();
                int seancesCount = seancesArr.length();
                int confirmedCount = 0;
                for(int i=0;i<reservationsArr.length();i++){
                    JSONObject r = reservationsArr.getJSONObject(i);
                    String st = r.optString("statut");
                    if("CONFIRMEE".equalsIgnoreCase(st) || "TERMINEE".equalsIgnoreCase(st)){
                        confirmedCount++;
                    }
                }

                double finalNoteGlobale = noteGlobale;
                int finalArticlesCount = articlesCount;
                int finalSeancesCount = seancesCount;
                int finalConfirmedCount = confirmedCount;

                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    note.setText(String.format(java.util.Locale.getDefault(), "%.1f", finalNoteGlobale));
                    articles.setText(String.valueOf(finalArticlesCount));
                    seances.setText(String.valueOf(finalSeancesCount));
                    reservations.setText(String.valueOf(finalConfirmedCount));
                });
            } catch(Exception e){
                runOnUiThread(() -> { progress.setVisibility(View.GONE); error.setText("Erreur de chargement"); error.setVisibility(View.VISIBLE); });
            }
        });
    }
}
