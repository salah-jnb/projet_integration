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

public class ClientCoachingActivity extends AppCompatActivity implements ClientCoachingAdapter.Callbacks {
    private RecyclerView list;
    private ProgressBar progress;
    private TextView empty;
    private final ArrayList<JSONObject> data = new ArrayList<>();
    private final ArrayList<JSONObject> all = new ArrayList<>();
    private final ArrayList<JSONObject> finishedNotRatedCache = new ArrayList<>();
    private com.google.android.material.chip.Chip chipPending, chipConfirmed, chipFinishedNotRated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_client);
        android.view.ViewGroup content = findViewById(R.id.content);
        android.view.View page = android.view.LayoutInflater.from(this).inflate(R.layout.activity_client_coaching, content, false);
        content.addView(page);
        list = page.findViewById(R.id.list);
        progress = page.findViewById(R.id.progress);
        empty = page.findViewById(R.id.empty);
        android.widget.Button book = page.findViewById(R.id.btn_book);
        chipPending = null;
        chipConfirmed = null;
        chipFinishedNotRated = null;
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
        list.setAdapter(new ClientCoachingAdapter(data, this));
        book.setOnClickListener(v -> openBookDialog());
        
        load();
    }

    private void route(int id) {
        if (id == R.id.nav_dashboard) {
            startActivity(new android.content.Intent(this, ClientActivity.class));
            finish();
        } else if (id == R.id.nav_profil) {
            startActivity(new android.content.Intent(this, ClientProfileActivity.class));
            finish();
        } else if (id == R.id.nav_abonnements) {
            startActivity(new android.content.Intent(this, ClientAbonnementsActivity.class));
            finish();
        } else if (id == R.id.nav_carte) {
            startActivity(new android.content.Intent(this, ClientCartesActivity.class));
            finish();
        } else if (id == R.id.nav_coaching) {
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
                JSONArray arr = api.getAuthArray("api/ReservationsCoaching/client/" + clientId);
                all.clear(); finishedNotRatedCache.clear();
                for (int i = 0; i < arr.length(); i++) all.add(arr.getJSONObject(i));
                for (JSONObject r : all) {
                    String st = r.optString("statut");
                    if ("TERMINEE".equalsIgnoreCase(st)) {
                        long rid = r.optLong("id");
                        try { JSONObject n = api.getAuthJson("api/Notations/reservation/" + rid); if (n == null || n.length() == 0) finishedNotRatedCache.add(r); } catch (Exception e) { finishedNotRatedCache.add(r); }
                    }
                }
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    applyFilter();
                });
            } catch (Exception e) {
                runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(View.VISIBLE); });
            }
        });
    }

    private void restartActivity(){ runOnUiThread(() -> { finish(); startActivity(getIntent()); }); }

    private void applyFilter(){
        boolean sp = chipPending==null || chipPending.isChecked();
        boolean sc = chipConfirmed==null || chipConfirmed.isChecked();
        boolean sf = chipFinishedNotRated==null || chipFinishedNotRated.isChecked();
        int old = data.size(); data.clear();
        if(sp || sc || sf){
            for(JSONObject r: all){ String st=r.optString("statut"); if(sp && "EN_ATTENTE".equalsIgnoreCase(st)) data.add(r); else if(sc && "CONFIRMEE".equalsIgnoreCase(st)) data.add(r); }
            if(sf){ for(JSONObject r: finishedNotRatedCache){ data.add(r);} }
        }
        empty.setVisibility(data.isEmpty()? View.VISIBLE: View.GONE);
        RecyclerView.Adapter<?> a = list.getAdapter(); if(a!=null){ if(old>0) a.notifyItemRangeRemoved(0, old); int ns=data.size(); if(ns>0) a.notifyItemRangeInserted(0, ns); }
    }

    @Override
    public void onCancel(long id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try { new ApiClient(this).deleteAuth("api/ReservationsCoaching/" + id); restartActivity(); } catch (Exception ignored) {}
        });
    }

    @Override
    public void onRate(long reservationId, long coachId, int note, String commentaire) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JSONObject body = new JSONObject()
                        .put("clientId", new SessionManager(this).getUserId())
                        .put("coachId", coachId)
                        .put("reservationCoachingId", reservationId)
                        .put("note", note)
                        .put("commentaire", commentaire);
                new ApiClient(this).postAuthJson("api/Notations", body);
                restartActivity();
            } catch (Exception ignored) {}
        });
    }

    @Override
    public void onRatePrompt(long reservationId, long coachId) {
        openRateDialog(reservationId, coachId);
    }

    private void openRateDialog(long reservationId, long coachId){ android.view.View view=android.view.LayoutInflater.from(this).inflate(R.layout.dialog_rate_coach, null, false); final android.widget.RatingBar rb=view.findViewById(R.id.rb_note); final com.google.android.material.textfield.TextInputEditText etComment=view.findViewById(R.id.et_comment); androidx.appcompat.app.AlertDialog dlg=new com.google.android.material.dialog.MaterialAlertDialogBuilder(this).setView(view).setNegativeButton("Annuler", (d,w)-> d.dismiss()).setPositiveButton("Envoyer", (d,w)-> { int note=Math.round(rb.getRating()); String c=String.valueOf(etComment.getText()); if(note<1||note>5){ android.widget.Toast.makeText(this, "Sélectionnez une note", android.widget.Toast.LENGTH_SHORT).show(); return; } onRate(reservationId, coachId, note, c==null? "": c); }).create(); dlg.show(); android.widget.Button pb=dlg.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE); if(pb!=null){ androidx.core.graphics.drawable.DrawableCompat.setTint(pb.getBackground(), getResources().getColor(R.color.jnb_orange)); pb.setTextColor(getResources().getColor(R.color.white)); }
    }

    private int parseSelected(String s){ if(s==null) return 5; if(s.startsWith("5")) return 5; if(s.startsWith("4")) return 4; if(s.startsWith("3")) return 3; if(s.startsWith("2")) return 2; return 1; }
    private void openBookDialog(){ android.widget.LinearLayout container=new android.widget.LinearLayout(this); container.setOrientation(android.widget.LinearLayout.VERTICAL); container.setPadding(32,32,32,0);
        final int DEFAULT_DUREE = 60;
        android.widget.TextView title=new android.widget.TextView(this); title.setText("Réserver une séance de coaching"); title.setTextSize(18f); title.setTextColor(0xFFFFFFFF); container.addView(title);
        android.widget.TextView subtitle=new android.widget.TextView(this); subtitle.setText("Sélectionnez un coach et un créneau"); subtitle.setTextColor(0xFFBDBDBD); subtitle.setTextSize(12f); container.addView(subtitle);

        android.widget.TextView labCoach=new android.widget.TextView(this); labCoach.setText("Coach *"); labCoach.setTextColor(0xFFFFFFFF); labCoach.setPadding(0,16,0,0); container.addView(labCoach);
        final android.widget.Spinner spCoach=new android.widget.Spinner(this); container.addView(spCoach);

        android.widget.LinearLayout dtRow=new android.widget.LinearLayout(this); dtRow.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        dtRow.setPadding(0,16,0,0);
        android.widget.LinearLayout.LayoutParams lpHalf=new android.widget.LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        final android.widget.TextView tvDate=new android.widget.TextView(this); tvDate.setText("Date *"); tvDate.setTextColor(0xFFFFFFFF);
        final android.widget.Button btnDate=new android.widget.Button(this); btnDate.setText("jj/mm/aaaa"); btnDate.setAllCaps(false); btnDate.setBackgroundColor(0x00000000); btnDate.setTextColor(0xFFEEEEEE);
        final android.widget.TextView tvTime=new android.widget.TextView(this); tvTime.setText("Heure *"); tvTime.setTextColor(0xFFFFFFFF);
        final android.widget.Button btnTime=new android.widget.Button(this); btnTime.setText("--:--"); btnTime.setAllCaps(false); btnTime.setBackgroundColor(0x00000000); btnTime.setTextColor(0xFFEEEEEE);
        android.widget.LinearLayout left=new android.widget.LinearLayout(this); left.setOrientation(android.widget.LinearLayout.VERTICAL); left.setLayoutParams(lpHalf); left.addView(tvDate); left.addView(btnDate);
        android.widget.LinearLayout right=new android.widget.LinearLayout(this); right.setOrientation(android.widget.LinearLayout.VERTICAL); right.setLayoutParams(lpHalf); right.addView(tvTime); right.addView(btnTime);
        dtRow.addView(left); dtRow.addView(right); container.addView(dtRow);

        int dispHeight=(int)(120 * getResources().getDisplayMetrics().density);
        final com.google.android.material.card.MaterialCardView dispCard=new com.google.android.material.card.MaterialCardView(this);
        dispCard.setCardBackgroundColor(getResources().getColor(R.color.input_background));
        dispCard.setStrokeColor(getResources().getColor(R.color.input_border));
        dispCard.setStrokeWidth(1);
        dispCard.setRadius(12f);
        android.widget.LinearLayout.LayoutParams cardLp=new android.widget.LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, dispHeight);
        cardLp.setMargins(0,16,0,0);
        dispCard.setLayoutParams(cardLp);
        android.widget.ScrollView sv=new android.widget.ScrollView(this);
        sv.setFillViewport(true);
        sv.setVerticalScrollBarEnabled(true);
        android.widget.LinearLayout svContent=new android.widget.LinearLayout(this);
        svContent.setOrientation(android.widget.LinearLayout.VERTICAL);
        svContent.setPadding(16,16,16,16);
        final com.google.android.material.chip.ChipGroup chipDisp=new com.google.android.material.chip.ChipGroup(this);
        svContent.addView(chipDisp);
        sv.addView(svContent);
        dispCard.addView(sv);
        container.addView(dispCard);

        

        final android.widget.Button btnCheck=new android.widget.Button(this); btnCheck.setText("Vérifier la disponibilité"); btnCheck.setAllCaps(false);
        final android.widget.TextView tvAvail=new android.widget.TextView(this); tvAvail.setTextColor(0xFFFFFFFF); tvAvail.setPadding(0,8,0,0); container.addView(btnCheck); container.addView(tvAvail);

        final java.util.ArrayList<org.json.JSONObject> coaches=new java.util.ArrayList<>(); final java.util.ArrayList<String> coachLabels=new java.util.ArrayList<>(); final long[] selectedCoachId=new long[]{0};

        new Thread(() -> { try{ org.json.JSONArray arr=new ApiClient(this).getAuthArray("api/Coachs"); coaches.clear(); coachLabels.clear(); for(int i=0;i<arr.length();i++){ org.json.JSONObject c=arr.getJSONObject(i); coaches.add(c); String nom=c.optString("nom",""), prenom=c.optString("prenom",""); long cid=c.optLong("utilisateurId", c.optLong("id",0)); coachLabels.add((prenom+" "+nom).trim()); if(i==0) selectedCoachId[0]=cid; } runOnUiThread(() -> { android.widget.ArrayAdapter<String> ad=new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, coachLabels); spCoach.setAdapter(ad); spCoach.setSelection(0); }); } catch(Exception ignored){} }).start();

        spCoach.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){ public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id){ try{ org.json.JSONObject c=coaches.get(position); long cid=c.optLong("utilisateurId", c.optLong("id",0)); selectedCoachId[0]=cid; chipDisp.removeAllViews(); Executors.newSingleThreadExecutor().execute(() -> { try { org.json.JSONArray dispArr=new ApiClient(ClientCoachingActivity.this).getAuthArray("api/Disponibilites/coach/"+cid); runOnUiThread(() -> { chipDisp.removeAllViews(); if(dispArr!=null){ for(int i=0;i<dispArr.length();i++){ try{ org.json.JSONObject d=dispArr.getJSONObject(i); boolean actif=d.optBoolean("actif", true); if(!actif) continue; String jour=d.optString("jourSemaine", d.optString("jour","")); String deb=d.optString("heureDebut", d.optString("debut","")); String fin=d.optString("heureFin", d.optString("fin","")); if(deb!=null && deb.length()>=5) deb=deb.substring(0,5); if(fin!=null && fin.length()>=5) fin=fin.substring(0,5); com.google.android.material.chip.Chip chip=new com.google.android.material.chip.Chip(ClientCoachingActivity.this); chip.setText(jour+"  "+deb+" - "+fin); chip.setClickable(false); chip.setCheckable(false); chipDisp.addView(chip); } catch(Exception ignored3){} } } }); } catch(Exception ignored1){} }); } catch(Exception ignored){} } public void onNothingSelected(android.widget.AdapterView<?> parent){} });

        final int[] selectedYMD = new int[]{-1, -1, -1};
        final int[] selectedHour = new int[]{-1};
        final int[] selectedMinute = new int[]{-1};
        btnDate.setOnClickListener(v -> { com.google.android.material.datepicker.MaterialDatePicker<Long> dp=com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker().build(); dp.addOnPositiveButtonClickListener(sel -> { java.util.Calendar c=java.util.Calendar.getInstance(); c.setTimeInMillis(sel); int d=c.get(java.util.Calendar.DAY_OF_MONTH), m=c.get(java.util.Calendar.MONTH)+1, y=c.get(java.util.Calendar.YEAR); btnDate.setText(String.format(java.util.Locale.getDefault(), "%02d/%02d/%04d", d,m,y)); selectedYMD[0]=y; selectedYMD[1]=m; selectedYMD[2]=d; }); dp.show(getSupportFragmentManager(), "dp"); });
        btnTime.setOnClickListener(v -> { com.google.android.material.timepicker.MaterialTimePicker tp=new com.google.android.material.timepicker.MaterialTimePicker.Builder().setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_24H).build(); tp.addOnPositiveButtonClickListener(v2 -> { int h=tp.getHour(), min=tp.getMinute(); btnTime.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d", h,min)); selectedHour[0]=h; selectedMinute[0]=min; }); tp.show(getSupportFragmentManager(), "tp"); });

        final boolean[] availOK = new boolean[]{false};
        btnCheck.setOnClickListener(v -> {
            if(selectedCoachId[0]==0 || selectedYMD[0]<=0 || selectedHour[0]<0 || selectedMinute[0]<0){ tvAvail.setText("Sélectionnez la date et l'heure"); tvAvail.setTextColor(0xFFBDBDBD); return; }
            Executors.newSingleThreadExecutor().execute(() -> {
                try{
                    java.util.Calendar cal=java.util.Calendar.getInstance();
                    cal.set(selectedYMD[0], selectedYMD[1]-1, selectedYMD[2], selectedHour[0], selectedMinute[0], 0);
                    String iso=toLocalDateTime(cal);
                    String url="api/ReservationsCoaching/check-availability?coachId="+selectedCoachId[0]+"&dateSeance="+iso+"&dureeMinutes="+DEFAULT_DUREE;
                    String resText=new ApiClient(this).getAuthText(url);
                    final boolean ok;
                    if(resText!=null){ String t=resText.trim(); if(t.startsWith("{")){ boolean tmp=false; try{ org.json.JSONObject obj=new org.json.JSONObject(t); tmp=obj.optBoolean("disponible", false) || obj.optBoolean("available", false); } catch(Exception ignored){} ok=tmp; } else { ok="true".equalsIgnoreCase(t) || "1".equals(t); } } else { ok=false; }
                    availOK[0]=ok;
                    runOnUiThread(() -> { tvAvail.setText(ok? "Disponible": "Indisponible"); tvAvail.setTextColor(ok? 0xFF2E7D32: 0xFFD32F2F); });
                } catch(Exception e){
                    runOnUiThread(() -> { availOK[0]=false; tvAvail.setText("Indisponible"); tvAvail.setTextColor(0xFFD32F2F); });
                }
            });
        });

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this).setTitle("Réserver").setView(container).setNegativeButton("Annuler", (d,w)-> d.dismiss()).setPositiveButton("Suivant", (d,w)-> {
            if(!availOK[0]){ return; }
            java.util.Calendar cal=java.util.Calendar.getInstance();
            cal.set(selectedYMD[0], selectedYMD[1]-1, selectedYMD[2], selectedHour[0], selectedMinute[0], 0);
            String iso=toLocalDateTime(cal);
            openBookConfirmDialog(selectedCoachId[0], iso);
        }).show(); }

    private void openBookConfirmDialog(long coachId, String iso){ android.widget.LinearLayout container=new android.widget.LinearLayout(this); container.setOrientation(android.widget.LinearLayout.VERTICAL); container.setPadding(32,32,32,0);
        final int DEFAULT_DUREE = 60;
        android.widget.TextView labType=new android.widget.TextView(this); labType.setText("Type de séance *"); labType.setTextColor(0xFFFFFFFF); labType.setPadding(0,0,0,0); container.addView(labType);
        final android.widget.Spinner spType=new android.widget.Spinner(this); container.addView(spType);
        java.util.ArrayList<String> types=new java.util.ArrayList<>(); types.add("PRESENTIEL"); types.add("EN_LIGNE"); android.widget.ArrayAdapter<String> adType=new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types); spType.setAdapter(adType);
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this).setTitle("Confirmer").setView(container).setNegativeButton("Annuler", (d,w)-> d.dismiss()).setPositiveButton("Réserver", (d,w)-> {
            Executors.newSingleThreadExecutor().execute(() -> { try { long clientId=new SessionManager(this).getUserId(); String typeSel=(String) spType.getSelectedItem(); org.json.JSONObject body=new org.json.JSONObject().put("clientId", clientId).put("coachId", coachId).put("dateSeance", iso).put("dureeMinutes", DEFAULT_DUREE).put("typeSeance", typeSel).put("statut", "EN_ATTENTE"); new ApiClient(this).postAuthJson("api/ReservationsCoaching", body); restartActivity(); } catch(Exception ignored){} });
        }).show(); }

    private int parseIntSafe(String s,int def){ try{ return Integer.parseInt(s.trim()); } catch(Exception e){ return def; } }
    private String toLocalDateTime(java.util.Calendar c){ java.text.SimpleDateFormat f=new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()); return f.format(c.getTime()); }
}
