package jnb.fitness.coach;
import jnb.fitness.R;
import jnb.fitness.ApiClient;
import jnb.fitness.SessionManager;
import jnb.fitness.LandingActivity;
import jnb.fitness.UrlConfig;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.net.Uri;
import android.content.Intent;
import android.provider.MediaStore;
import android.database.Cursor;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

public class CoachProfileActivity extends AppCompatActivity {
    private ProgressBar progress; private TextView email,note,avis; private EditText prenom,nom,specialites,description; private ImageView photo; private Button save; private ImageView btnChangePhoto; private Button btnChangePassword;
    private JSONObject details;
    private static final int REQ_PICK_IMAGE = 5012;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_coach);
        android.view.ViewGroup content = findViewById(R.id.content);
        android.view.View page = android.view.LayoutInflater.from(this).inflate(R.layout.activity_coach_profile, content, false);
        content.addView(page);
        progress = page.findViewById(R.id.progress);
        prenom = page.findViewById(R.id.prenom);
        nom = page.findViewById(R.id.nom);
        email = page.findViewById(R.id.email);
        note = page.findViewById(R.id.note);
        avis = page.findViewById(R.id.avis);
        specialites = page.findViewById(R.id.specialites);
        description = page.findViewById(R.id.description);
        photo = page.findViewById(R.id.photo);
        btnChangePhoto = page.findViewById(R.id.btn_change_photo);
        btnChangePassword = page.findViewById(R.id.btn_change_password);
        save = page.findViewById(R.id.btn_save);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        androidx.drawerlayout.widget.DrawerLayout drawer = findViewById(R.id.drawer);
        androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        com.google.android.material.navigation.NavigationView nav = findViewById(R.id.nav);
        nav.setNavigationItemSelectedListener(item -> { drawer.closeDrawers(); route(item.getItemId()); return true; });
        jnb.fitness.NavigationHeaderUtil.applyForCoach(this);
        save.setOnClickListener(v -> saveProfile());
        if(btnChangePhoto!=null){ btnChangePhoto.setOnClickListener(v -> openPhotoPicker()); }
        if(btnChangePassword!=null){ btnChangePassword.setOnClickListener(v -> openChangePasswordDialog()); }
        loadDetails();
    }
    private void route(int id){ if(id==R.id.nav_dashboard){ startActivity(new android.content.Intent(this, CoachActivity.class)); finish(); } else if(id==R.id.nav_profil){} else if(id==R.id.nav_disponibilites){ startActivity(new android.content.Intent(this, CoachDisponibilitesActivity.class)); finish(); } else if(id==R.id.nav_reservations){ startActivity(new android.content.Intent(this, CoachReservationsActivity.class)); finish(); } else if(id==R.id.nav_cours){ startActivity(new android.content.Intent(this, CoachCollectiveClassesActivity.class)); finish(); } else if(id==R.id.nav_articles){ startActivity(new android.content.Intent(this, CoachArticlesActivity.class)); finish(); } else if(id==R.id.nav_produits){ startActivity(new android.content.Intent(this, CoachProductsActivity.class)); finish(); } else if(id==R.id.nav_notifications){ startActivity(new android.content.Intent(this, CoachNotificationsActivity.class)); finish(); } else if(id==R.id.nav_logout){ new SessionManager(this).logout(); startActivity(new android.content.Intent(this, LandingActivity.class)); finish(); } }
    private void loadDetails(){
        progress.setVisibility(View.VISIBLE);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                long id=new SessionManager(this).getUserId();
                JSONObject d=new ApiClient(this).getAuthJson("api/Coachs/"+id+"/details");
                details=d;
                runOnUiThread(this::bindDetails);
            } catch(Exception e){
                runOnUiThread(() -> progress.setVisibility(View.GONE));
            }
        });
    }

    private void bindDetails(){
        progress.setVisibility(View.GONE);
        if(details==null) return;
        String nomVal=details.optString("nom",""), prenomVal=details.optString("prenom",""), mail=details.optString("email",""), spec=details.optString("specialites",""), desc=details.optString("description","" );
        prenom.setText(prenomVal);
        nom.setText(nomVal);
        email.setText(mail);
        specialites.setText(spec);
        description.setText(desc);
        double noteVal=details.optDouble("noteGlobale",0);
        int avisCount=details.optInt("nombreAvis",0);
        note.setText(String.valueOf(noteVal));
        avis.setText(avisCount>0? ("Sur "+avisCount+" avis") : "");
        long userId=new SessionManager(this).getUserId();
        loadPhoto(userId);
    }

    private void loadPhoto(long userId){
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String base= UrlConfig.getApiBaseUrl(this);
                URL url=new URL(base+"api/Utilisateurs/"+userId+"/photo");
                HttpURLConnection conn=(HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept","image/*");
                String token=new SessionManager(this).getToken();
                if(token!=null){ conn.setRequestProperty("Authorization","Bearer "+token); }
                int code=conn.getResponseCode();
                if(code>=200 && code<300){
                    Bitmap bm=BitmapFactory.decodeStream(conn.getInputStream());
                    if(bm!=null){ photo.post(() -> photo.setImageBitmap(bm)); }
                }
                conn.disconnect();
            } catch(Exception ignored){}
        });
    }

    private void saveProfile(){
        String spec=specialites.getText().toString();
        String desc=description.getText().toString();
        String nomV=nom.getText().toString();
        String prenomV=prenom.getText().toString();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                long id=new SessionManager(this).getUserId();
                JSONObject bodyCoach=new JSONObject().put("Specialites", spec).put("Description", desc);
                new ApiClient(this).putAuthJson("api/Coachs/"+id, bodyCoach);
                JSONObject bodyUser=new JSONObject().put("Nom", nomV).put("Prenom", prenomV);
                new ApiClient(this).putAuthJson("api/Utilisateurs/"+id, bodyUser);
                runOnUiThread(this::loadDetails);
            } catch(Exception ignored){}
        });
    }

    private void openPhotoPicker(){ Intent intent=new Intent(Intent.ACTION_GET_CONTENT); intent.setType("image/*"); startActivityForResult(Intent.createChooser(intent, "Choisir une photo"), REQ_PICK_IMAGE); }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data){ super.onActivityResult(requestCode, resultCode, data); if(requestCode==REQ_PICK_IMAGE && resultCode==android.app.Activity.RESULT_OK && data!=null){ Uri uri=data.getData(); if(uri!=null){ uploadSelectedPhoto(uri); } } }

    private void uploadSelectedPhoto(Uri uri){ try{ String mime=getContentResolver().getType(uri); if(mime==null){ String ext=MimeTypeMap.getFileExtensionFromUrl(uri.toString()); mime=MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext); } java.io.InputStream is=getContentResolver().openInputStream(uri); byte[] bytes=readAll(is); is.close(); long id=new SessionManager(this).getUserId(); final String mt=mime; final byte[] b=bytes; final long userId=id; new Thread(() -> { try { new ApiClient(this).putAuthMultipart("api/Utilisateurs/"+userId+"/photo", "file", "photo.jpg", b, mt); runOnUiThread(() -> { loadDetails(); Toast.makeText(this, "Photo mise à jour", Toast.LENGTH_SHORT).show(); }); } catch(Exception e){ runOnUiThread(() -> Toast.makeText(this, "Erreur upload photo", Toast.LENGTH_SHORT).show()); } }).start(); } catch(Exception e){ Toast.makeText(this, "Erreur sélection photo", Toast.LENGTH_SHORT).show(); } }

    private byte[] readAll(java.io.InputStream is) throws Exception { java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream(); byte[] buf=new byte[8192]; int r; while((r=is.read(buf))!=-1){ baos.write(buf,0,r);} return baos.toByteArray(); }

    private void openChangePasswordDialog(){ android.view.View view=android.view.LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null, false); final com.google.android.material.textfield.TextInputEditText etOld=view.findViewById(R.id.et_old); final com.google.android.material.textfield.TextInputEditText etNew=view.findViewById(R.id.et_new); final com.google.android.material.textfield.TextInputEditText etConfirm=view.findViewById(R.id.et_confirm); new com.google.android.material.dialog.MaterialAlertDialogBuilder(this).setView(view).setNegativeButton("Annuler", (d,w)-> d.dismiss()).setPositiveButton("Enregistrer", (d,w)-> { String newPwd=String.valueOf(etNew.getText()); String confirm=String.valueOf(etConfirm.getText()); if(newPwd==null || newPwd.isEmpty() || !newPwd.equals(confirm)){ Toast.makeText(this, "Le nouveau mot de passe ne correspond pas", Toast.LENGTH_SHORT).show(); return; } Executors.newSingleThreadExecutor().execute(() -> { try { long id=new SessionManager(this).getUserId(); JSONObject body=new JSONObject().put("ancienMotDePasse", String.valueOf(etOld.getText())).put("nouveauMotDePasse", newPwd); new ApiClient(this).postAuthJson("api/Auth/change-password/"+id, body); runOnUiThread(() -> Toast.makeText(this, "Mot de passe modifié", Toast.LENGTH_SHORT).show()); } catch(Exception e){ runOnUiThread(() -> Toast.makeText(this, "Erreur de modification", Toast.LENGTH_SHORT).show()); } }); }).show(); }

    private String normalizeIsoOffset(String iso){ if(iso==null) return null; return iso.replaceFirst("([+-]\\d{2}):(\\d{2})$","$1$2"); }
}
