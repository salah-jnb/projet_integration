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

public class CoachCollectiveClassesActivity extends AppCompatActivity implements CoachSeancesAdapter.Callbacks {
    private RecyclerView list; private ProgressBar progress; private View empty; private final ArrayList<JSONObject> data = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_coach);
        android.view.ViewGroup content = findViewById(R.id.content);
        android.view.View page = android.view.LayoutInflater.from(this).inflate(R.layout.activity_coach_collective_classes, content, false);
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
        list.setAdapter(new CoachSeancesAdapter(data, this));
        load();
    }
    private void route(int id){ if(id==R.id.nav_dashboard){ startActivity(new android.content.Intent(this, CoachActivity.class)); finish(); } else if(id==R.id.nav_profil){ startActivity(new android.content.Intent(this, CoachProfileActivity.class)); finish(); } else if(id==R.id.nav_disponibilites){ startActivity(new android.content.Intent(this, CoachDisponibilitesActivity.class)); finish(); } else if(id==R.id.nav_reservations){ startActivity(new android.content.Intent(this, CoachReservationsActivity.class)); finish(); } else if(id==R.id.nav_cours){} else if(id==R.id.nav_articles){ startActivity(new android.content.Intent(this, CoachArticlesActivity.class)); finish(); } else if(id==R.id.nav_produits){ startActivity(new android.content.Intent(this, CoachProductsActivity.class)); finish(); } else if(id==R.id.nav_notifications){ startActivity(new android.content.Intent(this, CoachNotificationsActivity.class)); finish(); } else if(id==R.id.nav_logout){ new SessionManager(this).logout(); startActivity(new android.content.Intent(this, LandingActivity.class)); finish(); } }
    private void load(){ progress.setVisibility(View.VISIBLE); empty.setVisibility(View.GONE); Executors.newSingleThreadExecutor().execute(() -> { try { long coachId=new SessionManager(this).getUserId(); JSONArray arr=new ApiClient(this).getAuthArray("api/CoursCollectifs/coach/"+coachId+"/seances/disponibles"); data.clear(); for(int i=0;i<arr.length();i++){ JSONObject s=arr.getJSONObject(i); data.add(s);} runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(data.isEmpty()?View.VISIBLE:View.GONE); RecyclerView.Adapter<?> a=list.getAdapter(); if(a!=null){ a.notifyDataSetChanged(); } }); } catch(Exception e){ runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(View.VISIBLE); }); } }); }
    @Override public void onParticipants(JSONObject seance){ Executors.newSingleThreadExecutor().execute(() -> { try { long seanceId=seance.optLong("id"); JSONArray arr=new ApiClient(this).getAuthArray("api/ReservationsCours/seance/"+seanceId); ArrayList<JSONObject> participants=new ArrayList<>(); for(int i=0;i<arr.length();i++){ JSONObject r=arr.getJSONObject(i); long clientId=r.optLong("clientId"); JSONObject user=null; try{ user=new ApiClient(this).getAuthJson("api/Utilisateurs/"+clientId); } catch(Exception ignored){} if(user!=null){ r.put("clientNom", user.optString("nom","Inconnu")); r.put("clientPrenom", user.optString("prenom","")); } participants.add(r);} runOnUiThread(() -> showParticipantsDialog(seance, participants)); } catch(Exception ignored){} }); }
    private void showParticipantsDialog(JSONObject seance, ArrayList<JSONObject> parts){ android.view.View view=android.view.LayoutInflater.from(this).inflate(R.layout.dialog_coach_participants, null, false); android.widget.TextView header=view.findViewById(R.id.header); header.setText(seance.optString("coursNom")+" • "+formatDate(seance.optString("dateSeance"))); androidx.recyclerview.widget.RecyclerView rv=view.findViewById(R.id.rv_participants); rv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this)); final class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder{ android.widget.ImageView avatar; android.widget.TextView name,email,status; VH(android.view.View item){ super(item); avatar=item.findViewById(R.id.avatar); name=item.findViewById(R.id.name); email=item.findViewById(R.id.email); status=item.findViewById(R.id.status);} } androidx.recyclerview.widget.RecyclerView.Adapter<VH> adapter=new androidx.recyclerview.widget.RecyclerView.Adapter<VH>(){ @Override public VH onCreateViewHolder(android.view.ViewGroup p,int vt){ android.view.View v=android.view.LayoutInflater.from(p.getContext()).inflate(R.layout.item_coach_participant, p, false); return new VH(v);} @Override public void onBindViewHolder(VH h,int i){ JSONObject p=parts.get(i); String nom=p.optString("clientNom",""), prenom=p.optString("clientPrenom",""), email=p.optString("clientEmail",""); h.name.setText((prenom+" "+nom).trim()); h.email.setText(email); String st=p.optString("statut","CONFIRMEE"); h.status.setText(st); } @Override public int getItemCount(){ return parts.size(); } }; rv.setAdapter(adapter); new com.google.android.material.dialog.MaterialAlertDialogBuilder(this).setView(view).setPositiveButton("Fermer", (d,w)-> d.dismiss()).show(); }
    private String formatDate(String iso){ try{ java.text.SimpleDateFormat in=new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.getDefault()); java.util.Date d=in.parse(normalizeIsoOffset(iso)); java.text.SimpleDateFormat out=new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()); return out.format(d);} catch(Exception e){ return iso; } }

    private String normalizeIsoOffset(String s){ if(s==null) return null; return s.replaceFirst("([+-]\\d{2}):(\\d{2})$","$1$2"); }
}
