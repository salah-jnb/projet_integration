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

public class CoachArticlesActivity extends AppCompatActivity implements CoachArticlesAdapter.Callbacks {
    private RecyclerView list; private ProgressBar progress; private View empty; private final ArrayList<JSONObject> data = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_coach);
        android.view.ViewGroup content = findViewById(R.id.content);
        android.view.View page = android.view.LayoutInflater.from(this).inflate(R.layout.activity_coach_articles, content, false);
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
        list.setAdapter(new CoachArticlesAdapter(data, this));
        android.widget.Button btnCreate = page.findViewById(R.id.btn_create);
        if(btnCreate!=null){ btnCreate.setOnClickListener(v-> openCreateDialog()); }
        load();
    }
    private void route(int id){ if(id==R.id.nav_dashboard){ startActivity(new android.content.Intent(this, CoachActivity.class)); finish(); } else if(id==R.id.nav_profil){ startActivity(new android.content.Intent(this, CoachProfileActivity.class)); finish(); } else if(id==R.id.nav_disponibilites){ startActivity(new android.content.Intent(this, CoachDisponibilitesActivity.class)); finish(); } else if(id==R.id.nav_reservations){ startActivity(new android.content.Intent(this, CoachReservationsActivity.class)); finish(); } else if(id==R.id.nav_cours){ startActivity(new android.content.Intent(this, CoachCollectiveClassesActivity.class)); finish(); } else if(id==R.id.nav_articles){} else if(id==R.id.nav_produits){ startActivity(new android.content.Intent(this, CoachProductsActivity.class)); finish(); } else if(id==R.id.nav_notifications){ startActivity(new android.content.Intent(this, CoachNotificationsActivity.class)); finish(); } else if(id==R.id.nav_logout){ new SessionManager(this).logout(); startActivity(new android.content.Intent(this, LandingActivity.class)); finish(); } }
    private void load(){ progress.setVisibility(View.VISIBLE); empty.setVisibility(View.GONE); Executors.newSingleThreadExecutor().execute(() -> { try { long coachId=new SessionManager(this).getUserId(); JSONArray arr=new ApiClient(this).getAuthArray("api/Articles/coach/"+coachId); data.clear(); for(int i=0;i<arr.length();i++){ data.add(arr.getJSONObject(i)); } runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(data.isEmpty()?View.VISIBLE:View.GONE); RecyclerView.Adapter<?> a=list.getAdapter(); if(a!=null){ a.notifyDataSetChanged(); } }); } catch(Exception e){ runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(View.VISIBLE); }); } }); }
    @Override public void onPublish(long id){ Executors.newSingleThreadExecutor().execute(() -> { try { new ApiClient(this).putAuthJson("api/Articles/"+id+"/soumettre", new JSONObject()); runOnUiThread(this::load);} catch(Exception ignored){} }); }
    @Override public void onDelete(long id){ Executors.newSingleThreadExecutor().execute(() -> { try { new ApiClient(this).deleteAuth("api/Articles/"+id); runOnUiThread(this::load);} catch(Exception ignored){} }); }
    private android.net.Uri pendingImageUri; private String pendingImageMime; private android.widget.TextView pendingImageLabel; private static final int REQ_PICK_ARTICLE_IMAGE = 7012;
    @Override public void onEdit(JSONObject article){ openEditDialog(article); }
    private void openCreateDialog(){ android.view.View view=android.view.LayoutInflater.from(this).inflate(R.layout.dialog_coach_article, null, false); final com.google.android.material.textfield.TextInputEditText etTitre=view.findViewById(R.id.et_titre); final com.google.android.material.textfield.TextInputEditText etContenu=view.findViewById(R.id.et_contenu); final com.google.android.material.textfield.MaterialAutoCompleteTextView etStatut=view.findViewById(R.id.et_statut); final android.widget.Button btnPick=view.findViewById(R.id.btn_pick_image); final android.widget.TextView tvName=view.findViewById(R.id.tv_image_name); pendingImageLabel=tvName; String[] codes=new String[]{"BROUILLON","EN_ATTENTE_VALIDATION"}; etStatut.setAdapter(new android.widget.ArrayAdapter<>(this, android.R.layout.simple_list_item_1, codes)); etStatut.setText(codes[1], false); etStatut.setOnClickListener(v -> etStatut.showDropDown()); btnPick.setOnClickListener(v -> startActivityForResult(new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT).setType("image/*"), REQ_PICK_ARTICLE_IMAGE)); androidx.appcompat.app.AlertDialog dlg=new com.google.android.material.dialog.MaterialAlertDialogBuilder(this).setView(view).setNegativeButton("Annuler", (d,w)-> d.dismiss()).setPositiveButton("Publier", (d,w)-> { Executors.newSingleThreadExecutor().execute(() -> { try { long coachId=new SessionManager(this).getUserId(); java.util.HashMap<String,String> fields=new java.util.HashMap<>(); fields.put("coachId", String.valueOf(coachId)); fields.put("titre", String.valueOf(etTitre.getText())); fields.put("contenu", String.valueOf(etContenu.getText())); fields.put("statut", String.valueOf(etStatut.getText())); byte[] fileBytes=null; String fileName=null; String mime=pendingImageMime; if(pendingImageUri!=null){ try{ java.io.InputStream is=getContentResolver().openInputStream(pendingImageUri); fileBytes=readAll(is); if(is!=null) is.close(); fileName=getFileNameFromUri(pendingImageUri); } catch(Exception ignored2){} } JSONObject created=new ApiClient(this).postAuthMultipart("api/Articles", fields, fileBytes!=null? "image": null, fileName!=null? fileName: "image.jpg", fileBytes, mime); runOnUiThread(this::load);} catch(Exception ignored){} }); }).create(); dlg.show(); android.widget.Button pb=dlg.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE); if(pb!=null){ androidx.core.graphics.drawable.DrawableCompat.setTint(pb.getBackground(), getResources().getColor(R.color.jnb_orange)); pb.setTextColor(getResources().getColor(R.color.white)); } }

    private void openEditDialog(JSONObject article){ android.view.View view=android.view.LayoutInflater.from(this).inflate(R.layout.dialog_coach_article, null, false); final android.widget.TextView title=view.findViewById(R.id.title_header); final android.widget.TextView sub=view.findViewById(R.id.sub_header); final android.widget.TextView labelStatut=view.findViewById(R.id.label_statut); final com.google.android.material.textfield.TextInputLayout tilStatut=(com.google.android.material.textfield.TextInputLayout) ((android.view.ViewGroup) view).findViewById(R.id.et_statut).getParent().getParent(); final com.google.android.material.textfield.TextInputEditText etTitre=view.findViewById(R.id.et_titre); final com.google.android.material.textfield.TextInputEditText etContenu=view.findViewById(R.id.et_contenu); final com.google.android.material.textfield.MaterialAutoCompleteTextView etStatut=view.findViewById(R.id.et_statut); final android.widget.Button btnPick=view.findViewById(R.id.btn_pick_image); final android.widget.TextView tvName=view.findViewById(R.id.tv_image_name); pendingImageLabel=tvName; title.setText("Modifier l'article"); sub.setText("Vous pouvez changer le titre, le contenu et l'image"); if(labelStatut!=null) labelStatut.setVisibility(View.GONE); if(tilStatut!=null) tilStatut.setVisibility(View.GONE); etTitre.setText(article.optString("titre","")); etContenu.setText(article.optString("contenu","")); btnPick.setOnClickListener(v -> startActivityForResult(new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT).setType("image/*"), REQ_PICK_ARTICLE_IMAGE)); long articleId=article.optLong("id"); androidx.appcompat.app.AlertDialog dlg=new com.google.android.material.dialog.MaterialAlertDialogBuilder(this).setView(view).setNegativeButton("Annuler", (d,w)-> d.dismiss()).setPositiveButton("Enregistrer", (d,w)-> { Executors.newSingleThreadExecutor().execute(() -> { try { java.util.HashMap<String,String> fields=new java.util.HashMap<>(); fields.put("titre", String.valueOf(etTitre.getText())); fields.put("contenu", String.valueOf(etContenu.getText())); byte[] fileBytes=null; String fileName=null; String mime=pendingImageMime; if(pendingImageUri!=null){ try{ java.io.InputStream is=getContentResolver().openInputStream(pendingImageUri); fileBytes=readAll(is); if(is!=null) is.close(); fileName=getFileNameFromUri(pendingImageUri); } catch(Exception ignored2){} } new ApiClient(this).putAuthMultipartFields("api/Articles/"+articleId, fields, fileBytes!=null? "image": null, fileName!=null? fileName: "image.jpg", fileBytes, mime); runOnUiThread(this::load);} catch(Exception ignored){} }); }).create(); dlg.show(); android.widget.Button pb=dlg.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE); if(pb!=null){ androidx.core.graphics.drawable.DrawableCompat.setTint(pb.getBackground(), getResources().getColor(R.color.jnb_orange)); pb.setTextColor(getResources().getColor(R.color.white)); } }

    @Override protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data){ super.onActivityResult(requestCode, resultCode, data); if(requestCode==REQ_PICK_ARTICLE_IMAGE && resultCode==android.app.Activity.RESULT_OK && data!=null){ android.net.Uri uri=data.getData(); if(uri!=null){ pendingImageUri=uri; try{ pendingImageMime=getContentResolver().getType(uri); if(pendingImageLabel!=null){ String name = getFileNameFromUri(uri); pendingImageLabel.setText(name==null? "Fichier choisi": name); } } catch(Exception ignored){} } } }

    private void uploadArticleImage(long articleId, android.net.Uri uri, String mime){ try{ java.io.InputStream is=getContentResolver().openInputStream(uri); byte[] bytes=readAll(is); if(is!=null) is.close(); final byte[] b=bytes; final String mt=mime!=null? mime: "image/jpeg"; new Thread(() -> { try { new ApiClient(this).putAuthMultipart("api/Articles/"+articleId+"/image", "image", "image.jpg", b, mt); } catch(Exception ignored){} }).start(); } catch(Exception ignored){} }
    private byte[] readAll(java.io.InputStream is) throws Exception { java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream(); byte[] buf=new byte[8192]; int r; while((r=is.read(buf))!=-1){ baos.write(buf,0,r);} return baos.toByteArray(); }
    private String getFileNameFromUri(android.net.Uri uri){ String name=null; try{ android.database.Cursor c=getContentResolver().query(uri, new String[]{ android.provider.MediaStore.MediaColumns.DISPLAY_NAME }, null, null, null); if(c!=null){ if(c.moveToFirst()){ name=c.getString(0);} c.close(); } } catch(Exception ignored){} return name; }
}
