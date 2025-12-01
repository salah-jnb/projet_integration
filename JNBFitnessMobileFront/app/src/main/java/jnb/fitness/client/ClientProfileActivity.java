package jnb.fitness.client;
import jnb.fitness.R;
import jnb.fitness.ApiClient;
import jnb.fitness.SessionManager;
import jnb.fitness.LandingActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.concurrent.Executors;

public class ClientProfileActivity extends AppCompatActivity {
    private EditText nom, prenom, email, tel, adr; private ProgressBar progress; private TextView msg; private androidx.appcompat.widget.SwitchCompat swNewsletter; private android.widget.ImageView photo; private android.widget.ImageView btnChangePhoto; private android.widget.Button btnChangePassword; private boolean newsletter;
    private static final int REQ_PICK_IMAGE = 6021;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_client);
        android.view.ViewGroup content = findViewById(R.id.content);
        android.view.LayoutInflater.from(this).inflate(R.layout.activity_client_profile, content, true);
        progress = content.findViewById(R.id.progress);
        msg = content.findViewById(R.id.msg);
        nom = content.findViewById(R.id.input_nom);
        prenom = content.findViewById(R.id.input_prenom);
        email = content.findViewById(R.id.input_email);
        tel = content.findViewById(R.id.input_tel);
        adr = content.findViewById(R.id.input_adr);
        swNewsletter = content.findViewById(R.id.switch_newsletter);
        photo = content.findViewById(R.id.photo);
        btnChangePhoto = content.findViewById(R.id.btn_change_photo);
        btnChangePassword = content.findViewById(R.id.btn_change_password);
        android.widget.Button save = content.findViewById(R.id.btn_save);
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        androidx.drawerlayout.widget.DrawerLayout drawer = findViewById(R.id.drawer);
        androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        com.google.android.material.navigation.NavigationView nav = findViewById(R.id.nav);
        nav.setNavigationItemSelectedListener(item -> { drawer.closeDrawers(); route(item.getItemId()); return true; });
        jnb.fitness.NavigationHeaderUtil.applyForClient(this);
        save.setOnClickListener(v -> update());
        if(btnChangePhoto!=null){ btnChangePhoto.setOnClickListener(v -> openPhotoPicker()); }
        if(btnChangePassword!=null){ btnChangePassword.setOnClickListener(v -> openChangePasswordDialog()); }
        if(swNewsletter!=null){ swNewsletter.setOnCheckedChangeListener((b,checked) -> toggleNewsletter(checked)); }
        load();
    }
    private void route(int id){ if(id==R.id.nav_dashboard){ startActivity(new android.content.Intent(this, ClientActivity.class)); finish(); } else if(id==R.id.nav_profil){} else if(id==R.id.nav_abonnements){ startActivity(new android.content.Intent(this, ClientAbonnementsActivity.class)); finish(); } else if(id==R.id.nav_carte){ startActivity(new android.content.Intent(this, ClientCartesActivity.class)); finish(); } else if(id==R.id.nav_coaching){ startActivity(new android.content.Intent(this, ClientCoachingActivity.class)); finish(); } else if(id==R.id.nav_cours){ startActivity(new android.content.Intent(this, ClientCollectiveClassesActivity.class)); finish(); } else if(id==R.id.nav_notifications){ startActivity(new android.content.Intent(this, ClientNotificationsActivity.class)); finish(); } else if(id==R.id.nav_logout){ new SessionManager(this).logout(); startActivity(new android.content.Intent(this, LandingActivity.class)); finish(); } }
    private void load(){ progress.setVisibility(View.VISIBLE); msg.setVisibility(View.GONE); Executors.newSingleThreadExecutor().execute(() -> { try { long userId=new SessionManager(this).getUserId(); JSONObject u=new ApiClient(this).getAuthJson("api/Utilisateurs/"+userId); boolean ab=u.optBoolean("abonneNewsletter", false); newsletter=ab; runOnUiThread(() -> { nom.setText(u.optString("nom")); prenom.setText(u.optString("prenom")); email.setText(u.optString("email")); tel.setText(u.optString("telephone")); adr.setText(u.optString("adresse")); if(swNewsletter!=null) swNewsletter.setChecked(ab); progress.setVisibility(View.GONE); }); loadPhoto(userId); } catch(Exception e){ runOnUiThread(() -> { progress.setVisibility(View.GONE); msg.setText("Impossible de charger"); msg.setVisibility(View.VISIBLE); }); } }); }
    private void update(){ progress.setVisibility(View.VISIBLE); msg.setVisibility(View.GONE); Executors.newSingleThreadExecutor().execute(() -> { try { long userId=new SessionManager(this).getUserId(); JSONObject body=new JSONObject().put("nom", nom.getText().toString()).put("prenom", prenom.getText().toString()).put("telephone", tel.getText().toString()).put("adresse", adr.getText().toString()); new ApiClient(this).putAuthJson("api/Utilisateurs/"+userId, body); runOnUiThread(() -> { progress.setVisibility(View.GONE); msg.setText("Profil mis à jour"); msg.setVisibility(View.VISIBLE); }); } catch(Exception e){ runOnUiThread(() -> { progress.setVisibility(View.GONE); msg.setText("Échec de mise à jour"); msg.setVisibility(View.VISIBLE); }); } }); }
    private void toggleNewsletter(boolean checked){ Executors.newSingleThreadExecutor().execute(() -> { try { long userId=new SessionManager(this).getUserId(); new ApiClient(this).putAuthJson("api/Utilisateurs/"+userId+"/newsletter/"+checked, new JSONObject()); runOnUiThread(() -> { newsletter=checked; android.widget.Toast.makeText(this, "Préférence newsletter mise à jour", android.widget.Toast.LENGTH_SHORT).show(); }); } catch(Exception e){ runOnUiThread(() -> { if(swNewsletter!=null) swNewsletter.setChecked(newsletter); android.widget.Toast.makeText(this, "Erreur mise à jour newsletter", android.widget.Toast.LENGTH_SHORT).show(); }); } }); }
    private void openPhotoPicker(){ android.content.Intent intent=new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT); intent.setType("image/*"); startActivityForResult(android.content.Intent.createChooser(intent, "Choisir une photo"), REQ_PICK_IMAGE); }
    @Override protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data){ super.onActivityResult(requestCode, resultCode, data); if(requestCode==REQ_PICK_IMAGE && resultCode==android.app.Activity.RESULT_OK && data!=null){ android.net.Uri uri=data.getData(); if(uri!=null){ uploadSelectedPhoto(uri); } } }
    private void uploadSelectedPhoto(android.net.Uri uri){ try{ String mime=getContentResolver().getType(uri); if(mime==null){ String ext=android.webkit.MimeTypeMap.getFileExtensionFromUrl(uri.toString()); mime=android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext); } java.io.InputStream is=getContentResolver().openInputStream(uri); byte[] bytes=readAll(is); is.close(); long id=new SessionManager(this).getUserId(); final String mt=mime; final byte[] b=bytes; final long userId=id; new Thread(() -> { try { new ApiClient(this).putAuthMultipart("api/Utilisateurs/"+userId+"/photo", "file", "photo.jpg", b, mt); runOnUiThread(() -> { load(); android.widget.Toast.makeText(this, "Photo mise à jour", android.widget.Toast.LENGTH_SHORT).show(); }); } catch(Exception e){ runOnUiThread(() -> android.widget.Toast.makeText(this, "Erreur upload photo", android.widget.Toast.LENGTH_SHORT).show()); } }).start(); } catch(Exception e){ android.widget.Toast.makeText(this, "Erreur sélection photo", android.widget.Toast.LENGTH_SHORT).show(); } }
    private byte[] readAll(java.io.InputStream is) throws Exception { java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream(); byte[] buf=new byte[8192]; int r; while((r=is.read(buf))!=-1){ baos.write(buf,0,r);} return baos.toByteArray(); }
    private void loadPhoto(long userId){ Executors.newSingleThreadExecutor().execute(() -> { try { String base=jnb.fitness.UrlConfig.getApiBaseUrl(this); java.net.URL url=new java.net.URL(base+"api/Utilisateurs/"+userId+"/photo"); java.net.HttpURLConnection conn=(java.net.HttpURLConnection) url.openConnection(); conn.setConnectTimeout(8000); conn.setReadTimeout(15000); conn.setRequestMethod("GET"); String token=new SessionManager(this).getToken(); if(token!=null) conn.setRequestProperty("Authorization","Bearer "+token); conn.setRequestProperty("Accept","image/*"); android.graphics.Bitmap bm=android.graphics.BitmapFactory.decodeStream(conn.getInputStream()); conn.disconnect(); if(bm!=null && photo!=null){ photo.post(() -> photo.setImageBitmap(bm)); } } catch(Exception ignored){} }); }
    private void openChangePasswordDialog(){ android.view.View view=android.view.LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null, false); final com.google.android.material.textfield.TextInputEditText etOld=view.findViewById(R.id.et_old); final com.google.android.material.textfield.TextInputEditText etNew=view.findViewById(R.id.et_new); final com.google.android.material.textfield.TextInputEditText etConfirm=view.findViewById(R.id.et_confirm); new com.google.android.material.dialog.MaterialAlertDialogBuilder(this).setView(view).setNegativeButton("Annuler", (d,w)-> d.dismiss()).setPositiveButton("Enregistrer", (d,w)-> { String newPwd=String.valueOf(etNew.getText()); String confirm=String.valueOf(etConfirm.getText()); if(newPwd==null || newPwd.isEmpty() || !newPwd.equals(confirm)){ android.widget.Toast.makeText(this, "Le nouveau mot de passe ne correspond pas", android.widget.Toast.LENGTH_SHORT).show(); return; } Executors.newSingleThreadExecutor().execute(() -> { try { long id=new SessionManager(this).getUserId(); JSONObject body=new JSONObject().put("ancienMotDePasse", String.valueOf(etOld.getText())).put("nouveauMotDePasse", newPwd); new ApiClient(this).postAuthJson("api/Auth/change-password/"+id, body); runOnUiThread(() -> android.widget.Toast.makeText(this, "Mot de passe modifié", android.widget.Toast.LENGTH_SHORT).show()); } catch(Exception e){ runOnUiThread(() -> android.widget.Toast.makeText(this, "Erreur de modification", android.widget.Toast.LENGTH_SHORT).show()); } }); }).show(); }
}
