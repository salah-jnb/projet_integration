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

public class ClientCartesActivity extends AppCompatActivity {
    private ProgressBar progress; private TextView empty; private TextView solde; private android.widget.Button btnTransfer;
    private RecyclerView listTransfers; private final ArrayList<JSONObject> transfers = new ArrayList<>();
    private JSONObject card;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_client);
        android.view.ViewGroup content = findViewById(R.id.content);
        android.view.View page = android.view.LayoutInflater.from(this).inflate(R.layout.activity_client_cartes, content, false);
        content.addView(page);
        progress = page.findViewById(R.id.progress);
        empty = page.findViewById(R.id.empty);
        solde = page.findViewById(R.id.solde);
        btnTransfer = page.findViewById(R.id.btn_transferer);
        listTransfers = page.findViewById(R.id.list_transfers);
        listTransfers.setLayoutManager(new LinearLayoutManager(this));
        listTransfers.setAdapter(new BetterTransfersAdapter());
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        androidx.drawerlayout.widget.DrawerLayout drawer = findViewById(R.id.drawer);
        androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        com.google.android.material.navigation.NavigationView nav = findViewById(R.id.nav);
        nav.setNavigationItemSelectedListener(item -> { drawer.closeDrawers(); route(item.getItemId()); return true; });
        jnb.fitness.NavigationHeaderUtil.applyForClient(this);
        btnTransfer.setOnClickListener(v -> openTransferDialogUnified());
        load();
    }

    private void route(int id){ if(id==R.id.nav_dashboard){ startActivity(new android.content.Intent(this, ClientActivity.class)); finish(); } else if(id==R.id.nav_profil){ startActivity(new android.content.Intent(this, ClientProfileActivity.class)); finish(); } else if(id==R.id.nav_abonnements){ startActivity(new android.content.Intent(this, ClientAbonnementsActivity.class)); finish(); } else if(id==R.id.nav_carte){} else if(id==R.id.nav_coaching){ startActivity(new android.content.Intent(this, ClientCoachingActivity.class)); finish(); } else if(id==R.id.nav_cours){ startActivity(new android.content.Intent(this, ClientCollectiveClassesActivity.class)); finish(); } else if(id==R.id.nav_articles){ startActivity(new android.content.Intent(this, ClientArticlesActivity.class)); finish(); } else if(id==R.id.nav_produits){ startActivity(new android.content.Intent(this, ClientProductsActivity.class)); finish(); } else if(id==R.id.nav_parrainage){ startActivity(new android.content.Intent(this, ClientReferralsActivity.class)); finish(); } else if(id==R.id.nav_notifications){ startActivity(new android.content.Intent(this, ClientNotificationsActivity.class)); finish(); } else if(id==R.id.nav_logout){ new SessionManager(this).logout(); startActivity(new android.content.Intent(this, LandingActivity.class)); finish(); } }

    private void load(){ progress.setVisibility(View.VISIBLE); empty.setVisibility(View.GONE); Executors.newSingleThreadExecutor().execute(() -> { try { ApiClient api=new ApiClient(this); long userId=new SessionManager(this).getUserId(); JSONObject c=api.getAuthJson("api/Cartes/utilisateur/"+userId); card=c; long carteId=c.optLong("id"); double soldeVal=c.optDouble("solde", Double.NaN); double soldeCent=c.optDouble("soldeCent", Double.NaN); final String soldeText = !Double.isNaN(soldeCent)? String.format(java.util.Locale.getDefault(), "%.2f TND", soldeCent): (!Double.isNaN(soldeVal)? String.format(java.util.Locale.getDefault(), "%.2f TND", soldeVal): "0.00 TND"); JSONArray arr = api.getAuthArray("api/Transferts/carte/"+carteId); transfers.clear(); for(int i=0;i<arr.length();i++){ transfers.add(arr.getJSONObject(i)); } runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(card==null?View.VISIBLE:View.GONE); solde.setText(soldeText); RecyclerView.Adapter<?> a=listTransfers.getAdapter(); if(a!=null) a.notifyDataSetChanged(); }); } catch(Exception e){ runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(View.VISIBLE); }); } }); }

    @Override protected void onResume(){ super.onResume(); if(listTransfers!=null && !(listTransfers.getAdapter() instanceof BetterTransfersAdapter)){ listTransfers.setAdapter(new BetterTransfersAdapter()); } }

    private void openTransferDialog(){ android.widget.LinearLayout container=new android.widget.LinearLayout(this); container.setOrientation(android.widget.LinearLayout.VERTICAL); container.setPadding(32,32,32,0); android.widget.EditText search=new android.widget.EditText(this); search.setHint("Rechercher (nom, email, téléphone)"); final android.widget.ListView list=new android.widget.ListView(this); final java.util.ArrayList<JSONObject> users=new java.util.ArrayList<>(); final android.widget.ArrayAdapter<String> ad=new android.widget.ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new java.util.ArrayList<>()); list.setAdapter(ad); container.addView(search); container.addView(list); new Thread(() -> { try { JSONArray ua=new ApiClient(this).getAuthArray("api/Utilisateurs"); users.clear(); for(int i=0;i<ua.length();i++){ users.add(ua.getJSONObject(i)); } runOnUiThread(() -> { ad.clear(); for(JSONObject u: users){ ad.add(u.optString("prenom","")+" "+u.optString("nom"," ")+" - "+u.optString("email","")); } ad.notifyDataSetChanged(); }); } catch(Exception ignored){} }).start(); search.addTextChangedListener(new android.text.TextWatcher(){ public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int b,int c){ final String t=String.valueOf(s).toLowerCase(); ad.clear(); for(JSONObject u: users){ String nom=u.optString("nom",""), prenom=u.optString("prenom",""), email=u.optString("email",""), tel=u.optString("telephone",""); if(nom.toLowerCase().contains(t) || prenom.toLowerCase().contains(t) || email.toLowerCase().contains(t) || tel.toLowerCase().contains(t)){ ad.add(prenom+" "+nom+" - "+email); } } ad.notifyDataSetChanged(); } public void afterTextChanged(android.text.Editable s){} }); final JSONObject[] selected=new JSONObject[1]; list.setOnItemClickListener((p,v,pos,id)-> { String label=(String) p.getItemAtPosition(pos); for(JSONObject u: users){ String cmp=u.optString("prenom","")+" "+u.optString("nom"," ")+" - "+u.optString("email","" ); if(cmp.equals(label)){ selected[0]=u; break; } } }); new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Transférer des fonds").setView(container).setNegativeButton("Annuler", (d,w)-> d.dismiss()).setPositiveButton("Suivant", (d,w)-> { if(selected[0]==null){ android.widget.Toast.makeText(this, "Sélectionnez un recepteur", android.widget.Toast.LENGTH_SHORT).show(); return; } proceedTransfer(selected[0]); }).show(); }

    private void proceedTransfer(JSONObject user){ Executors.newSingleThreadExecutor().execute(() -> { try { long uid=user.optLong("id"); JSONObject rc=new ApiClient(this).getAuthJson("api/Cartes/utilisateur/"+uid); long receiverId=rc.optLong("id"); runOnUiThread(() -> { if(receiverId<=0){ android.widget.Toast.makeText(this, "Aucune carte pour cet utilisateur", android.widget.Toast.LENGTH_SHORT).show(); return; } openAmountDialog(receiverId); }); } catch(Exception e){ runOnUiThread(() -> android.widget.Toast.makeText(this, "Erreur récupération carte", android.widget.Toast.LENGTH_SHORT).show()); } }); }

    private void openTransferDialogUnified(){
        android.view.View view = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_client_transfer, null, false);
        final com.google.android.material.textfield.TextInputEditText etSearch = view.findViewById(R.id.et_search);
        final androidx.recyclerview.widget.RecyclerView rvUsers = view.findViewById(R.id.rv_users);
        final com.google.android.material.textfield.TextInputEditText etAmount = view.findViewById(R.id.et_amount);
        final androidx.appcompat.widget.AppCompatSpinner spDevise = view.findViewById(R.id.sp_devise);
        final com.google.android.material.textfield.TextInputEditText etMotif = view.findViewById(R.id.et_motif);

        rvUsers.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        android.widget.ArrayAdapter<String> devAd = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"TND"});
        devAd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDevise.setAdapter(devAd);

        final java.util.ArrayList<JSONObject> allUsers = new java.util.ArrayList<>();
        final java.util.ArrayList<JSONObject> displayedUsers = new java.util.ArrayList<>();
        final JSONObject[] selected = new JSONObject[1];
        final long[] receiverId = new long[]{0};

        final class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            TextView t1; TextView t2;
            VH(android.view.View itemView){ super(itemView); t1=itemView.findViewById(android.R.id.text1); t2=itemView.findViewById(android.R.id.text2); }
        }
        final androidx.recyclerview.widget.RecyclerView.Adapter<VH> adapter = new androidx.recyclerview.widget.RecyclerView.Adapter<VH>(){
            @Override public VH onCreateViewHolder(android.view.ViewGroup p,int t){ android.view.View v=android.view.LayoutInflater.from(p.getContext()).inflate(android.R.layout.simple_list_item_2,p,false); return new VH(v);} 
            @Override public void onBindViewHolder(VH h,int i){ JSONObject u=displayedUsers.get(i); h.t1.setText(u.optString("prenom","")+" "+u.optString("nom","")); h.t2.setText(u.optString("email","")); h.itemView.setOnClickListener(v->{ selected[0]=u; Executors.newSingleThreadExecutor().execute(() -> { try { long uid=selected[0].optLong("id"); JSONObject rc=new ApiClient(ClientCartesActivity.this).getAuthJson("api/Cartes/utilisateur/"+uid); long rid=rc.optLong("id"); receiverId[0]=rid; ClientCartesActivity.this.runOnUiThread(() -> { if(rid<=0){ android.widget.Toast.makeText(ClientCartesActivity.this,"Aucune carte pour cet utilisateur",android.widget.Toast.LENGTH_SHORT).show(); } else { android.widget.Toast.makeText(ClientCartesActivity.this,"Recepteur sélectionné (Carte #"+rid+")",android.widget.Toast.LENGTH_SHORT).show(); } }); } catch(Exception e){ ClientCartesActivity.this.runOnUiThread(() -> android.widget.Toast.makeText(ClientCartesActivity.this,"Erreur récupération carte",android.widget.Toast.LENGTH_SHORT).show()); } }); }); }
            @Override public int getItemCount(){ return displayedUsers.size(); }
        };
        rvUsers.setAdapter(adapter);

        new Thread(() -> {
            try {
                JSONArray ua = new ApiClient(this).getAuthArray("api/Utilisateurs");
                allUsers.clear();
                long me = new SessionManager(this).getUserId();
                for (int i = 0; i < ua.length(); i++) {
                    JSONObject u = ua.getJSONObject(i);
                    String type = u.optString("typeUtilisateur", "");
                    if ("CLIENT".equalsIgnoreCase(type) && u.optLong("id") != me) allUsers.add(u);
                }
                runOnUiThread(() -> { displayedUsers.clear(); displayedUsers.addAll(allUsers); adapter.notifyDataSetChanged(); });
            } catch (Exception ignored) {}
        }).start();

        etSearch.addTextChangedListener(new android.text.TextWatcher(){ public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int b,int c){ String t=String.valueOf(s).toLowerCase(); displayedUsers.clear(); for(JSONObject u: allUsers){ String nom=u.optString("nom",""), prenom=u.optString("prenom",""), email=u.optString("email",""), tel=u.optString("telephone",""); if(nom.toLowerCase().contains(t)||prenom.toLowerCase().contains(t)||email.toLowerCase().contains(t)||tel.toLowerCase().contains(t)){ displayedUsers.add(u);} } adapter.notifyDataSetChanged(); } public void afterTextChanged(android.text.Editable s){} });

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(view)
                .setNegativeButton("Annuler", (d, w) -> d.dismiss())
                .setPositiveButton("Confirmer le transfert", (d, w) -> {
                    if (selected[0] == null || receiverId[0] <= 0) { android.widget.Toast.makeText(this, "Veuillez sélectionner un recepteur avec carte valide", android.widget.Toast.LENGTH_SHORT).show(); return; }
                    String aStr = String.valueOf(etAmount.getText()); double parsed; try{ parsed=Double.parseDouble(aStr);} catch(Exception e){ parsed=0; }
                    if (parsed <= 0) { android.widget.Toast.makeText(this, "Veuillez entrer un montant valide", android.widget.Toast.LENGTH_SHORT).show(); return; }
                    final double amount = parsed; final String motifVal = String.valueOf(etMotif.getText()); String dev = (String) spDevise.getSelectedItem();
                    if (card != null && card.optLong("id") == receiverId[0]) { android.widget.Toast.makeText(this, "Transfert vers votre propre carte interdit", android.widget.Toast.LENGTH_SHORT).show(); return; }
                    Executors.newSingleThreadExecutor().execute(() -> { try { long emet = card != null ? card.optLong("id") : 0; JSONObject payload = new JSONObject().put("emetteurCarteId", emet).put("recepteurCarteId", receiverId[0]).put("montantEuro", amount).put("motif", motifVal.isEmpty()? "Transfert": motifVal).put("devise", dev==null?"TND":dev); new ApiClient(this).postAuthJson("api/Transferts", payload); runOnUiThread(() -> { android.widget.Toast.makeText(this, "Transfert effectué", android.widget.Toast.LENGTH_SHORT).show(); load(); }); } catch(Exception e){ runOnUiThread(() -> android.widget.Toast.makeText(this, "Échec du transfert", android.widget.Toast.LENGTH_SHORT).show()); } });
                })
                .show();
    }

    private void openAmountDialog(long receiverCardId){ android.widget.LinearLayout container=new android.widget.LinearLayout(this); container.setOrientation(android.widget.LinearLayout.VERTICAL); container.setPadding(32,32,32,0); final android.widget.EditText etAmount=new android.widget.EditText(this); etAmount.setHint("Montant (TND)"); etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL); final android.widget.EditText etMotif=new android.widget.EditText(this); etMotif.setHint("Motif"); container.addView(etAmount); container.addView(etMotif); final long rId=receiverCardId; new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Confirmer le transfert").setView(container).setNegativeButton("Annuler", (d,w)-> d.dismiss()).setPositiveButton("Transférer", (d,w)-> { String aStr=etAmount.getText().toString(); double parsed; try{ parsed=Double.parseDouble(aStr); } catch(Exception e){ parsed=0; } if(parsed<=0){ android.widget.Toast.makeText(this, "Montant invalide", android.widget.Toast.LENGTH_SHORT).show(); return; } final double amount=parsed; final String motifVal=etMotif.getText().toString(); Executors.newSingleThreadExecutor().execute(() -> { try { long emet=card!=null? card.optLong("id"):0; JSONObject payload=new JSONObject().put("emetteurCarteId", emet).put("recepteurCarteId", rId).put("montantEuro", amount).put("motif", motifVal.isEmpty()? "Transfert": motifVal).put("devise", "TND"); new ApiClient(this).postAuthJson("api/Transferts", payload); runOnUiThread(() -> { android.widget.Toast.makeText(this, "Transfert effectué", android.widget.Toast.LENGTH_SHORT).show(); load(); }); } catch(Exception e){ runOnUiThread(() -> android.widget.Toast.makeText(this, "Échec du transfert", android.widget.Toast.LENGTH_SHORT).show()); } }); }).show(); }

    private class TransfersAdapter extends RecyclerView.Adapter<TransfersAdapter.VH>{ class VH extends RecyclerView.ViewHolder{ TextView motif,date,amount,status; VH(View v){ super(v);} } @Override public VH onCreateViewHolder(android.view.ViewGroup p,int t){ android.widget.LinearLayout row=new android.widget.LinearLayout(p.getContext()); row.setOrientation(android.widget.LinearLayout.VERTICAL); row.setPadding(24,24,24,24); TextView tMotif=new TextView(p.getContext()); tMotif.setTextColor(0xFFFFFFFF); tMotif.setTextSize(16f); row.addView(tMotif); TextView tDate=new TextView(p.getContext()); tDate.setTextColor(0xFF9E9E9E); tDate.setTextSize(12f); row.addView(tDate); TextView tAmount=new TextView(p.getContext()); tAmount.setTextColor(0xFFFFFFFF); tAmount.setTextSize(16f); tAmount.setGravity(android.view.Gravity.END); row.addView(tAmount); TextView tStatus=new TextView(p.getContext()); tStatus.setTextColor(0xFFBDBDBD); tStatus.setTextSize(12f); tStatus.setGravity(android.view.Gravity.END); row.addView(tStatus); android.widget.LinearLayout container=new android.widget.LinearLayout(p.getContext()); container.setOrientation(android.widget.LinearLayout.VERTICAL); container.setBackgroundColor(p.getResources().getColor(R.color.input_background)); container.setPadding(16,16,16,16); container.addView(row); VH vh=new VH(container); vh.motif=tMotif; vh.date=tDate; vh.amount=tAmount; vh.status=tStatus; return vh;} @Override public void onBindViewHolder(VH h,int i){ JSONObject t=transfers.get(i); long cardId=card!=null? card.optLong("id"):0; boolean isEm=t.optLong("emetteurCarteId")==cardId; boolean isRec=t.optLong("recepteurCarteId")==cardId; h.motif.setText(t.optString("motif")); String dateIso=t.optString("dateTransfert"); h.date.setText(dateIso!=null && !dateIso.isEmpty()? dateIso.substring(0,10): ""); double m=t.optDouble("montantCent", t.optDouble("montant", 0)); String sign=isEm? "-": (isRec? "+": ""); h.amount.setText(sign+String.format(java.util.Locale.getDefault(), "%.2f TND", m)); h.amount.setTextColor(isEm? android.graphics.Color.RED: (isRec? 0xFF2E7D32: android.graphics.Color.GRAY)); h.status.setText(t.optString("statut")); }
        @Override public int getItemCount(){ return transfers.size(); }
    }


    private class BetterTransfersAdapter extends RecyclerView.Adapter<BetterTransfersAdapter.VH>{
        class VH extends RecyclerView.ViewHolder{ TextView motif,date,amount,status; VH(View v){ super(v);} }
        @Override public VH onCreateViewHolder(android.view.ViewGroup p,int t){ android.content.Context ctx=p.getContext(); com.google.android.material.card.MaterialCardView card=new com.google.android.material.card.MaterialCardView(ctx); card.setCardBackgroundColor(ctx.getResources().getColor(R.color.input_background)); card.setStrokeColor(ctx.getResources().getColor(R.color.input_border)); card.setStrokeWidth(1); card.setRadius(12f); android.widget.LinearLayout root=new android.widget.LinearLayout(ctx); root.setOrientation(android.widget.LinearLayout.VERTICAL); root.setPadding(16,16,16,16); android.widget.LinearLayout top=new android.widget.LinearLayout(ctx); top.setOrientation(android.widget.LinearLayout.HORIZONTAL); top.setGravity(android.view.Gravity.CENTER_VERTICAL); TextView tMotif=new TextView(ctx); tMotif.setTextColor(0xFFFFFFFF); tMotif.setTextSize(16f); android.widget.LinearLayout.LayoutParams lpMotif=new android.widget.LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f); tMotif.setLayoutParams(lpMotif); TextView tAmount=new TextView(ctx); tAmount.setTextColor(0xFFFFFFFF); tAmount.setTextSize(16f); tAmount.setGravity(android.view.Gravity.END); top.addView(tMotif); top.addView(tAmount); View divider=new View(ctx); divider.setBackgroundColor(ctx.getResources().getColor(R.color.divider_color)); android.widget.LinearLayout.LayoutParams lpDiv=new android.widget.LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1); lpDiv.topMargin=8; lpDiv.bottomMargin=8; divider.setLayoutParams(lpDiv); android.widget.LinearLayout bottom=new android.widget.LinearLayout(ctx); bottom.setOrientation(android.widget.LinearLayout.HORIZONTAL); bottom.setGravity(android.view.Gravity.CENTER_VERTICAL); TextView tDate=new TextView(ctx); tDate.setTextColor(0xFF9E9E9E); tDate.setTextSize(12f); android.widget.LinearLayout.LayoutParams lpDate=new android.widget.LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f); tDate.setLayoutParams(lpDate); TextView tStatus=new TextView(ctx); tStatus.setTextColor(0xFFFFFFFF); tStatus.setTextSize(12f); tStatus.setPadding(20,10,20,10); tStatus.setBackground(ctx.getResources().getDrawable(R.drawable.badge_status)); bottom.addView(tDate); bottom.addView(tStatus); root.addView(top); root.addView(divider); root.addView(bottom); card.addView(root); VH vh=new VH(card); vh.motif=tMotif; vh.date=tDate; vh.amount=tAmount; vh.status=tStatus; return vh; }
        @Override public void onBindViewHolder(VH h,int i){ JSONObject t=transfers.get(i); long cardId=card!=null? card.optLong("id"):0; boolean isEm=t.optLong("emetteurCarteId")==cardId; boolean isRec=t.optLong("recepteurCarteId")==cardId; h.motif.setText(t.optString("motif")); String dateIso=t.optString("dateTransfert"); h.date.setText(dateIso!=null && !dateIso.isEmpty()? dateIso.substring(0,10): ""); double m=t.optDouble("montantCent", t.optDouble("montant", 0)); String sign=isEm? "-": (isRec? "+": ""); int color=isEm? 0xFFD32F2F : (isRec? 0xFF2E7D32 : 0xFFFFFFFF); h.amount.setTextColor(color); h.amount.setText(sign+String.format(java.util.Locale.getDefault(), "%.2f TND", Math.abs(m))); String st=t.optString("statut", "SUCCES"); h.status.setText(st.toUpperCase(java.util.Locale.getDefault())); }
        @Override public int getItemCount(){ return transfers.size(); }
    }
}
