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

public class AdminUsersActivity extends AppCompatActivity implements AdminUsersAdapter.Callbacks {
    private RecyclerView list;
    private ProgressBar progress;
    private TextView empty;
    private android.widget.EditText search;
    private final ArrayList<JSONObject> data = new ArrayList<>();
    private final ArrayList<JSONObject> display = new ArrayList<>();
    private android.widget.Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_admin);
        android.view.ViewGroup content = findViewById(R.id.content);
        android.view.LayoutInflater.from(this).inflate(R.layout.activity_admin_users, content, true);
        list = content.findViewById(R.id.list);
        progress = content.findViewById(R.id.progress);
        empty = content.findViewById(R.id.empty);
        search = content.findViewById(R.id.search);
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        androidx.drawerlayout.widget.DrawerLayout drawer = findViewById(R.id.drawer);
        androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        com.google.android.material.navigation.NavigationView nav = findViewById(R.id.nav);
        nav.setNavigationItemSelectedListener(item -> { drawer.closeDrawers(); route(item.getItemId()); return true; });
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(new AdminUsersAdapter(display, this));
        search.addTextChangedListener(new android.text.TextWatcher(){ public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int b,int c){ filter(s.toString()); } public void afterTextChanged(android.text.Editable e){} });
        btnCreate = content.findViewById(R.id.btn_create_user);
        if(btnCreate!=null){ btnCreate.setOnClickListener(v -> openCreateDialog()); }
        load();
    }

    private void route(int id) {
        if (id == R.id.nav_dashboard) {
            startActivity(new android.content.Intent(this, AdminDashboardActivity.class));
            finish();
        } else if (id == R.id.nav_users) {
        } else if (id == R.id.nav_abonnements) {
            startActivity(new android.content.Intent(this, AdminAbonnementsActivity.class));
            finish();
        } else if (id == R.id.nav_cours) {
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
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ApiClient api = new ApiClient(this);
                JSONArray arr = api.getAuthArray("api/Utilisateurs");
                data.clear();
                for (int i = 0; i < arr.length(); i++) data.add(arr.getJSONObject(i));
                String q = search.getText()!=null?search.getText().toString():"";
                int oldSize = display.size();
                applyFilter(q);
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    empty.setVisibility(display.isEmpty() ? View.VISIBLE : View.GONE);
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

    private void filter(String q){ applyFilter(q); RecyclerView.Adapter<?> a=list.getAdapter(); if(a!=null){ a.notifyDataSetChanged(); } empty.setVisibility(display.isEmpty()?View.VISIBLE:View.GONE); }
    private void applyFilter(String q){ display.clear(); String s=q.toLowerCase(); for(JSONObject o: data){ String nom=o.optString("nom",""), prenom=o.optString("prenom",""), email=o.optString("email",""
); if(nom.toLowerCase().contains(s) || prenom.toLowerCase().contains(s) || email.toLowerCase().contains(s)) display.add(o); } }

    @Override
    public void onToggleNewsletter(long id, boolean abonne) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                new ApiClient(this).putAuthJson("api/Utilisateurs/" + id + "/newsletter/" + abonne, new JSONObject());
                runOnUiThread(this::restart);
            } catch (Exception ignored) {}
        });
    }

    @Override
    public void onChangeStatus(long id, String statut) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                new ApiClient(this).putAuthJson("api/Utilisateurs/" + id + "/statut/" + statut, new JSONObject());
                runOnUiThread(this::restart);
            } catch (Exception ignored) {}
        });
    }

    @Override
    public void onDelete(long id) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Supprimer")
                .setMessage("Êtes-vous sûr de vouloir supprimer cet utilisateur?")
                .setNegativeButton("Annuler", (d, w) -> d.dismiss())
                .setPositiveButton("Supprimer", (d, w) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try { new ApiClient(this).deleteAuth("api/Utilisateurs/" + id); runOnUiThread(this::load); } catch (Exception ignored) {}
                    });
                }).show();
    }

    @Override
    public void onEdit(JSONObject user) {
        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        android.widget.EditText etPrenom = new android.widget.EditText(this);
        etPrenom.setHint("Prénom"); etPrenom.setText(user.optString("prenom",""));
        android.widget.EditText etNom = new android.widget.EditText(this);
        etNom.setHint("Nom"); etNom.setText(user.optString("nom",""));
        android.widget.EditText etTel = new android.widget.EditText(this);
        etTel.setHint("Téléphone"); etTel.setText(user.optString("telephone",""));
        android.widget.EditText etAdr = new android.widget.EditText(this);
        etAdr.setHint("Adresse"); etAdr.setText(user.optString("adresse",""));
        container.setPadding(32,32,32,0);
        container.addView(etPrenom); container.addView(etNom); container.addView(etTel); container.addView(etAdr);
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Modifier l'utilisateur")
                .setView(container)
                .setNegativeButton("Annuler", (d,w)-> d.dismiss())
                .setPositiveButton("Enregistrer", (d,w)-> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            JSONObject update = new JSONObject();
                            update.put("nom", etNom.getText().toString());
                            update.put("prenom", etPrenom.getText().toString());
                            update.put("telephone", etTel.getText().toString());
                            update.put("adresse", etAdr.getText().toString());
                            new ApiClient(this).putAuthJson("api/Utilisateurs/" + user.optLong("id"), update);
                            runOnUiThread(this::restart);
                        } catch(Exception ignored){}
                    });
                }).show();
    }

    private void openCreateDialog(){
        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(32,32,32,0);

        android.widget.EditText etPrenom = new android.widget.EditText(this); etPrenom.setHint("Prénom *");
        android.widget.EditText etNom = new android.widget.EditText(this); etNom.setHint("Nom *");
        android.widget.EditText etEmail = new android.widget.EditText(this); etEmail.setHint("Email *"); etEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        android.widget.EditText etPassword = new android.widget.EditText(this); etPassword.setHint("Mot de passe *"); etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        android.widget.EditText etTel = new android.widget.EditText(this); etTel.setHint("Téléphone *"); etTel.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        android.widget.EditText etAdr = new android.widget.EditText(this); etAdr.setHint("Adresse *");

        final android.widget.Spinner spType = new android.widget.Spinner(this);
        java.util.List<String> types = java.util.Arrays.asList("Client","Coach");
        android.widget.ArrayAdapter<String> adType = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spType.setAdapter(adType);

        container.addView(etPrenom); container.addView(etNom); container.addView(etEmail); container.addView(etPassword);
        container.addView(etTel); container.addView(etAdr); container.addView(spType);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Créer un nouvel utilisateur")
                .setView(container)
                .setNegativeButton("Annuler", (d,w)-> d.dismiss())
                .setPositiveButton("Suivant", (d,w)-> {
                    String prenom = etPrenom.getText().toString().trim();
                    String nom = etNom.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();
                    String tel = etTel.getText().toString().trim();
                    String adr = etAdr.getText().toString().trim();
                    String type = (String) spType.getSelectedItem();
                    if(prenom.isEmpty()||nom.isEmpty()||email.isEmpty()||password.isEmpty()||tel.isEmpty()||adr.isEmpty()){ android.widget.Toast.makeText(this,"Veuillez remplir tous les champs obligatoires", android.widget.Toast.LENGTH_SHORT).show(); return; }
                    NewUser u = new NewUser();
                    u.prenom=prenom; u.nom=nom; u.email=email; u.password=password; u.tel=tel; u.adr=adr; u.type=type;
                    if("Coach".equals(type)) openCoachDetailsDialog(u); else openClientParrainageDialog(u);
                })
                .show();
    }

    static class NewUser { String prenom, nom, email, password, tel, adr, type; }

    private void openClientParrainageDialog(NewUser u){
        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(32,32,32,0);
        android.widget.EditText etCode = new android.widget.EditText(this); etCode.setHint("Code de parrainage");
        container.addView(etCode);
        androidx.appcompat.app.AlertDialog dlg = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Informations client")
                .setView(container)
                .setNegativeButton("Retour", (d,w)-> { d.dismiss(); openCreateDialog(); })
                .setPositiveButton("Créer", null)
                .create();
        dlg.show();
        android.widget.Button btn = dlg.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
        btn.setOnClickListener(v -> {
            String code = etCode.getText().toString().trim();
            if(code.isEmpty()){
                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        JSONObject body = new JSONObject();
                        body.put("prenom", u.prenom);
                        body.put("nom", u.nom);
                        body.put("email", u.email);
                        body.put("telephone", u.tel);
                        body.put("adresse", u.adr);
                        body.put("motDePasse", u.password);
                        body.put("photo", "");
                        body.put("typeUtilisateur", 0);
                        body.put("codeParrainage", "");
                        body.put("specialites", "");
                        body.put("description", "");
                        new ApiClient(this).postAuthJson("api/Utilisateurs", body);
                        runOnUiThread(() -> { dlg.dismiss(); restart(); });
                    } catch(Exception ignored){}
                });
            } else {
                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        JSONObject res = new ApiClient(this).getAuthJson("api/Clients/parrainage/"+code);
                        boolean ok = res.has("utilisateurId") || res.has("codeParrainage") || res.has("CodeParrainage");
                        runOnUiThread(() -> {
                            if(ok){
                                Executors.newSingleThreadExecutor().execute(() -> {
                                    try {
                                        JSONObject body = new JSONObject();
                                        body.put("prenom", u.prenom);
                                        body.put("nom", u.nom);
                                        body.put("email", u.email);
                                        body.put("telephone", u.tel);
                                        body.put("adresse", u.adr);
                                        body.put("motDePasse", u.password);
                                        body.put("photo", "");
                                        body.put("typeUtilisateur", 0);
                                        body.put("codeParrainage", code);
                                        body.put("specialites", "");
                                        body.put("description", "");
                                        new ApiClient(this).postAuthJson("api/Utilisateurs", body);
                                        runOnUiThread(() -> { dlg.dismiss(); restart(); });
                                    } catch(Exception ignored){}
                                });
                            } else {
                                android.widget.Toast.makeText(this,"Code de parrainage introuvable", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch(Exception e){
                        runOnUiThread(() -> android.widget.Toast.makeText(this,"Code de parrainage introuvable", android.widget.Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });
    }

    private void openCoachDetailsDialog(NewUser u){
        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(32,32,32,0);
        android.widget.EditText etSpec = new android.widget.EditText(this); etSpec.setHint("Spécialités *");
        android.widget.EditText etDesc = new android.widget.EditText(this); etDesc.setHint("Description *");
        container.addView(etSpec); container.addView(etDesc);
        androidx.appcompat.app.AlertDialog dlg = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Informations coach")
                .setView(container)
                .setNegativeButton("Retour", (d,w)-> { d.dismiss(); openCreateDialog(); })
                .setPositiveButton("Créer", null)
                .create();
        dlg.show();
        android.widget.Button btn = dlg.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
        btn.setOnClickListener(v -> {
            String s = etSpec.getText().toString().trim();
            String ds = etDesc.getText().toString().trim();
            if(s.isEmpty()||ds.isEmpty()){
                android.widget.Toast.makeText(this,"Veuillez remplir spécialités et description", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    JSONObject body = new JSONObject();
                    body.put("prenom", u.prenom);
                    body.put("nom", u.nom);
                    body.put("email", u.email);
                    body.put("telephone", u.tel);
                    body.put("adresse", u.adr);
                    body.put("motDePasse", u.password);
                    body.put("photo", "");
                    body.put("typeUtilisateur", 1);
                    body.put("specialites", s);
                    body.put("description", ds);
                    body.put("codeParrainage", "");
                    new ApiClient(this).postAuthJson("api/Utilisateurs", body);
                    runOnUiThread(() -> { dlg.dismiss(); restart(); });
                } catch(Exception ignored){}
            });
        });
    }

    private void restart(){ finish(); startActivity(getIntent()); overridePendingTransition(0,0); }
}
