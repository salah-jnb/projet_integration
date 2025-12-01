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

public class ClientAbonnementsActivity extends AppCompatActivity {
    private RecyclerView list;
    private ProgressBar progress;
    private TextView empty;
    private final ArrayList<JSONObject> data = new ArrayList<>();
    private final ArrayList<JSONObject> types = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_client);
        android.view.ViewGroup content = findViewById(R.id.content);
        android.view.View page = android.view.LayoutInflater.from(this).inflate(R.layout.activity_client_abonnements, content, false);
        content.addView(page);
        list = page.findViewById(R.id.list);
        progress = page.findViewById(R.id.progress);
        empty = page.findViewById(R.id.empty);
        android.widget.Button subscribe = page.findViewById(R.id.btn_subscribe);
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
        list.setAdapter(new ClientAbonnementsAdapter(data));
        subscribe.setOnClickListener(v -> openSubscribeDialog());
        load();
        fetchSubscriptionTypes();
    }

    private void route(int id) {
        if (id == R.id.nav_dashboard) {
            startActivity(new android.content.Intent(this, ClientActivity.class));
            finish();
        } else if (id == R.id.nav_profil) {
            startActivity(new android.content.Intent(this, ClientProfileActivity.class));
            finish();
        } else if (id == R.id.nav_abonnements) {
        } else if (id == R.id.nav_carte) {
            startActivity(new android.content.Intent(this, ClientCartesActivity.class));
            finish();
        } else if (id == R.id.nav_coaching) {
            startActivity(new android.content.Intent(this, ClientCoachingActivity.class));
            finish();
        } else if (id == R.id.nav_cours) {
            startActivity(new android.content.Intent(this, ClientCollectiveClassesActivity.class));
            finish();
        } else if (id == R.id.nav_articles) {
            startActivity(new android.content.Intent(this, ClientArticlesActivity.class));
            finish();
        } else if (id == R.id.nav_produits) {
            startActivity(new android.content.Intent(this, ClientProductsActivity.class));
            finish();
        } else if (id == R.id.nav_parrainage) {
            startActivity(new android.content.Intent(this, ClientReferralsActivity.class));
            finish();
        } else if (id == R.id.nav_notifications) {
            startActivity(new android.content.Intent(this, ClientNotificationsActivity.class));
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
                long clientId = new SessionManager(this).getUserId();
                JSONArray arr = api.getAuthArray("api/Abonnements/client/" + clientId + "/actifs");
                int oldSize = data.size();
                data.clear();
                for (int i = 0; i < arr.length(); i++) data.add(arr.getJSONObject(i));
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    empty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                    RecyclerView.Adapter<?> a = list.getAdapter();
                    if (a != null) {
                        if (oldSize > 0) a.notifyItemRangeRemoved(0, oldSize);
                        int newSize = data.size();
                        if (newSize > 0) a.notifyItemRangeInserted(0, newSize);
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
    private void openSubscribeDialog(){ android.widget.LinearLayout container=new android.widget.LinearLayout(this); container.setOrientation(android.widget.LinearLayout.VERTICAL); container.setPadding(32,32,32,0);
        android.widget.TextView tvType=new android.widget.TextView(this); tvType.setText("Type d'abonnement"); tvType.setTextColor(getResources().getColor(R.color.white));
        android.widget.Spinner spTypes=new android.widget.Spinner(this);
        java.util.List<String> labels=new java.util.ArrayList<>(); for(org.json.JSONObject t: types){ String nom=t.optString("nom"); int prix=t.optInt("prix",0); labels.add(nom+" - "+prix+" TND"); }
        android.widget.ArrayAdapter<String> adapter=new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels);
        spTypes.setAdapter(adapter);
        final int[] selectedIndex={0}; spTypes.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){ public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id){ selectedIndex[0]=position; } public void onNothingSelected(android.widget.AdapterView<?> parent){} });
        android.widget.TextView tvPay=new android.widget.TextView(this); tvPay.setText("Méthode paiement"); tvPay.setTextColor(getResources().getColor(R.color.white));
        android.widget.RadioGroup rg=new android.widget.RadioGroup(this); rg.setOrientation(android.widget.RadioGroup.VERTICAL);
        android.widget.RadioButton rbCarte=new android.widget.RadioButton(this); rbCarte.setText("Carte bancaire"); rbCarte.setTag("CARTE_BANCAIRE"); rg.addView(rbCarte);
        android.widget.RadioButton rbEspeces=new android.widget.RadioButton(this); rbEspeces.setText("Espèces"); rbEspeces.setTag("ESPECES"); rg.addView(rbEspeces);
        android.widget.RadioButton rbCheque=new android.widget.RadioButton(this); rbCheque.setText("Chèque"); rbCheque.setTag("CHEQUE"); rg.addView(rbCheque);
        rbEspeces.setChecked(true);
        container.addView(tvType); container.addView(spTypes); container.addView(tvPay); container.addView(rg);
        new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Souscrire").setView(container)
                .setNegativeButton("Annuler", (d,w)-> d.dismiss())
                .setPositiveButton("Valider", (d,w)-> {
                    if(types.isEmpty()){ android.widget.Toast.makeText(this, "Types non chargés", android.widget.Toast.LENGTH_SHORT).show(); return; }
                    org.json.JSONObject sel=types.get(Math.max(0, selectedIndex[0])); int typeId=sel.optInt("id",0); int montant=sel.optInt("prix",0);
            int checkedId=rg.getCheckedRadioButtonId(); String methode="ESPECES"; if(checkedId!=-1){ android.widget.RadioButton rb=rg.findViewById(checkedId); Object tag=rb.getTag(); if(tag!=null) methode=String.valueOf(tag); }
                    if(typeId>0 && montant>0){ performSubscribe(typeId, montant, methode); }
                }).show(); }

    private void fetchSubscriptionTypes(){ Executors.newSingleThreadExecutor().execute(() -> { try{ ApiClient api=new ApiClient(this); org.json.JSONArray arr=api.getAuthArray("api/Abonnements/types"); types.clear(); for(int i=0;i<arr.length();i++) types.add(arr.getJSONObject(i)); } catch(Exception ignored){} }); }
    private int parseInt(String s){ try{ return Integer.parseInt(s.trim()); } catch(Exception e){ return 0; } }
    private void performSubscribe(int typeAbonnementId,int montant,String methode){ Executors.newSingleThreadExecutor().execute(() -> { try { long clientId=new SessionManager(this).getUserId(); ApiClient api=new ApiClient(this);
                if("CARTE_BANCAIRE".equals(methode)){
                    JSONObject card=api.getAuthJson("api/Cartes/utilisateur/"+clientId);
                    long cardId= card!=null? card.optLong("id"):0;
                    if(cardId==0){ runOnUiThread(() -> android.widget.Toast.makeText(this, "Carte introuvable", android.widget.Toast.LENGTH_SHORT).show()); return; }
                    api.postAuthRaw("api/Cartes/"+cardId+"/diminuer", String.valueOf(montant));
                }
                JSONObject ab=api.postAuthJson("api/Abonnements", new JSONObject().put("clientId", clientId).put("typeAbonnementId", typeAbonnementId).put("dateDebut", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).format(new java.util.Date())));
                long abonnementId=ab.optLong("id"); api.postAuthJson("api/Paiements", new JSONObject().put("clientId", clientId).put("abonnementId", abonnementId).put("montant", montant).put("methodePaiement", methode));
                try { JSONObject parr=api.getAuthJson("api/Parrainages/filleul/"+clientId); if(parr!=null && parr.optLong("id")>0 && !parr.optBoolean("valide", false)){ api.putAuthJson("api/Parrainages/"+parr.optLong("id")+"/valider", new JSONObject()); } } catch(Exception ignored){}
                if("CARTE_BANCAIRE".equals(methode)){
                    try { JSONObject adminCard=api.getAuthJson("api/Cartes/utilisateur/1"); long adminCardId=adminCard!=null? adminCard.optLong("id") : 0; if(adminCardId>0){ api.postAuthRaw("api/Cartes/"+adminCardId+"/recharger", String.valueOf(montant)); } } catch(Exception ignored){}
                }
                runOnUiThread(() -> { android.widget.Toast.makeText(this, "Souscription enregistrée", android.widget.Toast.LENGTH_SHORT).show(); load(); });
            } catch(Exception e){ runOnUiThread(() -> android.widget.Toast.makeText(this, "Échec souscription", android.widget.Toast.LENGTH_SHORT).show()); } }); }
}
