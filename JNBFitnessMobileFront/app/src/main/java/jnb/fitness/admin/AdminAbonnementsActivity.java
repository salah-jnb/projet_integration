package jnb.fitness.admin;
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

public class AdminAbonnementsActivity extends AppCompatActivity implements AdminAbonnementsAdapter.Callbacks {
    private RecyclerView list;
    private ProgressBar progress;
    private TextView empty;
    private final ArrayList<JSONObject> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_admin);
        android.view.ViewGroup content = findViewById(R.id.content);
        android.view.LayoutInflater.from(this).inflate(R.layout.activity_admin_abonnements, content, true);
        list = content.findViewById(R.id.list);
        progress = content.findViewById(R.id.progress);
        empty = content.findViewById(R.id.empty);
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        androidx.drawerlayout.widget.DrawerLayout drawer = findViewById(R.id.drawer);
        androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        com.google.android.material.navigation.NavigationView nav = findViewById(R.id.nav);
        nav.setNavigationItemSelectedListener(item -> { drawer.closeDrawers(); route(item.getItemId()); return true; });
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(new AdminAbonnementsAdapter(data, this));
        android.view.View fab = content.findViewById(R.id.fab_add);
        android.view.View btnEmpty = content.findViewById(R.id.btn_add_empty);
        if(fab!=null) fab.setOnClickListener(v -> openCreateTypeDialog());
        if(btnEmpty!=null) btnEmpty.setOnClickListener(v -> openCreateTypeDialog());
        load();
    }

    private void route(int id) {
        if (id == R.id.nav_dashboard) {
            startActivity(new android.content.Intent(this, AdminDashboardActivity.class));
            finish();
        } else if (id == R.id.nav_users) {
            startActivity(new android.content.Intent(this, AdminUsersActivity.class));
            finish();
        } else if (id == R.id.nav_abonnements) { }
        else if (id == R.id.nav_cours) {
            startActivity(new android.content.Intent(this, AdminCoursActivity.class));
            finish();
        } else if (id == R.id.nav_articles) {
            startActivity(new android.content.Intent(this, AdminArticlesActivity.class));
            finish();
        } else if (id == R.id.nav_produits) {
            startActivity(new android.content.Intent(this, AdminProduitsActivity.class));
            finish();
        } else if (id == R.id.nav_paiements) {
            startActivity(new android.content.Intent(this, AdminPaiementsActivity.class));
            finish();
        } else if (id == R.id.nav_cartes) {
            startActivity(new android.content.Intent(this, AdminCartesActivity.class));
            finish();
        } else if (id == R.id.nav_settings) {
            startActivity(new android.content.Intent(this, AdminActivity.class));
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
        java.util.concurrent.ExecutorService ex = java.util.concurrent.Executors.newSingleThreadExecutor();
        ex.execute(() -> {
            try {
                ApiClient api = new ApiClient(this);
                JSONArray arr = api.getAuthArray("api/Abonnements/types");
                data.clear();
                for (int i = 0; i < arr.length(); i++) {
                    org.json.JSONObject o = arr.getJSONObject(i);
                    data.add(o);
                }
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    empty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                    RecyclerView.Adapter<?> a = list.getAdapter();
                    if (a != null) a.notifyDataSetChanged();
                    View emptyContainer = findViewById(R.id.emptyContainer);
                    if (emptyContainer != null) emptyContainer.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    empty.setVisibility(View.VISIBLE);
                });
            } finally { ex.shutdown(); }
        });
    }

    @Override
    public void onCancel(long id) { }

    @Override
    public void onEditType(JSONObject o) {
        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(32,32,32,0);
        android.widget.EditText etNom = new android.widget.EditText(this); etNom.setHint("Nom de l'abonnement *"); etNom.setText(o.optString("nom",""));
        final android.widget.Spinner spType = new android.widget.Spinner(this);
        java.util.List<String> types = java.util.Arrays.asList("SALLE","COURS_COLLECTIFS","PACK_COACHING_5","PACK_COACHING_10","PACK_COACHING_20");
        android.widget.ArrayAdapter<String> adType = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spType.setAdapter(adType);
        int idx = types.indexOf(o.optString("type","SALLE")); if(idx>=0) spType.setSelection(idx);
        android.widget.EditText etDesc = new android.widget.EditText(this); etDesc.setHint("Description *"); etDesc.setText(o.optString("description",""));
        android.widget.EditText etDuree = new android.widget.EditText(this); etDuree.setHint("Durée (mois)"); etDuree.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); int d = o.optInt("dureeEnMois",0); if(d>0) etDuree.setText(String.valueOf(d));
        android.widget.EditText etSeances = new android.widget.EditText(this); etSeances.setHint("Nombre de séances"); etSeances.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); int ns = o.optInt("nombreSeances",0); if(ns>0) etSeances.setText(String.valueOf(ns));
        android.widget.EditText etPrix = new android.widget.EditText(this); etPrix.setHint("Prix (TND) *"); etPrix.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL); double p = o.optDouble("prix",0); etPrix.setText(String.valueOf(p));
        container.addView(etNom); container.addView(spType); container.addView(etDesc); container.addView(etDuree); container.addView(etSeances); container.addView(etPrix);
        spType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){ public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View v, int pos, long id){ String t=(String) spType.getSelectedItem(); etSeances.setVisibility(t.startsWith("PACK_COACHING")? View.VISIBLE: View.GONE);} public void onNothingSelected(android.widget.AdapterView<?> p){} });
        androidx.appcompat.app.AlertDialog dlg = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Modifier le type d'abonnement")
                .setView(container)
                .setNegativeButton("Annuler", (dialog,w)-> dialog.dismiss())
                .setPositiveButton("Enregistrer", null)
                .create();
        dlg.show();
        android.widget.Button ok = dlg.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
        ok.setOnClickListener(v -> {
            String nom = etNom.getText().toString().trim();
            String type = (String) spType.getSelectedItem();
            String desc = etDesc.getText().toString().trim();
            String duree = etDuree.getText().toString().trim();
            String seances = etSeances.getText().toString().trim();
            String prix = etPrix.getText().toString().trim();
            if(nom.isEmpty()||type==null||type.isEmpty()||desc.isEmpty()||prix.isEmpty()){ android.widget.Toast.makeText(this,"Veuillez remplir les champs requis", android.widget.Toast.LENGTH_SHORT).show(); return; }
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    long id = o.optLong("id");
                    org.json.JSONObject body = new org.json.JSONObject();
                    body.put("type", type);
                    body.put("nom", nom);
                    body.put("description", desc);
                    if(!duree.isEmpty()) body.put("dureeEnMois", Integer.parseInt(duree)); else body.put("dureeEnMois", org.json.JSONObject.NULL);
                    if(type.startsWith("PACK_COACHING")) { if(!seances.isEmpty()) body.put("nombreSeances", Integer.parseInt(seances)); else body.put("nombreSeances", org.json.JSONObject.NULL); }
                    body.put("prix", new java.math.BigDecimal(prix));
                    body.put("actif", true);
                    new ApiClient(this).putAuthJson("api/Abonnements/types/"+id, body);
                    runOnUiThread(() -> { dlg.dismiss(); restart(); });
                } catch(Exception e){ runOnUiThread(() -> android.widget.Toast.makeText(this,"Erreur lors de la modification", android.widget.Toast.LENGTH_SHORT).show()); }
            });
        });
    }

    @Override
    public void onDeleteType(long id) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Supprimer")
                .setMessage("Confirmez la suppression de ce type d'abonnement")
                .setNegativeButton("Annuler", (d,w)-> d.dismiss())
                .setPositiveButton("Supprimer", (d,w)-> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try { new ApiClient(this).deleteAuth("api/Abonnements/types/"+id); runOnUiThread(this::restart); } catch(Exception ignored){}
                    });
                })
                .show();
    }

    private void openCreateTypeDialog(){
        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(32,32,32,0);

        android.widget.EditText etNom = new android.widget.EditText(this); etNom.setHint("Nom de l'abonnement *");
        final android.widget.Spinner spType = new android.widget.Spinner(this);
        java.util.List<String> types = java.util.Arrays.asList("SALLE","COURS_COLLECTIFS","PACK_COACHING_5","PACK_COACHING_10","PACK_COACHING_20");
        android.widget.ArrayAdapter<String> adType = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spType.setAdapter(adType);
        android.widget.EditText etDesc = new android.widget.EditText(this); etDesc.setHint("Description *");
        android.widget.EditText etDuree = new android.widget.EditText(this); etDuree.setHint("Durée (mois)"); etDuree.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        android.widget.EditText etSeances = new android.widget.EditText(this); etSeances.setHint("Nombre de séances"); etSeances.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        android.widget.EditText etPrix = new android.widget.EditText(this); etPrix.setHint("Prix (TND) *"); etPrix.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        container.addView(etNom); container.addView(spType); container.addView(etDesc);
        container.addView(etDuree); container.addView(etSeances); container.addView(etPrix);

        spType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){ public void onItemSelected(android.widget.AdapterView<?> p, android.view.View v, int pos, long id){ String t = (String) spType.getSelectedItem(); etSeances.setVisibility(t.startsWith("PACK_COACHING")? View.VISIBLE : View.GONE); } public void onNothingSelected(android.widget.AdapterView<?> p){} });

        androidx.appcompat.app.AlertDialog dlg = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Créer un type d'abonnement")
                .setView(container)
                .setNegativeButton("Annuler", (d,w)-> d.dismiss())
                .setPositiveButton("Créer", null)
                .create();
        dlg.show();
        android.widget.Button ok = dlg.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
        ok.setOnClickListener(v -> {
            String nom = etNom.getText().toString().trim();
            String type = (String) spType.getSelectedItem();
            String desc = etDesc.getText().toString().trim();
            String duree = etDuree.getText().toString().trim();
            String seances = etSeances.getText().toString().trim();
            String prix = etPrix.getText().toString().trim();
            if(nom.isEmpty()||type==null||type.isEmpty()||desc.isEmpty()||prix.isEmpty()){ android.widget.Toast.makeText(this,"Veuillez remplir les champs requis", android.widget.Toast.LENGTH_SHORT).show(); return; }
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    org.json.JSONObject body = new org.json.JSONObject();
                    body.put("type", type);
                    body.put("nom", nom);
                    body.put("description", desc);
                    if(!duree.isEmpty()) body.put("dureeEnMois", Integer.parseInt(duree)); else body.put("dureeEnMois", JSONObject.NULL);
                    if(type.startsWith("PACK_COACHING")) { if(!seances.isEmpty()) body.put("nombreSeances", Integer.parseInt(seances)); else body.put("nombreSeances", JSONObject.NULL); }
                    body.put("prix", new java.math.BigDecimal(prix));
                    new ApiClient(this).postAuthJson("api/Abonnements/types", body);
                    runOnUiThread(() -> { dlg.dismiss(); restart(); });
                } catch(Exception e){ runOnUiThread(() -> android.widget.Toast.makeText(this,"Erreur lors de la création", android.widget.Toast.LENGTH_SHORT).show()); }
            });
        });
    }

    private void restart(){ finish(); startActivity(getIntent()); overridePendingTransition(0,0); }
}
