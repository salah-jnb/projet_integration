package jnb.fitness.coach;
import jnb.fitness.R;
import jnb.fitness.ApiClient;
import jnb.fitness.SessionManager;
import jnb.fitness.LandingActivity;
import jnb.fitness.SimpleDeleteAdapter;
import androidx.core.content.ContextCompat;

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

public class CoachDisponibilitesActivity extends AppCompatActivity implements SimpleDeleteAdapter.Callbacks {
    private ProgressBar progress; private View empty; private final ArrayList<JSONObject> data = new ArrayList<>(); private android.widget.LinearLayout groups;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_coach);
        android.view.ViewGroup content = findViewById(R.id.content);
        android.view.View page = android.view.LayoutInflater.from(this).inflate(R.layout.activity_coach_disponibilites, content, false);
        content.addView(page);
        groups = page.findViewById(R.id.groups_container);
        progress = page.findViewById(R.id.progress);
        empty = page.findViewById(R.id.empty);
        android.view.View btnCreate = page.findViewById(R.id.btn_create);
        if(btnCreate!=null){ btnCreate.setOnClickListener(v->{ openEditDialogModern(null); }); }
        android.view.View btnEmptyCreate = page.findViewById(R.id.btn_add_empty);
        if(btnEmptyCreate!=null){ btnEmptyCreate.setOnClickListener(v->{ openEditDialogModern(null); }); }
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
    private void route(int id){ if(id==R.id.nav_dashboard){ startActivity(new android.content.Intent(this, CoachActivity.class)); finish(); } else if(id==R.id.nav_profil){ startActivity(new android.content.Intent(this, CoachProfileActivity.class)); finish(); } else if(id==R.id.nav_disponibilites){} else if(id==R.id.nav_reservations){ startActivity(new android.content.Intent(this, CoachReservationsActivity.class)); finish(); } else if(id==R.id.nav_cours){ startActivity(new android.content.Intent(this, CoachCollectiveClassesActivity.class)); finish(); } else if(id==R.id.nav_articles){ startActivity(new android.content.Intent(this, CoachArticlesActivity.class)); finish(); } else if(id==R.id.nav_produits){ startActivity(new android.content.Intent(this, CoachProductsActivity.class)); finish(); } else if(id==R.id.nav_notifications){ startActivity(new android.content.Intent(this, CoachNotificationsActivity.class)); finish(); } else if(id==R.id.nav_logout){ new SessionManager(this).logout(); startActivity(new android.content.Intent(this, LandingActivity.class)); finish(); } }
    private void load(){ progress.setVisibility(View.VISIBLE); empty.setVisibility(View.GONE); Executors.newSingleThreadExecutor().execute(() -> { try { ApiClient api=new ApiClient(this); long coachId=new SessionManager(this).getUserId(); JSONArray arr=api.getAuthArray("api/Disponibilites/coach/"+coachId); int oldSize=data.size(); data.clear(); for(int i=0;i<arr.length();i++){ JSONObject o=arr.getJSONObject(i); data.add(o);} runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(data.isEmpty()?View.VISIBLE:View.GONE); android.widget.Toast.makeText(this, "Disponibilités: "+data.size(), android.widget.Toast.LENGTH_SHORT).show(); renderGroups(); }); } catch(Exception e){ runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(View.VISIBLE); android.widget.Toast.makeText(this, "Erreur chargement disponibilités", android.widget.Toast.LENGTH_SHORT).show(); }); } }); }

    private void renderGroups(){ if(groups==null) return; groups.removeAllViews(); String[] order={"LUNDI","MARDI","MERCREDI","JEUDI","VENDREDI","SAMEDI","DIMANCHE"}; java.util.Map<String, java.util.List<JSONObject>> map=new java.util.HashMap<>(); for(JSONObject o: data){ if(!o.optBoolean("actif", true)) continue; String j=o.optString("jourSemaine","" ); if(j==null) j=""; java.util.List<JSONObject> list=map.get(j); if(list==null){ list=new java.util.ArrayList<>(); map.put(j, list);} list.add(o);} java.util.Comparator<JSONObject> byStart=(a,b)->{ String sa=a.optString("heureDebut",""), sb=b.optString("heureDebut","" ); String sa2=sa.length()>=5? sa.substring(0,5): sa; String sb2=sb.length()>=5? sb.substring(0,5): sb; return sa2.compareTo(sb2); }; for(String day: order){ java.util.List<JSONObject> slots=map.get(day); if(slots!=null) java.util.Collections.sort(slots, byStart); addDayCard(day, slots); } }

    private void addDayCard(String day, java.util.List<JSONObject> slots){ android.content.Context ctx=this; androidx.cardview.widget.CardView card=new androidx.cardview.widget.CardView(ctx); card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_bg_light)); card.setRadius(dp(12)); card.setCardElevation(dp(2)); android.widget.LinearLayout container=new android.widget.LinearLayout(ctx); container.setOrientation(android.widget.LinearLayout.VERTICAL); container.setPadding(dp(16),dp(16),dp(16),dp(16)); android.widget.LinearLayout header=new android.widget.LinearLayout(ctx); header.setOrientation(android.widget.LinearLayout.HORIZONTAL); header.setGravity(android.view.Gravity.CENTER_VERTICAL); android.widget.TextView tvDay=new android.widget.TextView(ctx); tvDay.setText(formatDayLabel(day)); tvDay.setTextColor(ContextCompat.getColor(this, R.color.white)); tvDay.setTextSize(18); tvDay.setTypeface(android.graphics.Typeface.DEFAULT_BOLD); android.widget.TextView tvCount=new android.widget.TextView(ctx); tvCount.setText(slots!=null? slots.size()+" créneaux":"Indisponible"); tvCount.setTextColor(ContextCompat.getColor(this, R.color.text_secondary)); tvCount.setTextSize(12); tvCount.setBackgroundResource(R.drawable.badge_pending); tvCount.setPadding(dp(8),dp(4),dp(8),dp(4)); android.widget.LinearLayout.LayoutParams lpLeft=new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,1f); header.addView(tvDay, lpLeft); header.addView(tvCount); container.addView(header);
        if(slots!=null && !slots.isEmpty()){ for(JSONObject o: slots){ container.addView(makeSlotRow(o)); } } android.widget.LinearLayout.LayoutParams lp=new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT); lp.setMargins(0, dp(8), 0, dp(8)); card.addView(container); groups.addView(card, lp); }

    private View makeSlotRow(JSONObject o){ android.content.Context ctx=this; android.widget.LinearLayout row=new android.widget.LinearLayout(ctx); row.setOrientation(android.widget.LinearLayout.HORIZONTAL); row.setGravity(android.view.Gravity.CENTER_VERTICAL); row.setBackgroundResource(R.drawable.edittext_background); row.setPadding(dp(12),dp(12),dp(12),dp(12)); android.widget.TextView tv=new android.widget.TextView(ctx); String j=o.optString("jourSemaine",""), hd=o.optString("heureDebut",""), hf=o.optString("heureFin","" ); String hd2=hd.length()>=5? hd.substring(0,5): hd; String hf2=hf.length()>=5? hf.substring(0,5): hf; tv.setText("\u23F0  "+hd2+" - "+hf2); tv.setTextColor(ContextCompat.getColor(this, R.color.white)); tv.setTextSize(16); android.widget.ImageButton btnEdit=new android.widget.ImageButton(ctx); btnEdit.setImageResource(R.drawable.ic_edit); btnEdit.setBackgroundResource(R.drawable.photo_button_bg); btnEdit.setColorFilter(ContextCompat.getColor(this, R.color.white)); btnEdit.setContentDescription("Modifier"); android.widget.ImageButton btnDel=new android.widget.ImageButton(ctx); btnDel.setImageResource(R.drawable.ic_delete); btnDel.setBackgroundResource(R.drawable.photo_button_bg); btnDel.setColorFilter(ContextCompat.getColor(this, R.color.white)); btnDel.setContentDescription("Supprimer"); long id=o.optLong("id"); btnEdit.setOnClickListener(v-> onEdit(o)); btnDel.setOnClickListener(v-> onDelete(id)); android.widget.LinearLayout.LayoutParams lpText=new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,1f); row.addView(tv, lpText); android.widget.LinearLayout.LayoutParams lpBtn=new android.widget.LinearLayout.LayoutParams(dp(40), dp(40)); lpBtn.setMargins(dp(8),0,0,0); row.addView(btnEdit, lpBtn); row.addView(btnDel, lpBtn); android.widget.LinearLayout.LayoutParams lp=new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT); lp.setMargins(0, dp(8),0,dp(8)); row.setLayoutParams(lp); return row; }

    private int dp(int v){ float d=getResources().getDisplayMetrics().density; return (int)(v*d); }
    private String formatDayLabel(String d){ switch(String.valueOf(d).toUpperCase()){ case "LUNDI": return "Lundi"; case "MARDI": return "Mardi"; case "MERCREDI": return "Mercredi"; case "JEUDI": return "Jeudi"; case "VENDREDI": return "Vendredi"; case "SAMEDI": return "Samedi"; case "DIMANCHE": return "Dimanche"; default: return d; } }
    private void openEditDialogModern(JSONObject item){
        android.widget.LinearLayout container=new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(32,32,32,0);

        String[] days={"LUNDI","MARDI","MERCREDI","JEUDI","VENDREDI","SAMEDI","DIMANCHE"};
        android.widget.Spinner spJour=new android.widget.Spinner(this);
        android.widget.ArrayAdapter<String> ad=new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, java.util.Arrays.asList(days));
        spJour.setAdapter(ad);
        String existingDay=item!=null? item.optString("jourSemaine","") : "";
        int idx=java.util.Arrays.asList(days).indexOf(existingDay!=null? existingDay.toUpperCase(): "");
        if(idx>=0) spJour.setSelection(idx);

        android.widget.EditText etDebut=new android.widget.EditText(this);
        etDebut.setHint("Heure début (HH:mm)");
        etDebut.setFocusable(false);
        etDebut.setClickable(true);
        String debStr=item!=null? item.optString("heureDebut","") : "";
        String debInit=debStr.length()>=5? debStr.substring(0,5): "";
        etDebut.setText(debInit);

        android.widget.EditText etFin=new android.widget.EditText(this);
        etFin.setHint("Heure fin (HH:mm)");
        etFin.setFocusable(false);
        etFin.setClickable(true);
        String finStr=item!=null? item.optString("heureFin","") : "";
        String finInit=finStr.length()>=5? finStr.substring(0,5): "";
        etFin.setText(finInit);

        androidx.appcompat.widget.SwitchCompat swActif=new androidx.appcompat.widget.SwitchCompat(this);
        swActif.setText("Actif");
        swActif.setChecked(item!=null? item.optBoolean("actif", true): true);

        android.view.View.OnClickListener openStart=v->{
            int h=0,m=0;
            if(etDebut.getText()!=null && etDebut.getText().length()==5){
                try{ h=Integer.parseInt(etDebut.getText().toString().substring(0,2)); m=Integer.parseInt(etDebut.getText().toString().substring(3,5)); } catch(Exception ignored){}
            }
            new android.app.TimePickerDialog(this,(picker,hh,mm)->{ etDebut.setText(String.format(java.util.Locale.getDefault(),"%02d:%02d",hh,mm)); },h,m,true).show();
        };
        android.view.View.OnClickListener openEnd=v->{
            int h=0,m=0;
            if(etFin.getText()!=null && etFin.getText().length()==5){
                try{ h=Integer.parseInt(etFin.getText().toString().substring(0,2)); m=Integer.parseInt(etFin.getText().toString().substring(3,5)); } catch(Exception ignored){}
            }
            new android.app.TimePickerDialog(this,(picker,hh,mm)->{ etFin.setText(String.format(java.util.Locale.getDefault(),"%02d:%02d",hh,mm)); },h,m,true).show();
        };
        etDebut.setOnClickListener(openStart);
        etFin.setOnClickListener(openEnd);

        container.addView(spJour);
        container.addView(etDebut);
        container.addView(etFin);
        container.addView(swActif);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(item!=null? "Modifier disponibilité": "Créer disponibilité")
                .setView(container)
                .setNegativeButton("Annuler", (dialog,w)-> dialog.dismiss())
                .setPositiveButton(item!=null? "Enregistrer": "Créer", (dlg,w)-> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            ApiClient api=new ApiClient(this);
                            String jour=String.valueOf(spJour.getSelectedItem());
                            String hdeb=etDebut.getText().toString();
                            String hfin=etFin.getText().toString();
                            JSONObject body=new JSONObject()
                                    .put("jourSemaine", jour)
                                    .put("heureDebut", hdeb+":00")
                                    .put("heureFin", hfin+":00")
                                    .put("actif", swActif.isChecked());
                            if(item!=null){ long id=item.optLong("id"); api.putAuthJson("api/Disponibilites/"+id, body); }
                            else { long coachId=new SessionManager(this).getUserId(); api.postAuthJson("api/Disponibilites/coach/"+coachId, body); }
                            runOnUiThread(this::load);
                        } catch(Exception e){ runOnUiThread(() -> android.widget.Toast.makeText(this, "Erreur", android.widget.Toast.LENGTH_SHORT).show()); }
                    });
                })
                .show();
    }
    private void openEditDialog(JSONObject item){ android.widget.LinearLayout container=new android.widget.LinearLayout(this); container.setOrientation(android.widget.LinearLayout.VERTICAL); android.widget.EditText etJour=new android.widget.EditText(this); etJour.setHint("Jour de semaine"); etJour.setText(item!=null? item.optString("jourSemaine","") : ""); android.widget.EditText etDebut=new android.widget.EditText(this); etDebut.setHint("Heure début (HH:mm)"); String debStr=item!=null? item.optString("heureDebut","") : ""; etDebut.setText(debStr.length()>=5? debStr.substring(0,5): debStr); android.widget.EditText etFin=new android.widget.EditText(this); etFin.setHint("Heure fin (HH:mm)"); String finStr=item!=null? item.optString("heureFin","") : ""; etFin.setText(finStr.length()>=5? finStr.substring(0,5): finStr); androidx.appcompat.widget.SwitchCompat swActif=new androidx.appcompat.widget.SwitchCompat(this); swActif.setText("Actif"); swActif.setChecked(item!=null? item.optBoolean("actif", true): true); container.setPadding(32,32,32,0); container.addView(etJour); container.addView(etDebut); container.addView(etFin); container.addView(swActif); new androidx.appcompat.app.AlertDialog.Builder(this).setTitle(item!=null? "Modifier disponibilité": "Créer disponibilité").setView(container).setNegativeButton("Annuler", (dialog,w)-> dialog.dismiss()).setPositiveButton(item!=null? "Enregistrer": "Créer", (dlg,w)-> { Executors.newSingleThreadExecutor().execute(() -> { try { ApiClient api=new ApiClient(this); JSONObject body=new JSONObject().put("jourSemaine", etJour.getText().toString()).put("heureDebut", etDebut.getText().toString()+":00").put("heureFin", etFin.getText().toString()+":00").put("actif", swActif.isChecked()); if(item!=null){ long id=item.optLong("id"); api.putAuthJson("api/Disponibilites/"+id, body); } else { long coachId=new SessionManager(this).getUserId(); api.postAuthJson("api/Disponibilites/coach/"+coachId, body); } runOnUiThread(this::load); } catch(Exception ignored){} }); }).show(); }
    @Override public void onEdit(JSONObject item){ openEditDialogModern(item); }
    @Override public void onDelete(long id){ Executors.newSingleThreadExecutor().execute(() -> { try { new ApiClient(this).deleteAuth("api/Disponibilites/"+id); load(); } catch(Exception ignored){} }); }
}
