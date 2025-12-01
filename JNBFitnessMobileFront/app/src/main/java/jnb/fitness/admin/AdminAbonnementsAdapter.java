package jnb.fitness.admin;
import jnb.fitness.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.List;

public class AdminAbonnementsAdapter extends RecyclerView.Adapter<AdminAbonnementsAdapter.VH> {
    public interface Callbacks { void onCancel(long id); void onEditType(JSONObject o); void onDeleteType(long id); }
    private final List<JSONObject> data;
    private final Callbacks cb;
    public AdminAbonnementsAdapter(List<JSONObject> data, Callbacks cb) { this.data = data; this.cb = cb; }

    static class VH extends RecyclerView.ViewHolder {
        TextView client; TextView title; TextView dates; TextView meta; TextView statut; Button cancel; com.google.android.material.button.MaterialButton edit; com.google.android.material.button.MaterialButton delete;
        VH(View v) { super(v); client = v.findViewById(R.id.client); title = v.findViewById(R.id.title); dates = v.findViewById(R.id.dates); meta = v.findViewById(R.id.meta); statut = v.findViewById(R.id.statut); cancel = v.findViewById(R.id.btn_cancel); edit = v.findViewById(R.id.btn_edit); delete = v.findViewById(R.id.btn_delete); }
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_abonnement, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        JSONObject o = data.get(i);
        long id = o.optLong("id");
        String nom = o.optString("nom");
        String type = o.optString("type");
        String desc = o.optString("description");
        int duree = o.optInt("dureeEnMois", 0);
        int nSeances = o.optInt("nombreSeances", 0);
        double prix = o.optDouble("prix", 0);
        boolean actif = o.optBoolean("actif", true);
        h.client.setText(type);
        h.title.setText(nom);
        h.dates.setText(duree > 0 ? ("Durée: " + duree + " mois") : (nSeances > 0 ? ("Séances: " + nSeances) : ""));
        h.meta.setText("Prix: " + ((prix == (long)prix) ? String.valueOf((long)prix) : String.valueOf(prix)) + " TND" + (desc!=null && !desc.isEmpty() ? ("\n" + desc) : ""));
        h.statut.setText(actif?"ACTIF":"INACTIF");
        int color = android.graphics.Color.parseColor(actif?"#2e7d32":"#616161");
        h.statut.setBackgroundColor(color);
        h.cancel.setVisibility(View.GONE);
        h.edit.setOnClickListener(v -> cb.onEditType(o));
        h.delete.setOnClickListener(v -> cb.onDeleteType(id));
    }

    @Override public int getItemCount() { return data.size(); }

    private String formatDate(String s) {
        try {
            if (s == null || s.isEmpty() || s.equals("null")) return "";
            java.text.DateFormat in = s.contains("T") ? new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()) : new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date d = in.parse(s);
            return new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(d);
        } catch (Exception ignored) {}
        return s;
    }
}
