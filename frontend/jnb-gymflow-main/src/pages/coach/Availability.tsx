import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Clock, Trash2, Loader2, Edit2 } from "lucide-react";
import { CreateAvailabilityDialog } from "@/components/coach/CreateAvailabilityDialog";
import { disponibilitesApi } from "@/lib/api";
import { toast } from "sonner";
import { useAuth } from "@/contexts/AuthContext";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Switch } from "@/components/ui/switch";

interface Disponibilite {
  id: number;
  coachId: number;
  jourSemaine: string;   // "LUNDI" | ...
  heureDebut: string;    // "HH:mm" ou "HH:mm:ss"
  heureFin: string;      // idem
  actif: boolean;
}

const Availability = () => {
  const { user } = useAuth();
  const [availability, setAvailability] = useState<Disponibilite[]>([]);
  const [loading, setLoading] = useState(true);
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const [toDeleteId, setToDeleteId] = useState<number | null>(null);

  const [editOpen, setEditOpen] = useState(false);
  const [editing, setEditing] = useState<Disponibilite | null>(null);
  const [saving, setSaving] = useState(false);

  const jours = ["LUNDI","MARDI","MERCREDI","JEUDI","VENDREDI","SAMEDI","DIMANCHE"];

  useEffect(() => {
    if (user?.utilisateurId) fetchAvailability();
  }, [user?.utilisateurId]);

  const fetchAvailability = async () => {
    if (!user?.utilisateurId) return;
    try {
      setLoading(true);
      const res = await disponibilitesApi.getByCoach(user.utilisateurId.toString());
      console.log("Disponibilités:", res.data);
      setAvailability(res.data);
    } catch (e) {
      console.error("Erreur lors du chargement des disponibilités:", e);
      toast.error("Impossible de charger les disponibilités");
    } finally {
      setLoading(false);
    }
  };

  // Normalise HH:mm:ss -> HH:mm (affichage input time)
  const toInputTime = (t: string) => (t?.length >= 5 ? t.slice(0,5) : t || "");
  // Convertit HH:mm -> HH:mm:ss pour l’API
  const toApiTime = (t: string) => (t && t.length === 5 ? `${t}:00` : t || "");

  // Suppression avec confirmation
  const confirmDelete = async () => {
    if (!toDeleteId) return;
    try {
      setDeletingId(toDeleteId);
      await disponibilitesApi.delete(toDeleteId.toString());
      toast.success("Disponibilité supprimée avec succès");
      setAvailability((prev) => prev.filter((d) => d.id !== toDeleteId));
    } catch (e) {
      console.error("Erreur lors de la suppression:", e);
      toast.error("Impossible de supprimer la disponibilité");
    } finally {
      setDeletingId(null);
      setToDeleteId(null);
    }
  };

  const startEdit = (d: Disponibilite) => {
    // Préparer heureDebut/heureFin pour l’input time
    setEditing({
      ...d,
      heureDebut: toInputTime(d.heureDebut),
      heureFin: toInputTime(d.heureFin),
    });
    setEditOpen(true);
  };

  const saveEdit = async () => {
    if (!editing) return;
    try {
      setSaving(true);
      const payload = {
        jourSemaine: editing.jourSemaine,
        heureDebut: toApiTime(editing.heureDebut),
        heureFin: toApiTime(editing.heureFin),
        actif: editing.actif,
      };
      await disponibilitesApi.update(editing.id.toString(), payload);
      toast.success("Disponibilité mise à jour");
      setEditOpen(false);
      setEditing(null);
      // Mise à jour locale
      setAvailability((prev) =>
        prev.map((d) => (d.id === editing.id ? { ...d, ...payload } as Disponibilite : d))
      );
    } catch (e) {
      console.error("Erreur lors de la mise à jour:", e);
      toast.error("Impossible de modifier la disponibilité");
    } finally {
      setSaving(false);
    }
  };

  // Grouper et trier
  const groupByDay = (dispo: Disponibilite[]) => {
    const grouped = dispo
      .filter((d) => d.actif)
      .reduce((acc, item) => {
        const k = item.jourSemaine;
        if (!acc[k]) acc[k] = [];
        acc[k].push(item);
        return acc;
      }, {} as Record<string, Disponibilite[]>);

    Object.keys(grouped).forEach((k) => {
      grouped[k].sort((a, b) => toInputTime(a.heureDebut).localeCompare(toInputTime(b.heureDebut)));
    });
    return grouped;
  };

  const groupedAvailability = groupByDay(availability);

  if (loading) {
    return (
      <div className="p-6 flex justify-center items-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold mb-2">Mes Disponibilités</h1>
          <p className="text-muted-foreground">Gérez vos créneaux horaires</p>
        </div>
        <CreateAvailabilityDialog onCreated={fetchAvailability} />
      </div>

      <div className="grid gap-4">
        {jours.map((jour) => {
          const creneaux = groupedAvailability[jour] || [];
          return (
            <Card key={jour}>
              <CardHeader>
                <CardTitle className="flex items-center justify-between">
                  <span className="capitalize">{jour.toLowerCase()}</span>
                  <Badge variant={creneaux.length > 0 ? "default" : "secondary"}>
                    {creneaux.length > 0
                      ? `${creneaux.length} créneau${creneaux.length > 1 ? "x" : ""}`
                      : "Indisponible"}
                  </Badge>
                </CardTitle>
              </CardHeader>
              {creneaux.length > 0 && (
                <CardContent className="space-y-2">
                  {creneaux.map((c) => (
                    <div
                      key={c.id}
                      className="flex items-center justify-between p-3 border border-border rounded-lg"
                    >
                      <div className="flex items-center gap-2">
                        <Clock className="h-4 w-4 text-muted-foreground" />
                        <span>
                          {toInputTime(c.heureDebut)} - {toInputTime(c.heureFin)}
                        </span>
                      </div>
                      <div className="flex gap-2">
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => startEdit(c)}
                        >
                          <Edit2 className="h-4 w-4" />
                        </Button>
                        <Button
                          size="sm"
                          variant="ghost"
                          onClick={() => setToDeleteId(c.id)}
                          disabled={deletingId === c.id}
                        >
                          {deletingId === c.id ? (
                            <Loader2 className="h-4 w-4 animate-spin" />
                          ) : (
                            <Trash2 className="h-4 w-4 text-destructive" />
                          )}
                        </Button>
                      </div>
                    </div>
                  ))}
                </CardContent>
              )}
            </Card>
          );
        })}
      </div>

      {/* Dialog de confirmation suppression */}
      <AlertDialog open={toDeleteId !== null} onOpenChange={(o) => !o && setToDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Supprimer ce créneau ?</AlertDialogTitle>
            <AlertDialogDescription>
              Cette action est irréversible.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={() => setToDeleteId(null)}>Annuler</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              onClick={confirmDelete}
            >
              Supprimer
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Dialog d’édition */}
      <Dialog open={editOpen} onOpenChange={setEditOpen}>
        <DialogContent className="sm:max-w-[460px]">
          <DialogHeader>
            <DialogTitle>Modifier la disponibilité</DialogTitle>
            <DialogDescription>Mettez à jour les informations</DialogDescription>
          </DialogHeader>
          {editing && (
            <div className="grid gap-4 py-4">
              <div className="grid gap-2">
                <Label>Jour de la semaine</Label>
                <Select
                  value={editing.jourSemaine}
                  onValueChange={(v) => setEditing({ ...editing, jourSemaine: v })}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Choisir un jour" />
                  </SelectTrigger>
                  <SelectContent>
                    {jours.map((j) => (
                      <SelectItem key={j} value={j}>
                        {j}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="grid gap-2">
                  <Label>Heure début</Label>
                  <Input
                    type="time"
                    value={editing.heureDebut}
                    onChange={(e) => setEditing({ ...editing, heureDebut: e.target.value })}
                  />
                </div>
                <div className="grid gap-2">
                  <Label>Heure fin</Label>
                  <Input
                    type="time"
                    value={editing.heureFin}
                    onChange={(e) => setEditing({ ...editing, heureFin: e.target.value })}
                  />
                </div>
              </div>

              <div className="flex items-center gap-2">
                <Switch
                  checked={editing.actif}
                  onCheckedChange={(val) => setEditing({ ...editing, actif: val })}
                />
                <span>Actif</span>
              </div>
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setEditOpen(false)}>
              Annuler
            </Button>
            <Button onClick={saveEdit} disabled={saving}>
              {saving && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Enregistrer
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default Availability;
