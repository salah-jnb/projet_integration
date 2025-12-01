package jnb.fitness.admin;
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

public class AdminDashboardActivity extends AppCompatActivity {
    private TextView clients; private TextView coachs; private TextView abos; private TextView seances; private TextView reservations; private TextView revenuMoi; private android.view.View progress; private TextView error;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_admin);
        android.view.ViewGroup content = findViewById(R.id.content);
        android.view.LayoutInflater.from(this).inflate(R.layout.activity_admin_dashboard, content, true);
        clients = content.findViewById(R.id.metric_clients);
        coachs = content.findViewById(R.id.metric_coachs);
        abos = content.findViewById(R.id.metric_abos);
        seances = content.findViewById(R.id.metric_seances);
        reservations = content.findViewById(R.id.metric_reservations);
        revenuMoi = content.findViewById(R.id.metric_revenu_moi);
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
        load();
    }

    private void route(int id){
        if(id==R.id.nav_dashboard){}
        else if(id==R.id.nav_users){ startActivity(new android.content.Intent(this, AdminUsersActivity.class)); finish(); }
        else if(id==R.id.nav_abonnements){ startActivity(new android.content.Intent(this, AdminAbonnementsActivity.class)); finish(); }
        else if(id==R.id.nav_cours){ startActivity(new android.content.Intent(this, AdminCoursActivity.class)); finish(); }
        else if(id==R.id.nav_articles){ startActivity(new android.content.Intent(this, AdminArticlesActivity.class)); finish(); }
        else if(id==R.id.nav_produits){ startActivity(new android.content.Intent(this, AdminProduitsActivity.class)); finish(); }
        else if(id==R.id.nav_paiements){ startActivity(new android.content.Intent(this, AdminPaiementsActivity.class)); finish(); }
        else if(id==R.id.nav_cartes){ startActivity(new android.content.Intent(this, AdminCartesActivity.class)); finish(); }
        else if(id==R.id.nav_logout){ new SessionManager(this).logout(); finish(); }
    }

    private void load(){
        progress.setVisibility(View.VISIBLE);
        error.setVisibility(View.GONE);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ApiClient api = new ApiClient(this);
                JSONArray clientsArr = api.getAuthArray("api/Clients");
                JSONArray coachsArr = api.getAuthArray("api/Coachs");
                JSONArray abosArr = api.getAuthArray("api/Abonnements/actifs");
                JSONArray seancesArr = api.getAuthArray("api/CoursCollectifs/seances/disponibles");
                JSONArray typesArr = api.getAuthArray("api/Abonnements/types");

                int confirmedCount = 0;
                for(int i=0;i<coachsArr.length();i++){
                    long cid = coachsArr.getJSONObject(i).optLong("utilisateurId");
                    try{
                        JSONArray rarr = api.getAuthArray("api/ReservationsCoaching/coach/"+cid);
                        for(int j=0;j<rarr.length();j++){
                            String st = rarr.getJSONObject(j).optString("statut");
                            if("CONFIRMEE".equalsIgnoreCase(st)) confirmedCount++;
                        }
                    } catch(Exception ignored){}
                }

                int finalClients = clientsArr.length();
                int finalCoachs = coachsArr.length();
                int finalAbos = abosArr.length();
                int finalSeances = seancesArr.length();
                int finalConfirmed = confirmedCount;

                double adminRevenue = 0.0;
                try {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    int currentMonth = cal.get(java.util.Calendar.MONTH) + 1;
                    int currentYear = cal.get(java.util.Calendar.YEAR);

                    java.util.HashMap<Long, Double> typePrix = new java.util.HashMap<>();
                    for(int i=0;i<typesArr.length();i++){
                        JSONObject t = typesArr.getJSONObject(i);
                        long tid = t.optLong("id");
                        double prix = t.optDouble("prix", 0.0);
                        typePrix.put(tid, prix);
                    }

                    for(int i=0;i<abosArr.length();i++){
                        JSONObject a = abosArr.getJSONObject(i);
                        boolean isOffert = a.optBoolean("offertParParrainage", false);
                        if(isOffert) continue;

                        String dateDebutStr = a.optString("dateDebut", "");
                        int y = -1; int m = -1;
                        try {
                            String isoDate = dateDebutStr.length() >= 10 ? dateDebutStr.substring(0,10) : dateDebutStr;
                            String[] parts = isoDate.split("-");
                            y = Integer.parseInt(parts[0]);
                            m = Integer.parseInt(parts[1]);
                        } catch(Exception ignored){}
                        if(y == currentYear && m == currentMonth){
                            long tid = a.optLong("typeAbonnementId");
                            Double prix = typePrix.get(tid);
                            if(prix != null) adminRevenue += prix;
                        }
                    }
                } catch(Exception ignored){}
                final double finalAdminRevenue = adminRevenue;

                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    clients.setText(String.valueOf(finalClients));
                    coachs.setText(String.valueOf(finalCoachs));
                    abos.setText(String.valueOf(finalAbos));
                    seances.setText(String.valueOf(finalSeances));
                    reservations.setText(String.valueOf(finalConfirmed));
                    revenuMoi.setText(String.format(java.util.Locale.getDefault(), "%.2f TND", finalAdminRevenue));
                });
            } catch(Exception e){
                runOnUiThread(() -> { progress.setVisibility(View.GONE); error.setText("Erreur de chargement"); error.setVisibility(View.VISIBLE); });
            }
        });
    }
}
