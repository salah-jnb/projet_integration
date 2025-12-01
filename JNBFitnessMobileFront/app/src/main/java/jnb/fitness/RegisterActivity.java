package jnb.fitness;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {
    private EditText etPrenom, etNom, etEmail, etPassword, etTelephone, etAdresse, etCode;
    private android.widget.Button btnSubmit, btnLogin;
    private ProgressBar progress;

    @Override protected void onCreate(Bundle savedInstanceState){ super.onCreate(savedInstanceState); setContentView(R.layout.activity_register); etPrenom=findViewById(R.id.input_prenom); etNom=findViewById(R.id.input_nom); etEmail=findViewById(R.id.input_email); etPassword=findViewById(R.id.input_password); etTelephone=findViewById(R.id.input_telephone); etAdresse=findViewById(R.id.input_adresse); etCode=findViewById(R.id.input_code); btnSubmit=findViewById(R.id.btn_register); btnLogin=findViewById(R.id.btn_login); progress=findViewById(R.id.progress); btnSubmit.setOnClickListener(v-> submit()); btnLogin.setOnClickListener(v-> startActivity(new Intent(this, LoginActivity.class))); }

    private void submit(){ String prenom=etPrenom.getText().toString().trim(); String nom=etNom.getText().toString().trim(); String email=etEmail.getText().toString().trim(); String pass=etPassword.getText().toString().trim(); String tel=etTelephone.getText().toString().trim(); String adr=etAdresse.getText().toString().trim(); String code=(etCode.getText().toString()!=null? etCode.getText().toString().trim().toUpperCase(): ""); if(prenom.isEmpty()||nom.isEmpty()||email.isEmpty()||pass.length()<6||tel.length()<8||adr.length()<5){ Toast.makeText(this, "Veuillez remplir tous les champs correctement", Toast.LENGTH_SHORT).show(); return; } progress.setVisibility(View.VISIBLE); btnSubmit.setEnabled(false); Executors.newSingleThreadExecutor().execute(() -> { try { ApiClient api=new ApiClient(this); // Vérif code parrainage (optionnel)
            String parrainIdStr=null; if(code!=null && !code.isEmpty()){ try{ JSONObject cli=api.getAuthJson("api/Clients/parrainage/"+code); parrainIdStr=String.valueOf(cli.optLong("utilisateurId")); } catch(Exception ignored){ runOnUiThread(() -> Toast.makeText(this, "Code de parrainage invalide", Toast.LENGTH_SHORT).show()); progress.setVisibility(View.INVISIBLE); btnSubmit.setEnabled(true); return; } }

            // Inscription
            JSONObject body=new JSONObject().put("email", email).put("motDePasse", pass).put("nom", nom).put("prenom", prenom).put("telephone", tel).put("adresse", adr).put("codeParrainage", code);
            JSONObject res=api.post("api/Auth/register", body);
            String token=res.optString("token"); long userId=res.optLong("utilisateurId"); String type=res.optString("typeUtilisateur"); String respNom=res.optString("nom"); String respPrenom=res.optString("prenom"); new SessionManager(this).save(token, userId, email, respNom, respPrenom, type);

            // Créer parrainage si code valable
            if(parrainIdStr!=null){ try{ long parrainId=Long.parseLong(parrainIdStr); JSONObject p=new JSONObject().put("parrainId", parrainId).put("filleulId", userId); api.postAuthJson("api/Parrainages", p); } catch(Exception ignored){} }

            runOnUiThread(() -> { progress.setVisibility(View.INVISIBLE); btnSubmit.setEnabled(true); redirect(type); });
        } catch(Exception e){ runOnUiThread(() -> { progress.setVisibility(View.INVISIBLE); btnSubmit.setEnabled(true); Toast.makeText(this, "Échec d'inscription", Toast.LENGTH_SHORT).show(); }); } }); }

    private void redirect(String type){ if(type==null||type.isEmpty()){ Toast.makeText(this, "Type utilisateur introuvable", Toast.LENGTH_SHORT).show(); return; } Intent intent; if("ADMINISTRATEUR".equals(type)) intent=new Intent(this, jnb.fitness.admin.AdminActivity.class); else if("COACH".equals(type)) intent=new Intent(this, jnb.fitness.coach.CoachActivity.class); else intent=new Intent(this, jnb.fitness.client.ClientActivity.class); startActivity(intent); finish(); }
}

