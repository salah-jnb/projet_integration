package jnb.fitness;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "jnb_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NOM = "nom";
    private static final String KEY_PRENOM = "prenom";
    private static final String KEY_TYPE = "type";

    private final SharedPreferences prefs;

    public SessionManager(Context ctx) {
        this.prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void save(String token, long utilisateurId, String email, String nom, String prenom, String typeUtilisateur) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putLong(KEY_USER_ID, utilisateurId)
                .putString(KEY_EMAIL, email)
                .putString(KEY_NOM, nom)
                .putString(KEY_PRENOM, prenom)
                .putString(KEY_TYPE, typeUtilisateur)
                .apply();
    }

    public String getToken() { return prefs.getString(KEY_TOKEN, null); }
    public long getUserId() { return prefs.getLong(KEY_USER_ID, 0); }
    public String getEmail() { return prefs.getString(KEY_EMAIL, null); }
    public String getNom() { return prefs.getString(KEY_NOM, null); }
    public String getPrenom() { return prefs.getString(KEY_PRENOM, null); }
    public String getType() { return prefs.getString(KEY_TYPE, null); }
    public boolean isLoggedIn() { return getToken() != null; }
    public void logout() { prefs.edit().clear().apply(); }
}