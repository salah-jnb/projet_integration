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
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.Executors;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AdminCoursActivity extends AppCompatActivity implements AdminCoursAdapter.Callbacks {
    private RecyclerView list;
    private ProgressBar progress;
    private android.view.View empty;
    private final ArrayList<JSONObject> data = new ArrayList<>();
    private final ArrayList<JSONObject> sessions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_admin);
        android.view.ViewGroup content = findViewById(R.id.content);
        android.view.LayoutInflater.from(this).inflate(R.layout.activity_admin_cours, content, true);
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
        list.setAdapter(new AdminCoursAdapter(data, this));
        android.widget.Button btnAdd = content.findViewById(R.id.btn_add_course);
        com.google.android.material.floatingactionbutton.FloatingActionButton fab = content.findViewById(R.id.fab_add);
        if (btnAdd != null) btnAdd.setOnClickListener(v -> openCreateCourseDialog());
        if (fab != null) fab.setOnClickListener(v -> openCreateCourseDialog());
        load();
    }

    private void openCreateCourseDialog() {
        TextInputLayout tilNom = new TextInputLayout(this);
        tilNom.setHint("Nom *");
        TextInputEditText etNom = new TextInputEditText(this);
        tilNom.addView(etNom);

        TextInputLayout tilDesc = new TextInputLayout(this);
        tilDesc.setHint("Description");
        TextInputEditText etDesc = new TextInputEditText(this);
        tilDesc.addView(etDesc);

        TextInputLayout tilCoach = new TextInputLayout(this);
        tilCoach.setHint("Coach *");
        android.widget.Spinner spCoach = new android.widget.Spinner(this);
        tilCoach.addView(spCoach);

        TextInputLayout tilJour = new TextInputLayout(this);
        tilJour.setHint("Jour de la semaine *");
        android.widget.Spinner spJour = new android.widget.Spinner(this);
        java.util.List<String> jours = java.util.Arrays.asList("Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi","Dimanche");
        android.widget.ArrayAdapter<String> adJour = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, jours);
        spJour.setAdapter(adJour);
        tilJour.addView(spJour);

        TextInputLayout tilDeb = new TextInputLayout(this);
        tilDeb.setHint("Heure début *");
        TextInputEditText etDeb = new TextInputEditText(this);
        etDeb.setFocusable(false);
        etDeb.setClickable(true);
        tilDeb.addView(etDeb);

        TextInputLayout tilFin = new TextInputLayout(this);
        tilFin.setHint("Heure fin *");
        TextInputEditText etFin = new TextInputEditText(this);
        etFin.setFocusable(false);
        etFin.setClickable(true);
        tilFin.addView(etFin);

        TextInputLayout tilCap = new TextInputLayout(this);
        tilCap.setHint("Capacité max *");
        TextInputEditText etCap = new TextInputEditText(this);
        etCap.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        tilCap.addView(etCap);

        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, 0);
        container.addView(tilNom);
        container.addView(tilDesc);
        container.addView(tilCoach);
        container.addView(tilJour);
        container.addView(tilDeb);
        container.addView(tilFin);
        container.addView(tilCap);

        final java.util.List<Long> coachIds = new java.util.ArrayList<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JSONArray arr = new ApiClient(this).getAuthArray("api/Coachs");
                java.util.List<String> labels = new java.util.ArrayList<>();
                coachIds.clear();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject c = arr.getJSONObject(i);
                    long uid = c.optLong("utilisateurId");
                    String nom = c.optString("nom", "");
                    String prenom = c.optString("prenom", "");
                    labels.add(prenom + " " + nom);
                    coachIds.add(uid);
                }
                android.widget.ArrayAdapter<String> adCoach = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels);
                runOnUiThread(() -> spCoach.setAdapter(adCoach));
            } catch (Exception ignored) {}
        });

        final int[] startHour = {0};
        final int[] startMinute = {0};
        final int[] endHour = {0};
        final int[] endMinute = {0};

        etDeb.setOnClickListener(v -> {
            MaterialTimePicker tp = new MaterialTimePicker.Builder().setTitleText("Heure début").setTimeFormat(TimeFormat.CLOCK_24H).build();
            tp.addOnPositiveButtonClickListener(v2 -> {
                startHour[0] = tp.getHour();
                startMinute[0] = tp.getMinute();
                etDeb.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d", startHour[0], startMinute[0]));
            });
            tp.show(getSupportFragmentManager(), "time_start");
        });

        etFin.setOnClickListener(v -> {
            MaterialTimePicker tp = new MaterialTimePicker.Builder().setTitleText("Heure fin").setTimeFormat(TimeFormat.CLOCK_24H).build();
            tp.addOnPositiveButtonClickListener(v2 -> {
                endHour[0] = tp.getHour();
                endMinute[0] = tp.getMinute();
                etFin.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d", endHour[0], endMinute[0]));
            });
            tp.show(getSupportFragmentManager(), "time_end");
        });

        new MaterialAlertDialogBuilder(this)
                .setTitle("Créer un cours collectif")
                .setView(container)
                .setNegativeButton("Annuler", (d,w)-> d.dismiss())
                .setPositiveButton("Créer", (d,w)-> {
                    String nom = String.valueOf(etNom.getText()).trim();
                    String desc = String.valueOf(etDesc.getText()).trim();
                    String jour = (String) spJour.getSelectedItem();
                    String capStr = String.valueOf(etCap.getText()).trim();
                    if(nom.isEmpty() || jour == null || jour.isEmpty() || etDeb.getText()==null || etFin.getText()==null || capStr.isEmpty() || spCoach.getAdapter()==null || spCoach.getSelectedItemPosition()<0){
                        android.widget.Toast.makeText(this, "Veuillez remplir tous les champs obligatoires", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int pos = spCoach.getSelectedItemPosition();
                    long coachId = (pos>=0 && pos<coachIds.size()) ? coachIds.get(pos) : 0;
                    if(coachId==0){ android.widget.Toast.makeText(this, "Sélectionnez un coach", android.widget.Toast.LENGTH_SHORT).show(); return; }
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            String deb = String.format(java.util.Locale.getDefault(), "%02d:%02d:00", startHour[0], startMinute[0]);
                            String fin = String.format(java.util.Locale.getDefault(), "%02d:%02d:00", endHour[0], endMinute[0]);
                            JSONObject payload = new JSONObject();
                            payload.put("nom", nom);
                            payload.put("description", desc);
                            payload.put("coachId", coachId);
                            payload.put("jourSemaine", jour);
                            payload.put("heureDebut", deb);
                            payload.put("heureFin", fin);
                            payload.put("capaciteMax", Integer.parseInt(capStr));
                            new ApiClient(this).postAuthJson("api/CoursCollectifs", payload);
                            runOnUiThread(this::load);
                        } catch(Exception ignored){}
                    });
                })
                .show();
    }

    private void route(int id) {
        if (id == R.id.nav_dashboard) {
            startActivity(new android.content.Intent(this, AdminDashboardActivity.class));
            finish();
        } else if (id == R.id.nav_users) {
            startActivity(new android.content.Intent(this, AdminUsersActivity.class));
            finish();
        } else if (id == R.id.nav_abonnements) {
            startActivity(new android.content.Intent(this, AdminAbonnementsActivity.class));
            finish();
        } else if (id == R.id.nav_cours) {
        } else if (id == R.id.nav_logout) {
            new SessionManager(this).logout();
            startActivity(new android.content.Intent(this, LandingActivity.class));
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
        }
    }

    private void load() {
        progress.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ApiClient api = new ApiClient(this);
                JSONArray arr = api.getAuthArray("api/CoursCollectifs");
                int oldSize = data.size();
                data.clear();
                for (int i = 0; i < arr.length(); i++) data.add(arr.getJSONObject(i));
                try { JSONArray seances = api.getAuthArray("api/CoursCollectifs/seances"); sessions.clear(); for(int i=0;i<seances.length();i++){ JSONObject s=seances.getJSONObject(i); sessions.add(s); } } catch(Exception ignored){}
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    empty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                    RecyclerView.Adapter<?> a = list.getAdapter();
                    if (a != null) a.notifyDataSetChanged();
                    View emptyState = findViewById(R.id.empty);
                    if (emptyState != null) emptyState.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
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
    public void onDelete(long id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                new ApiClient(this).deleteAuth("api/CoursCollectifs/" + id);
                load();
            } catch (Exception ignored) {}
        });
    }

    @Override
    public void onEdit(JSONObject course) {
        TextInputLayout tilNom = new TextInputLayout(this);
        tilNom.setHint("Nom *");
        TextInputEditText etNom = new TextInputEditText(this);
        etNom.setText(course.optString("nom",""));
        tilNom.addView(etNom);

        TextInputLayout tilDesc = new TextInputLayout(this);
        tilDesc.setHint("Description");
        TextInputEditText etDesc = new TextInputEditText(this);
        etDesc.setText(course.optString("description",""));
        tilDesc.addView(etDesc);

        TextInputLayout tilJour = new TextInputLayout(this);
        tilJour.setHint("Jour de la semaine *");
        android.widget.Spinner spJour = new android.widget.Spinner(this);
        java.util.List<String> jours = java.util.Arrays.asList("Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi","Dimanche");
        android.widget.ArrayAdapter<String> adJour = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, jours);
        spJour.setAdapter(adJour);
        String currentJour = course.optString("jourSemaine", course.optString("jour",""));
        int selJour = jours.indexOf(currentJour);
        if(selJour>=0) spJour.setSelection(selJour);
        tilJour.addView(spJour);

        TextInputLayout tilDeb = new TextInputLayout(this);
        tilDeb.setHint("Heure début *");
        TextInputEditText etDeb = new TextInputEditText(this);
        etDeb.setFocusable(false);
        etDeb.setClickable(true);
        String curDeb = course.optString("heureDebut","");
        etDeb.setText(curDeb);
        tilDeb.addView(etDeb);

        TextInputLayout tilFin = new TextInputLayout(this);
        tilFin.setHint("Heure fin *");
        TextInputEditText etFin = new TextInputEditText(this);
        etFin.setFocusable(false);
        etFin.setClickable(true);
        String curFin = course.optString("heureFin","");
        etFin.setText(curFin);
        tilFin.addView(etFin);

        TextInputLayout tilCap = new TextInputLayout(this);
        tilCap.setHint("Capacité max *");
        TextInputEditText etCap = new TextInputEditText(this);
        etCap.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etCap.setText(String.valueOf(course.optInt("capaciteMax", course.optInt("capacite", 0))));
        tilCap.addView(etCap);

        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(pad,pad,pad,0);
        container.addView(tilNom);
        container.addView(tilDesc);
        container.addView(tilJour);
        container.addView(tilDeb);
        container.addView(tilFin);
        container.addView(tilCap);

        final int[] startHour = {0};
        final int[] startMinute = {0};
        final int[] endHour = {0};
        final int[] endMinute = {0};
        try {
            if(curDeb!=null && curDeb.length()>=5){ String[] p=curDeb.split(":"); startHour[0]=Integer.parseInt(p[0]); startMinute[0]=Integer.parseInt(p[1]); }
            if(curFin!=null && curFin.length()>=5){ String[] p=curFin.split(":"); endHour[0]=Integer.parseInt(p[0]); endMinute[0]=Integer.parseInt(p[1]); }
        } catch(Exception ignored){}

        etDeb.setOnClickListener(v->{ com.google.android.material.timepicker.MaterialTimePicker tp=new com.google.android.material.timepicker.MaterialTimePicker.Builder().setTitleText("Heure début").setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_24H).setHour(startHour[0]).setMinute(startMinute[0]).build(); tp.addOnPositiveButtonClickListener(v2->{ startHour[0]=tp.getHour(); startMinute[0]=tp.getMinute(); etDeb.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d", startHour[0], startMinute[0])); }); tp.show(getSupportFragmentManager(), "time_start_edit"); });
        etFin.setOnClickListener(v->{ com.google.android.material.timepicker.MaterialTimePicker tp=new com.google.android.material.timepicker.MaterialTimePicker.Builder().setTitleText("Heure fin").setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_24H).setHour(endHour[0]).setMinute(endMinute[0]).build(); tp.addOnPositiveButtonClickListener(v2->{ endHour[0]=tp.getHour(); endMinute[0]=tp.getMinute(); etFin.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d", endHour[0], endMinute[0])); }); tp.show(getSupportFragmentManager(), "time_end_edit"); });

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Modifier le cours")
                .setView(container)
                .setNegativeButton("Annuler", (d,w)-> d.dismiss())
                .setPositiveButton("Enregistrer", (d,w)-> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            JSONObject update = new JSONObject();
                            update.put("nom", String.valueOf(etNom.getText()));
                            update.put("description", String.valueOf(etDesc.getText()));
                            String jour = (String) spJour.getSelectedItem();
                            update.put("jourSemaine", jour);
                            String deb = String.format(java.util.Locale.getDefault(), "%02d:%02d:00", startHour[0], startMinute[0]);
                            String fin = String.format(java.util.Locale.getDefault(), "%02d:%02d:00", endHour[0], endMinute[0]);
                            update.put("heureDebut", deb);
                            update.put("heureFin", fin);
                            update.put("capaciteMax", Integer.parseInt(String.valueOf(etCap.getText())));
                            new ApiClient(this).putAuthJson("api/CoursCollectifs/"+course.optLong("id"), update);
                            runOnUiThread(this::load);
                        } catch(Exception ignored){}
                    });
                })
                .show();
    }

    @Override
    public void onCreateSeance(long courseId) {
        TextInputLayout tilDate = new TextInputLayout(this);
        tilDate.setHint("Date et heure");
        TextInputEditText etDate = new TextInputEditText(this);
        etDate.setFocusable(false);
        etDate.setClickable(true);
        tilDate.addView(etDate);

        TextInputLayout tilPlaces = new TextInputLayout(this);
        tilPlaces.setHint("Places");
        TextInputEditText etPlaces = new TextInputEditText(this);
        etPlaces.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        tilPlaces.addView(etPlaces);

        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(pad,pad,pad,0);
        container.addView(tilDate);
        container.addView(tilPlaces);

        final long[] selectedDateUtc = { -1 };
        final int[] selectedHour = { 0 };
        final int[] selectedMinute = { 0 };

        etDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> dp = MaterialDatePicker.Builder.datePicker().setTitleText("Choisir la date").build();
            dp.addOnPositiveButtonClickListener(utcMillis -> {
                selectedDateUtc[0] = utcMillis;
                MaterialTimePicker tp = new MaterialTimePicker.Builder().setTitleText("Choisir l'heure").setTimeFormat(TimeFormat.CLOCK_24H).build();
                tp.addOnPositiveButtonClickListener(v2 -> {
                    selectedHour[0] = tp.getHour();
                    selectedMinute[0] = tp.getMinute();
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    cal.setTimeInMillis(selectedDateUtc[0]);
                    Calendar local = Calendar.getInstance();
                    local.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), selectedHour[0], selectedMinute[0], 0);
                    java.text.SimpleDateFormat disp = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
                    etDate.setText(disp.format(local.getTime()));
                });
                tp.show(getSupportFragmentManager(), "time_picker");
            });
            dp.show(getSupportFragmentManager(), "date_picker");
        });

        tilDate.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        tilDate.setEndIconDrawable(R.drawable.ic_schedule);
        tilDate.setEndIconOnClickListener(v -> etDate.performClick());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Créer une séance")
                .setView(container)
                .setNegativeButton("Annuler", (d,w)-> d.dismiss())
                .setPositiveButton("Créer", (d,w)-> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            Calendar cal = Calendar.getInstance();
                            if (selectedDateUtc[0] > 0) {
                                Calendar baseUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                baseUtc.setTimeInMillis(selectedDateUtc[0]);
                                cal.set(baseUtc.get(Calendar.YEAR), baseUtc.get(Calendar.MONTH), baseUtc.get(Calendar.DAY_OF_MONTH), selectedHour[0], selectedMinute[0], 0);
                            }
                            java.text.SimpleDateFormat iso = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault());
                            iso.setTimeZone(TimeZone.getTimeZone("UTC"));
                            String isoStr = iso.format(cal.getTime());

                            JSONObject payload = new JSONObject();
                            payload.put("coursCollectifId", courseId);
                            payload.put("dateSeance", isoStr);
                            payload.put("placesDisponibles", Integer.parseInt(String.valueOf(etPlaces.getText())));
                            new ApiClient(this).postAuthJson("api/CoursCollectifs/seances", payload);
                            runOnUiThread(this::load);
                        } catch(Exception ignored){}
                    });
                }).show();
    }

    @Override
    public void onViewSeances(long courseId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JSONArray arr = new ApiClient(this).getAuthArray("api/CoursCollectifs/seances");
                ArrayList<JSONObject> listSess = new ArrayList<>();
                for(int i=0;i<arr.length();i++){ JSONObject s=arr.getJSONObject(i); if(s.optLong("coursCollectifId")==courseId) listSess.add(s); }
                runOnUiThread(() -> {
                    android.widget.ScrollView scroll = new android.widget.ScrollView(this);
                    android.widget.LinearLayout listContainer = new android.widget.LinearLayout(this);
                    listContainer.setOrientation(android.widget.LinearLayout.VERTICAL);
                    int pad = (int) (16 * getResources().getDisplayMetrics().density);
                    listContainer.setPadding(pad,pad,pad,pad);
                    scroll.addView(listContainer);

                    java.text.SimpleDateFormat disp = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
                    java.text.SimpleDateFormat isoZ = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()); isoZ.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                    java.text.SimpleDateFormat isoNoZ = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());

                    for(JSONObject s: listSess){
                        com.google.android.material.card.MaterialCardView card = new com.google.android.material.card.MaterialCardView(this);
                        card.setCardBackgroundColor(getResources().getColor(R.color.card_background_dark));
                        card.setStrokeColor(getResources().getColor(R.color.jnb_orange));
                        card.setStrokeWidth((int)(1 * getResources().getDisplayMetrics().density));
                        card.setRadius(16);

                        android.widget.LinearLayout row = new android.widget.LinearLayout(this);
                        row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                        row.setPadding(pad,pad,pad,pad);

                        android.widget.TextView tv = new android.widget.TextView(this);
                        String ds = s.optString("dateSeance");
                        String txt = ds;
                        try { java.util.Date d = isoZ.parse(ds); if(d!=null) txt = disp.format(d); } catch(Exception ignored){}
                        if(txt.equals(ds)) { try { java.util.Date d2 = isoNoZ.parse(ds); if(d2!=null) txt = disp.format(d2); } catch(Exception ignored){} }
                        tv.setText(txt+" • "+s.optInt("placesDisponibles")+" places");
                        tv.setTextColor(getResources().getColor(R.color.white));
                        android.widget.LinearLayout.LayoutParams tvLp = new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                        tv.setLayoutParams(tvLp);

                        com.google.android.material.button.MaterialButton btnEdit = new com.google.android.material.button.MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
                        btnEdit.setIconResource(R.drawable.ic_edit);
                        btnEdit.setIconTint(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.white)));
                        btnEdit.setText("");
                        btnEdit.setOnClickListener(v->{ int id=(int)s.optLong("id"); editSeance(id, s); });

                        com.google.android.material.button.MaterialButton btnDelete = new com.google.android.material.button.MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
                        btnDelete.setIconResource(R.drawable.ic_delete);
                        btnDelete.setIconTint(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.white)));
                        btnDelete.setText("");
                        btnDelete.setOnClickListener(v->{ int id=(int)s.optLong("id"); deleteSeance(id); });

                        row.addView(tv);
                        row.addView(btnEdit);
                        row.addView(btnDelete);
                        card.addView(row);
                        android.widget.LinearLayout.LayoutParams cardLp = new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                        cardLp.bottomMargin = pad/2;
                        listContainer.addView(card, cardLp);
                    }

                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                            .setTitle("Séances")
                            .setView(scroll)
                            .setPositiveButton("Fermer", (d,w)-> d.dismiss())
                            .show();
                });
            } catch(Exception ignored){}
        });
    }

    private void editSeance(int id, JSONObject s){
        TextInputLayout tilDate = new TextInputLayout(this);
        tilDate.setHint("Date et heure");
        TextInputEditText etDate = new TextInputEditText(this);
        etDate.setFocusable(false);
        etDate.setClickable(true);
        tilDate.addView(etDate);

        TextInputLayout tilPlaces = new TextInputLayout(this);
        tilPlaces.setHint("Places");
        TextInputEditText etPlaces = new TextInputEditText(this);
        etPlaces.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etPlaces.setText(String.valueOf(s.optInt("placesDisponibles",0)));
        tilPlaces.addView(etPlaces);

        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(pad,pad,pad,0);
        container.addView(tilDate);
        container.addView(tilPlaces);

        final long[] selectedDateUtc = { -1 };
        final int[] selectedHour = { 0 };
        final int[] selectedMinute = { 0 };

        etDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> dp = MaterialDatePicker.Builder.datePicker().setTitleText("Choisir la date").build();
            dp.addOnPositiveButtonClickListener(utcMillis -> {
                selectedDateUtc[0] = utcMillis;
                MaterialTimePicker tp = new MaterialTimePicker.Builder().setTitleText("Choisir l'heure").setTimeFormat(TimeFormat.CLOCK_24H).build();
                tp.addOnPositiveButtonClickListener(v2 -> {
                    selectedHour[0] = tp.getHour();
                    selectedMinute[0] = tp.getMinute();
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    cal.setTimeInMillis(selectedDateUtc[0]);
                    Calendar local = Calendar.getInstance();
                    local.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), selectedHour[0], selectedMinute[0], 0);
                    java.text.SimpleDateFormat disp = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
                    etDate.setText(disp.format(local.getTime()));
                });
                tp.show(getSupportFragmentManager(), "time_picker_edit");
            });
            dp.show(getSupportFragmentManager(), "date_picker_edit");
        });

        tilDate.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        tilDate.setEndIconDrawable(R.drawable.ic_schedule);
        tilDate.setEndIconOnClickListener(v -> etDate.performClick());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Modifier la séance")
                .setView(container)
                .setNegativeButton("Annuler", (d,w)-> d.dismiss())
                .setPositiveButton("Enregistrer", (d,w)-> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            Calendar cal = Calendar.getInstance();
                            if (selectedDateUtc[0] > 0) {
                                Calendar baseUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                baseUtc.setTimeInMillis(selectedDateUtc[0]);
                                cal.set(baseUtc.get(Calendar.YEAR), baseUtc.get(Calendar.MONTH), baseUtc.get(Calendar.DAY_OF_MONTH), selectedHour[0], selectedMinute[0], 0);
                            } else {
                                // fallback: garder la date existante si non modifiée
                                String current = s.optString("dateSeance","");
                                if (!current.isEmpty()) {
                                    // laisser tel quel côté serveur
                                    JSONObject payload = new JSONObject();
                                    payload.put("dateSeance", current);
                                    payload.put("placesDisponibles", Integer.parseInt(String.valueOf(etPlaces.getText())));
                                    new ApiClient(this).putAuthJson("api/CoursCollectifs/seances/"+id, payload);
                                    runOnUiThread(this::load);
                                    return;
                                }
                            }
                            java.text.SimpleDateFormat iso = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault());
                            iso.setTimeZone(TimeZone.getTimeZone("UTC"));
                            String isoStr = iso.format(cal.getTime());

                            JSONObject payload = new JSONObject();
                            payload.put("dateSeance", isoStr);
                            payload.put("placesDisponibles", Integer.parseInt(String.valueOf(etPlaces.getText())));
                            new ApiClient(this).putAuthJson("api/CoursCollectifs/seances/"+id, payload);
                            runOnUiThread(this::load);
                        } catch(Exception ignored){}
                    });
                }).show();
    }

    private void deleteSeance(int id){ Executors.newSingleThreadExecutor().execute(() -> { try { new ApiClient(this).deleteAuth("api/CoursCollectifs/seances/"+id); runOnUiThread(this::load);} catch(Exception ignored){} }); }
}
