package jnb.fitness.admin;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import jnb.fitness.R;
import jnb.fitness.SessionManager;

public class AdminActivity extends AppCompatActivity {
    private DrawerLayout drawer;
    private NavigationView nav;
    private ViewGroup content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_admin);
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


        handle(nav.getMenu().findItem(R.id.nav_dashboard));
    }

    private void handle(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_dashboard) {
            startActivity(new android.content.Intent(this, AdminDashboardActivity.class));
            finish();
        } else if (id == R.id.nav_users) {
            startActivity(new android.content.Intent(this, AdminUsersActivity.class));
        } else if (id == R.id.nav_abonnements) {
            startActivity(new android.content.Intent(this, AdminAbonnementsActivity.class));
        } else if (id == R.id.nav_cours) {
            startActivity(new android.content.Intent(this, AdminCoursActivity.class));
        } else if (id == R.id.nav_articles) {
            startActivity(new android.content.Intent(this, AdminArticlesActivity.class));
        } else if (id == R.id.nav_produits) {
            startActivity(new android.content.Intent(this, AdminProduitsActivity.class));
        } else if (id == R.id.nav_paiements) {
            startActivity(new android.content.Intent(this, AdminPaiementsActivity.class));
        } else if (id == R.id.nav_cartes) {
            startActivity(new android.content.Intent(this, AdminCartesActivity.class));
        } else if (id == R.id.nav_logout) {
            new SessionManager(this).logout();
            finish();
        }
    }
}
