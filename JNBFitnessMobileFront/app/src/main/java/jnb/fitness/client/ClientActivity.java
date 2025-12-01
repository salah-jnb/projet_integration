package jnb.fitness.client;
import jnb.fitness.R;
import jnb.fitness.SessionManager;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class ClientActivity extends AppCompatActivity {
    private DrawerLayout drawer;
    private NavigationView nav;
    private ViewGroup content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_client);
        drawer = findViewById(R.id.drawer);
        nav = findViewById(R.id.nav);
        content = findViewById(R.id.content);

        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        nav.setNavigationItemSelectedListener(item -> {
            drawer.closeDrawers();
            handle(item);
            return true;
        });

        jnb.fitness.NavigationHeaderUtil.applyForClient(this);

        handle(nav.getMenu().findItem(R.id.nav_dashboard));
    }

    private void handle(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_dashboard) {
            startActivity(new android.content.Intent(this, ClientDashboardActivity.class));
            finish();
        } else if (id == R.id.nav_profil) {
            startActivity(new android.content.Intent(this, ClientProfileActivity.class));
        } else if (id == R.id.nav_articles) {
            startActivity(new android.content.Intent(this, ClientArticlesActivity.class));
        } else if (id == R.id.nav_abonnements) {
            startActivity(new android.content.Intent(this, ClientAbonnementsActivity.class));
        } else if (id == R.id.nav_carte) {
            startActivity(new android.content.Intent(this, ClientCartesActivity.class));
        } else if (id == R.id.nav_coaching) {
            startActivity(new android.content.Intent(this, ClientCoachingActivity.class));
        } else if (id == R.id.nav_cours) {
            startActivity(new android.content.Intent(this, ClientCollectiveClassesActivity.class));
        } else if (id == R.id.nav_produits) {
            startActivity(new android.content.Intent(this, ClientProductsActivity.class));
        } else if (id == R.id.nav_parrainage) {
            startActivity(new android.content.Intent(this, ClientReferralsActivity.class));
        } else if (id == R.id.nav_notifications) {
            startActivity(new android.content.Intent(this, ClientNotificationsActivity.class));
        } else if (id == R.id.nav_logout) {
            new SessionManager(this).logout();
            finish();
        }
    }
}
