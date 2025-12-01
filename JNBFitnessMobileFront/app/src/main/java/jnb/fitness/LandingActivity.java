package jnb.fitness;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LandingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            String type = session.getType();
            Intent intent;
            if ("ADMINISTRATEUR".equals(type)) intent = new Intent(this, jnb.fitness.admin.AdminActivity.class);
            else if ("COACH".equals(type)) intent = new Intent(this, jnb.fitness.coach.CoachActivity.class);
            else intent = new Intent(this, jnb.fitness.client.ClientActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        Button start = findViewById(R.id.btn_start);
        Button login = findViewById(R.id.btn_login);
        start.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        login.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
    }
}
