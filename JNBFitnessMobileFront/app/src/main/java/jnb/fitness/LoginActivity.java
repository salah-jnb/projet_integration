package jnb.fitness;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton; private Button registerButton;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.input_email);
        passwordInput = findViewById(R.id.input_password);
        loginButton = findViewById(R.id.btn_login);
        registerButton = findViewById(R.id.btn_register);
        progress = findViewById(R.id.progress);

        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) redirect(session.getType());

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email et mot de passe requis", Toast.LENGTH_SHORT).show();
                return;
            }
            progress.setVisibility(ProgressBar.VISIBLE);
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    ApiClient api = new ApiClient(this);
                    JSONObject body = new JSONObject()
                            .put("email", email)
                            .put("motDePasse", password);
                    JSONObject res = api.post("api/Auth/login", body);
                    String token = res.getString("token");
                    long userId = res.getLong("utilisateurId");
                    String type = res.getString("typeUtilisateur");
                    String nom = res.optString("nom");
                    String prenom = res.optString("prenom");
                    new SessionManager(this).save(token, userId, email, nom, prenom, type);
                    runOnUiThread(() -> {
                        progress.setVisibility(ProgressBar.INVISIBLE);
                        redirect(type);
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        progress.setVisibility(ProgressBar.INVISIBLE);
                        Toast.makeText(this, "Échec de connexion", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

        registerButton.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void redirect(String type) {
        if (type == null) {
            Toast.makeText(this, "Type utilisateur introuvable", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent;
        switch (type) {
            case "ADMINISTRATEUR":
                intent = new Intent(this, jnb.fitness.admin.AdminActivity.class);
                break;
            case "COACH":
                intent = new Intent(this, jnb.fitness.coach.CoachActivity.class);
                break;
            default:
                intent = new Intent(this, jnb.fitness.client.ClientActivity.class);
                break;
        }
        startActivity(intent);
        finish();
    }
}
