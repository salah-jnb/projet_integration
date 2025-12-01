package jnb.fitness.admin;
import jnb.fitness.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.List;

public class AdminUsersAdapter extends RecyclerView.Adapter<AdminUsersAdapter.VH> {
    public interface Callbacks {
        void onToggleNewsletter(long id, boolean abonne);
        void onChangeStatus(long id, String statut);
        void onDelete(long id);
        void onEdit(org.json.JSONObject user);
    }

    private final List<JSONObject> data;
    private final Callbacks cb;

    public AdminUsersAdapter(List<JSONObject> data, Callbacks cb) {
        this.data = data;
        this.cb = cb;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name;
        TextView email;
        TextView statut;
        TextView type;
        SwitchMaterial newsletter;
        Button btnStatut;
        Button btnModifier;
        Button btnSupprimer;
        VH(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            email = v.findViewById(R.id.email);
            statut = v.findViewById(R.id.statut);
            type = v.findViewById(R.id.type);
            newsletter = v.findViewById(R.id.newsletter);
            btnStatut = v.findViewById(R.id.btn_statut);
            btnModifier = v.findViewById(R.id.btn_modifier);
            btnSupprimer = v.findViewById(R.id.btn_supprimer);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        JSONObject o = data.get(i);
        long id = o.optLong("id");
        String nom = o.optString("nom");
        String prenom = o.optString("prenom");
        String email = o.optString("email");
        String statut = o.optString("statut");
        String type = o.optString("typeUtilisateur");
        boolean abonne = o.optBoolean("newsletter", false);
        h.name.setText(nom + " " + prenom);
        h.email.setText(email);
        h.statut.setText(statut);
        h.type.setText(type.isEmpty()?"":type);
        h.newsletter.setOnCheckedChangeListener(null);
        h.newsletter.setChecked(abonne);
        int color = android.graphics.Color.parseColor(statut.equals("ACTIF")?"#2e7d32": statut.equals("SUSPENDU")?"#f9a825":"#616161");
        h.statut.setBackgroundColor(color);
        h.statut.setPadding(8,8,8,8);
        h.newsletter.setOnCheckedChangeListener((b, checked) -> cb.onToggleNewsletter(id, checked));
        h.btnStatut.setOnClickListener(v -> {
            android.widget.PopupMenu menu = new android.widget.PopupMenu(v.getContext(), h.btnStatut);
            menu.getMenu().add("ACTIF");
            menu.getMenu().add("SUSPENDU");
            menu.getMenu().add("INACTIF");
            menu.setOnMenuItemClickListener(item -> { cb.onChangeStatus(id, item.getTitle().toString()); return true; });
            menu.show();
        });
        h.btnModifier.setOnClickListener(v -> cb.onEdit(o));
        h.btnSupprimer.setOnClickListener(v -> cb.onDelete(id));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
