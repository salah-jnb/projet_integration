package jnb.fitness.coach;
import jnb.fitness.R;
import jnb.fitness.SessionManager;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.widget.Toolbar;

public class CoachActivity extends AppCompatActivity {
    private DrawerLayout drawer;
    private NavigationView nav;
    private ViewGroup content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_coach);
        drawer = findViewById(R.id.drawer);
        nav = findViewById(R.id.nav);
        content = findViewById(R.id.content);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        nav.setNavigationItemSelectedListener(item -> {
            drawer.closeDrawers();
            handle(item);
            return true;
        });

        jnb.fitness.NavigationHeaderUtil.applyForCoach(this);

        handle(nav.getMenu().findItem(R.id.nav_dashboard));
    }

    private void handle(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_dashboard) {
            startActivity(new android.content.Intent(this, CoachDashboardActivity.class));
            finish();
        } else if (id == R.id.nav_profil) {
            startActivity(new android.content.Intent(this, CoachProfileActivity.class));
        } else if (id == R.id.nav_reservations) {
            startActivity(new android.content.Intent(this, CoachReservationsActivity.class));
        } else if (id == R.id.nav_disponibilites) {
            startActivity(new android.content.Intent(this, CoachDisponibilitesActivity.class));
        } else if (id == R.id.nav_cours) {
            startActivity(new android.content.Intent(this, CoachCollectiveClassesActivity.class));
        } else if (id == R.id.nav_articles) {
            startActivity(new android.content.Intent(this, CoachArticlesActivity.class));
        } else if (id == R.id.nav_produits) {
            startActivity(new android.content.Intent(this, CoachProductsActivity.class));
        } else if (id == R.id.nav_notifications) {
            startActivity(new android.content.Intent(this, CoachNotificationsActivity.class));
        } else if (id == R.id.nav_logout) {
            new SessionManager(this).logout();
            finish();
        }
    }
}
