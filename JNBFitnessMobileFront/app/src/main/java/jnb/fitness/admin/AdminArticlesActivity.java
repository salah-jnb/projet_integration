package jnb.fitness.admin;
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

public class AdminArticlesActivity extends AppCompatActivity implements AdminArticlesAdapter.Callbacks {
    private RecyclerView list;
    private ProgressBar progress;
    private TextView empty;
    private final ArrayList<JSONObject> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_admin);
        android.view.ViewGroup content = findViewById(R.id.content);
        android.view.LayoutInflater.from(this).inflate(R.layout.activity_admin_articles, content, true);
        list = content.findViewById(R.id.list);
        progress = content.findViewById(R.id.progress);
        empty = content.findViewById(R.id.empty);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(new AdminArticlesAdapter(data, this));
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        androidx.drawerlayout.widget.DrawerLayout drawer = findViewById(R.id.drawer);
        androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        com.google.android.material.navigation.NavigationView nav = findViewById(R.id.nav);
        nav.setNavigationItemSelectedListener(item -> { drawer.closeDrawers(); route(item.getItemId()); return true; });
        load();
    }

    private void route(int id) {
        if (id == R.id.nav_dashboard) { startActivity(new android.content.Intent(this, AdminDashboardActivity.class)); finish(); }
        else if (id == R.id.nav_users) { startActivity(new android.content.Intent(this, AdminUsersActivity.class)); finish(); }
        else if (id == R.id.nav_abonnements) { startActivity(new android.content.Intent(this, AdminAbonnementsActivity.class)); finish(); }
        else if (id == R.id.nav_cours) { startActivity(new android.content.Intent(this, AdminCoursActivity.class)); finish(); }
        else if (id == R.id.nav_articles) {}
        else if (id == R.id.nav_produits) { startActivity(new android.content.Intent(this, AdminProduitsActivity.class)); finish(); }
        else if (id == R.id.nav_paiements) { startActivity(new android.content.Intent(this, AdminPaiementsActivity.class)); finish(); }
        else if (id == R.id.nav_cartes) { startActivity(new android.content.Intent(this, AdminCartesActivity.class)); finish(); }
        else if (id == R.id.nav_logout) { new SessionManager(this).logout(); startActivity(new android.content.Intent(this, LandingActivity.class)); finish(); }
    }

    private void load() {
        progress.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ApiClient api = new ApiClient(this);
                JSONArray arr = api.getAuthArray("api/Articles/en-attente");
                int oldSize = data.size();
                data.clear();
                for (int i = 0; i < arr.length(); i++) data.add(arr.getJSONObject(i));
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    empty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                    RecyclerView.Adapter<?> a = list.getAdapter();
                    if (a != null) a.notifyDataSetChanged();
                });
            } catch (Exception e) {
                runOnUiThread(() -> { progress.setVisibility(View.GONE); empty.setVisibility(View.VISIBLE); });
            }
        });
    }

    @Override
    public void onValidate(long id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try { new ApiClient(this).putAuthJson("api/Articles/" + id + "/valider", new JSONObject()); load(); } catch (Exception ignored) {}
        });
    }

    @Override
    public void onReject(long id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try { new ApiClient(this).deleteAuth("api/Articles/" + id); load(); } catch (Exception ignored) {}
        });
    }

    @Override
    public void onView(long id) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JSONObject art = new ApiClient(this).getAuthJson("api/Articles/" + id);
                runOnUiThread(() -> {
                    android.widget.ScrollView sv = new android.widget.ScrollView(this);
                    android.widget.LinearLayout ll = new android.widget.LinearLayout(this);
                    ll.setOrientation(android.widget.LinearLayout.VERTICAL);
                    int pad = (int) (16 * getResources().getDisplayMetrics().density);
                    ll.setPadding(pad,pad,pad,0);

                    android.widget.ImageView img = new android.widget.ImageView(this);
                    img.setLayoutParams(new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, (int)(200 * getResources().getDisplayMetrics().density)));
                    img.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                    String imageUrlRaw = art.optString("imageUrl", "");
                    String imageBase64 = imageUrlRaw.isEmpty() ? art.optString("imageBase64", art.optString("image", "")) : "";
                    String resolved = null;
                    if (!imageUrlRaw.isEmpty()) {
                        String base = jnb.fitness.UrlConfig.getApiBaseUrl(this);
                        if (imageUrlRaw.startsWith("http")) {
                            resolved = imageUrlRaw;
                        } else if (imageUrlRaw.startsWith("/")) {
                            resolved = base + imageUrlRaw.substring(1);
                        } else if (imageUrlRaw.contains("uploads/")) {
                            resolved = base + (imageUrlRaw.startsWith("uploads/") ? imageUrlRaw : ("uploads/" + imageUrlRaw));
                        } else {
                            resolved = base + "uploads/articles/" + imageUrlRaw;
                        }
                    }
                    try {
                        if (resolved != null) { com.bumptech.glide.Glide.with(this).load(resolved).into(img); }
                        else if (!imageBase64.isEmpty()) {
                            String data = imageBase64.startsWith("data:")? imageBase64.substring(imageBase64.indexOf(",")+1): imageBase64;
                            byte[] bytes = android.util.Base64.decode(data, android.util.Base64.DEFAULT);
                            android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                            img.setImageBitmap(bmp);
                        }
                    } catch (Exception ignored) {}

                    android.widget.TextView t = new android.widget.TextView(this);
                    t.setText(art.optString("titre")); t.setTextColor(getResources().getColor(R.color.white)); t.setTextSize(18);
                    android.widget.TextView meta = new android.widget.TextView(this);
                    meta.setText("Par " + art.optString("coachPrenom","") + " " + art.optString("coachNom","")); meta.setTextColor(getResources().getColor(R.color.text_secondary_dark));
                    android.widget.TextView c = new android.widget.TextView(this);
                    c.setText(art.optString("contenu")); c.setTextColor(getResources().getColor(R.color.white));

                    ll.addView(img);
                    ll.addView(t);
                    ll.addView(meta);
                    ll.addView(c);
                    sv.addView(ll);
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                            .setTitle("Article")
                            .setView(sv)
                            .setPositiveButton("Fermer", (d,w)-> d.dismiss())
                            .show();
                });
            } catch(Exception ignored){}
        });
    }
}
