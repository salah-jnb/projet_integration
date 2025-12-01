package jnb.fitness;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

public class NavigationHeaderUtil {
    public static void applyForClient(Activity activity){
        NavigationView nav = activity.findViewById(R.id.nav);
        if(nav == null) return;
        View existing = null;
        try { existing = nav.getHeaderView(0); } catch(Exception ignored){}
        if(existing != null){ try { nav.removeHeaderView(existing); } catch(Exception ignored){} }
        LinearLayout container = new LinearLayout(activity);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        int pad = dp(activity, 16);
        container.setPadding(pad, pad, pad, pad);

        ImageView avatar = new ImageView(activity);
        avatar.setImageResource(R.drawable.ic_profile_placeholder);
        LinearLayout.LayoutParams lpAvatar = new LinearLayout.LayoutParams(dp(activity, 48), dp(activity, 48));
        lpAvatar.rightMargin = dp(activity, 12);
        container.addView(avatar, lpAvatar);

        LinearLayout textCol = new LinearLayout(activity);
        textCol.setOrientation(LinearLayout.VERTICAL);
        TextView name = new TextView(activity);
        name.setTextColor(activity.getResources().getColor(R.color.white));
        name.setTextSize(18);
        name.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        SessionManager sm = new SessionManager(activity);
        String fullName = ((sm.getPrenom()!=null? sm.getPrenom(): "") + " " + (sm.getNom()!=null? sm.getNom(): "")).trim();
        if(fullName.isEmpty()) fullName = "Client";
        name.setText(fullName);
        textCol.addView(name);
        container.addView(textCol, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        nav.addHeaderView(container);
        loadUserPhotoInto(activity, avatar, sm.getUserId());
    }

    public static void applyForCoach(Activity activity){
        NavigationView nav = activity.findViewById(R.id.nav);
        if(nav == null) return;
        View existing = null;
        try { existing = nav.getHeaderView(0); } catch(Exception ignored){}
        if(existing != null){ try { nav.removeHeaderView(existing); } catch(Exception ignored){} }
        LinearLayout container = new LinearLayout(activity);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        int pad = dp(activity, 16);
        container.setPadding(pad, pad, pad, pad);

        ImageView avatar = new ImageView(activity);
        avatar.setImageResource(R.drawable.ic_profile_placeholder);
        LinearLayout.LayoutParams lpAvatar = new LinearLayout.LayoutParams(dp(activity, 48), dp(activity, 48));
        lpAvatar.rightMargin = dp(activity, 12);
        container.addView(avatar, lpAvatar);

        LinearLayout textCol = new LinearLayout(activity);
        textCol.setOrientation(LinearLayout.VERTICAL);
        TextView name = new TextView(activity);
        name.setTextColor(activity.getResources().getColor(R.color.white));
        name.setTextSize(18);
        name.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        SessionManager sm = new SessionManager(activity);
        String fullName = ((sm.getPrenom()!=null? sm.getPrenom(): "") + " " + (sm.getNom()!=null? sm.getNom(): "")).trim();
        if(fullName.isEmpty()) fullName = "Coach";
        name.setText(fullName);
        TextView spec = new TextView(activity);
        spec.setTextColor(activity.getResources().getColor(R.color.text_secondary));
        spec.setTextSize(13);
        TextView rating = new TextView(activity);
        rating.setTextColor(activity.getResources().getColor(R.color.jnb_orange));
        rating.setTextSize(14);
        textCol.addView(name);
        textCol.addView(spec);
        textCol.addView(rating);
        container.addView(textCol, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        nav.addHeaderView(container);
        loadUserPhotoInto(activity, avatar, sm.getUserId());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ApiClient api = new ApiClient(activity);
                long id = sm.getUserId();
                JSONObject d = api.getAuthJson("api/Coachs/"+id+"/details");
                String s = d.optString("specialites", "");
                double note = d.optDouble("noteGlobale", 0);
                int avis = d.optInt("nombreAvis", 0);
                final String specText = s!=null? s: "";
                final String ratingText = (note>0? String.format(java.util.Locale.getDefault(), "\u2605 %.1f", note): "") + (avis>0? (" ("+avis+" avis)"): "");
                activity.runOnUiThread(() -> { spec.setText(specText); rating.setText(ratingText.trim()); });
            } catch(Exception ignored){}
        });
    }

    private static void loadUserPhotoInto(Activity activity, ImageView into, long userId){
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String base = UrlConfig.getApiBaseUrl(activity);
                URL url = new URL(base+"api/Utilisateurs/"+userId+"/photo");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept","image/*");
                String token = new SessionManager(activity).getToken();
                if(token!=null){ conn.setRequestProperty("Authorization","Bearer "+token); }
                int code = conn.getResponseCode();
                if(code>=200 && code<300){
                    Bitmap bm = BitmapFactory.decodeStream(conn.getInputStream());
                    if(bm!=null){ into.post(() -> into.setImageBitmap(bm)); }
                }
                conn.disconnect();
            } catch(Exception ignored){}
        });
    }

    private static int dp(Activity a, int d){ float scale = a.getResources().getDisplayMetrics().density; return (int)(d * scale + 0.5f); }
}

