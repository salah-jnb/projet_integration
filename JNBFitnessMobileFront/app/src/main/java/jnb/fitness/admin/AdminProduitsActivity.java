package jnb.fitness.admin;
import jnb.fitness.R;
import jnb.fitness.ApiClient;
import jnb.fitness.SessionManager;
import jnb.fitness.LandingActivity;
import jnb.fitness.admin.AdminProductsAdapter;

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

public class AdminProduitsActivity extends AppCompatActivity implements AdminProductsAdapter.Callbacks {
    private RecyclerView list; private ProgressBar progress; private TextView empty; private final ArrayList<JSONObject> data = new ArrayList<>(); private final ArrayList<JSONObject> all = new ArrayList<>(); private android.widget.EditText search; private String selectedImageDataUri; private byte[] selectedImageBytes; private String selectedImageMime; private String selectedImageFileName;
    @Override protected void onCreate(Bundle savedInstanceState){ super.onCreate(savedInstanceState); setContentView(R.layout.activity_drawer_admin); android.view.ViewGroup content=findViewById(R.id.content); android.view.LayoutInflater.from(this).inflate(R.layout.activity_admin_produits, content, true); list=content.findViewById(R.id.list); progress=content.findViewById(R.id.progress); empty=content.findViewById(R.id.empty); search=content.findViewById(R.id.search); list.setLayoutManager(new LinearLayoutManager(this)); AdminProductsAdapter adapter=new AdminProductsAdapter(data, this); list.setAdapter(adapter); com.google.android.material.appbar.MaterialToolbar toolbar=findViewById(R.id.toolbar); setSupportActionBar(toolbar); androidx.drawerlayout.widget.DrawerLayout drawer=findViewById(R.id.drawer); androidx.appcompat.app.ActionBarDrawerToggle toggle=new androidx.appcompat.app.ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name); drawer.addDrawerListener(toggle); toggle.syncState(); com.google.android.material.navigation.NavigationView nav=findViewById(R.id.nav); nav.setNavigationItemSelectedListener(item->{ drawer.closeDrawers(); route(item.getItemId()); return true;}); android.view.View btnAdd = content.findViewById(R.id.btn_add); if(btnAdd!=null) btnAdd.setOnClickListener(v-> openCreateDialog()); if(search!=null){ search.addTextChangedListener(new android.text.TextWatcher(){ public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int b,int c){ filter(s.toString()); } public void afterTextChanged(android.text.Editable e){} }); } load(); }
    private void route(int id){ if(id==R.id.nav_dashboard){ startActivity(new android.content.Intent(this, AdminDashboardActivity.class)); finish(); } else if(id==R.id.nav_produits){} else if(id==R.id.nav_articles){ startActivity(new android.content.Intent(this, AdminArticlesActivity.class)); finish(); } else if(id==R.id.nav_abonnements){ startActivity(new android.content.Intent(this, AdminAbonnementsActivity.class)); finish(); } else if(id==R.id.nav_cours){ startActivity(new android.content.Intent(this, AdminCoursActivity.class)); finish(); } else if(id==R.id.nav_paiements){ startActivity(new android.content.Intent(this, AdminPaiementsActivity.class)); finish(); } else if(id==R.id.nav_cartes){ startActivity(new android.content.Intent(this, AdminCartesActivity.class)); finish(); } else if(id==R.id.nav_users){ startActivity(new android.content.Intent(this, AdminUsersActivity.class)); finish(); } else if(id==R.id.nav_logout){ new SessionManager(this).logout(); startActivity(new android.content.Intent(this, LandingActivity.class)); finish(); } }
    private void load(){ progress.setVisibility(View.VISIBLE); empty.setVisibility(View.GONE); Executors.newSingleThreadExecutor().execute(() -> { try { ApiClient api=new ApiClient(this); JSONArray arr=api.getAuthArray("api/Produits"); all.clear(); for(int i=0;i<arr.length();i++) all.add(arr.getJSONObject(i)); runOnUiThread(() -> { progress.setVisibility(View.GONE); filter(search!=null? search.getText().toString(): ""); }); } catch(Exception e){ runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(View.VISIBLE); }); } }); }

    private void filter(String q){ data.clear(); String s = q==null? "": q.trim().toLowerCase(); for(JSONObject o: all){ String nom=o.optString("nom",""); String cat=o.optString("categorie",""); if(s.isEmpty() || nom.toLowerCase().contains(s) || cat.toLowerCase().contains(s)){ data.add(o); } } RecyclerView.Adapter<?> a=list.getAdapter(); if(a!=null) a.notifyDataSetChanged(); empty.setVisibility(data.isEmpty()? View.VISIBLE: View.GONE); }
    @Override public void onDelete(long id){ Executors.newSingleThreadExecutor().execute(() -> { try { new ApiClient(this).deleteAuth("api/Produits/"+id); load(); } catch(Exception ignored){} }); }
    @Override public void onEdit(JSONObject item){ openEditDialog(item); }

    private static final int REQ_PICK_PRODUCT_IMAGE = 7021;
    private void openCreateDialog(){
        com.google.android.material.textfield.TextInputLayout tilNom = new com.google.android.material.textfield.TextInputLayout(this); tilNom.setHint("Nom"); com.google.android.material.textfield.TextInputEditText etNom = new com.google.android.material.textfield.TextInputEditText(this); tilNom.addView(etNom);
        com.google.android.material.textfield.TextInputLayout tilDesc = new com.google.android.material.textfield.TextInputLayout(this); tilDesc.setHint("Description"); com.google.android.material.textfield.TextInputEditText etDesc = new com.google.android.material.textfield.TextInputEditText(this); tilDesc.addView(etDesc);
        com.google.android.material.textfield.TextInputLayout tilPrix = new com.google.android.material.textfield.TextInputLayout(this); tilPrix.setHint("Prix"); com.google.android.material.textfield.TextInputEditText etPrix = new com.google.android.material.textfield.TextInputEditText(this); etPrix.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL); tilPrix.addView(etPrix);
        com.google.android.material.textfield.TextInputLayout tilCat = new com.google.android.material.textfield.TextInputLayout(this); tilCat.setHint("Catégorie"); com.google.android.material.textfield.TextInputEditText etCat = new com.google.android.material.textfield.TextInputEditText(this); tilCat.addView(etCat);
        android.widget.TextView tvImage = new android.widget.TextView(this); tvImage.setText(getString(R.string.product_image)); tvImage.setTextColor(getResources().getColor(R.color.white));
        com.google.android.material.button.MaterialButton btnPick = new com.google.android.material.button.MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle); btnPick.setText("Choisir une image"); btnPick.setOnClickListener(v-> openImagePicker());
        android.widget.LinearLayout container = new android.widget.LinearLayout(this); container.setOrientation(android.widget.LinearLayout.VERTICAL); int pad=(int)(16*getResources().getDisplayMetrics().density); container.setPadding(pad,pad,pad,0); container.addView(tilNom); container.addView(tilDesc); container.addView(tilPrix); container.addView(tilCat); container.addView(tvImage); container.addView(btnPick);
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this).setTitle(getString(R.string.create_product)).setView(container).setNegativeButton(getString(R.string.annuler),(d,w)-> d.dismiss()).setPositiveButton(getString(R.string.create_product),(d,w)->{
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    java.util.HashMap<String,String> fields = new java.util.HashMap<>();
                    fields.put("nom", String.valueOf(etNom.getText()));
                    fields.put("description", String.valueOf(etDesc.getText()));
                    String prixStr = String.valueOf(etPrix.getText()).trim();
                    if(prixStr.contains(".")) prixStr = prixStr.replace('.', ',');
                    fields.put("prix", prixStr);
                    fields.put("categorie", String.valueOf(etCat.getText()));
                    fields.put("imageUrl", selectedImageDataUri!=null? selectedImageDataUri : "");
                    new ApiClient(this).postAuthMultipart("api/Produits", fields, selectedImageBytes!=null?"image":null, selectedImageFileName, selectedImageBytes, selectedImageMime);
                    runOnUiThread(this::load);
                } catch(Exception ignored){}
            });
        }).show();
    }

    private void openEditDialog(JSONObject item){
        com.google.android.material.textfield.TextInputLayout tilNom = new com.google.android.material.textfield.TextInputLayout(this); tilNom.setHint("Nom"); com.google.android.material.textfield.TextInputEditText etNom = new com.google.android.material.textfield.TextInputEditText(this); etNom.setText(item.optString("nom","")); tilNom.addView(etNom);
        com.google.android.material.textfield.TextInputLayout tilDesc = new com.google.android.material.textfield.TextInputLayout(this); tilDesc.setHint("Description"); com.google.android.material.textfield.TextInputEditText etDesc = new com.google.android.material.textfield.TextInputEditText(this); etDesc.setText(item.optString("description","")); tilDesc.addView(etDesc);
        com.google.android.material.textfield.TextInputLayout tilPrix = new com.google.android.material.textfield.TextInputLayout(this); tilPrix.setHint("Prix"); com.google.android.material.textfield.TextInputEditText etPrix = new com.google.android.material.textfield.TextInputEditText(this); etPrix.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL); etPrix.setText(String.valueOf(item.optDouble("prix",0))); tilPrix.addView(etPrix);
        com.google.android.material.textfield.TextInputLayout tilCat = new com.google.android.material.textfield.TextInputLayout(this); tilCat.setHint("Catégorie"); com.google.android.material.textfield.TextInputEditText etCat = new com.google.android.material.textfield.TextInputEditText(this); etCat.setText(item.optString("categorie","")); tilCat.addView(etCat);
        com.google.android.material.switchmaterial.SwitchMaterial swActif = new com.google.android.material.switchmaterial.SwitchMaterial(this); swActif.setText("Actif"); swActif.setChecked(item.optBoolean("actif", true));
        android.widget.TextView tvImage = new android.widget.TextView(this); tvImage.setText(getString(R.string.product_image)); tvImage.setTextColor(getResources().getColor(R.color.white));
        com.google.android.material.button.MaterialButton btnPick = new com.google.android.material.button.MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle); btnPick.setText("Choisir une image"); btnPick.setOnClickListener(v-> openImagePicker());
        android.widget.LinearLayout container = new android.widget.LinearLayout(this); container.setOrientation(android.widget.LinearLayout.VERTICAL); int pad=(int)(16*getResources().getDisplayMetrics().density); container.setPadding(pad,pad,pad,0); container.addView(tilNom); container.addView(tilDesc); container.addView(tilPrix); container.addView(tilCat); container.addView(swActif); container.addView(tvImage); container.addView(btnPick);
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this).setTitle("Modifier le produit").setView(container).setNegativeButton(getString(R.string.annuler),(d,w)-> d.dismiss()).setPositiveButton("Enregistrer",(d,w)->{
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    java.util.HashMap<String,String> fields = new java.util.HashMap<>();
                    fields.put("nom", String.valueOf(etNom.getText()));
                    fields.put("description", String.valueOf(etDesc.getText()));
                    String prixStr = String.valueOf(etPrix.getText()).trim();
                    if(prixStr.contains(".")) prixStr = prixStr.replace('.', ',');
                    fields.put("prix", prixStr);
                    fields.put("categorie", String.valueOf(etCat.getText()));
                    fields.put("imageUrl", selectedImageDataUri!=null? selectedImageDataUri : item.optString("imageUrl",""));
                    fields.put("actif", String.valueOf(swActif.isChecked()));
                    long id = item.optLong("id");
                    new ApiClient(this).putAuthMultipartFields("api/Produits/"+id, fields, selectedImageBytes!=null?"image":null, selectedImageFileName, selectedImageBytes, selectedImageMime);
                    runOnUiThread(this::load);
                } catch(Exception ignored){}
            });
        }).show();
    }

    private void openImagePicker(){ android.content.Intent intent=new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT); intent.setType("image/*"); startActivityForResult(android.content.Intent.createChooser(intent, "Choisir une image"), REQ_PICK_PRODUCT_IMAGE); }
    @Override protected void onActivityResult(int requestCode, int resultCode, android.content.Intent dataIntent){ super.onActivityResult(requestCode, resultCode, dataIntent); if(requestCode==REQ_PICK_PRODUCT_IMAGE && resultCode==android.app.Activity.RESULT_OK && dataIntent!=null){ android.net.Uri uri=dataIntent.getData(); if(uri!=null){ try{ String mime=getContentResolver().getType(uri); java.io.InputStream is=getContentResolver().openInputStream(uri); byte[] bytes=readAll(is); is.close(); String b64=android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP); String prefix = (mime!=null && mime.contains("png"))? "data:image/png;base64,": "data:image/jpeg;base64,"; selectedImageDataUri = prefix + b64; selectedImageBytes = bytes; selectedImageMime = mime!=null? mime: "image/jpeg"; selectedImageFileName = "image" + (mime!=null && mime.contains("png")? ".png": ".jpg"); android.widget.Toast.makeText(this, "Image sélectionnée", android.widget.Toast.LENGTH_SHORT).show(); } catch(Exception e){ android.widget.Toast.makeText(this, "Erreur image", android.widget.Toast.LENGTH_SHORT).show(); } } } }
    private byte[] readAll(java.io.InputStream is) throws Exception { java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream(); byte[] buf=new byte[8192]; int r; while((r=is.read(buf))!=-1){ baos.write(buf,0,r);} return baos.toByteArray(); }
}
