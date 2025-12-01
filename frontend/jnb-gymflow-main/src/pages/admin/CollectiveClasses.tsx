import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Calendar, Clock, Users, Edit, Trash2, Loader2, PlusCircle } from "lucide-react";
import { CreateCollectiveClassDialog } from "@/components/admin/CreateCollectiveClassDialog";
import { api, coursCollectifsApi, coachsApi } from "@/lib/api";
import { smartNotify } from "@/lib/notify";
import { toast } from "sonner";
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
import { Textarea } from "@/components/ui/textarea";
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

interface CollectiveClass {
  id: number;
  nom: string;
  description?: string;
  coachId: number;
  coachNom: string;
  coachPrenom: string;
  jourSemaine: string;
  heureDebut: string;
  heureFin: string;
  capaciteMax: number;
  statut: string;
}

interface Coach {
  utilisateurId: number;
  nom: string;
  prenom: string;
}

interface Seance {
  id: number;
  coursCollectifId: number;
  coursNom?: string;
  dateSeance: string;
  placesDisponibles: number;
  annulee: boolean;
}

const AdminCollectiveClasses = () => {
  const [classes, setClasses] = useState<CollectiveClass[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingClass, setEditingClass] = useState<CollectiveClass | null>(null);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [classToDelete, setClassToDelete] = useState<number | null>(null);
  const [coaches, setCoaches] = useState<Coach[]>([]);
  const [sessions, setSessions] = useState<Seance[]>([]);
  const [isSeanceDialogOpen, setIsSeanceDialogOpen] = useState(false);
  const [seanceForm, setSeanceForm] = useState<{ coursCollectifId: number | null; dateSeance: string; placesDisponibles: string }>({ coursCollectifId: null, dateSeance: "", placesDisponibles: "" });
  const [isEditSeanceDialogOpen, setIsEditSeanceDialogOpen] = useState(false);
  const [editingSeance, setEditingSeance] = useState<Seance | null>(null);

  const fetchClasses = async () => {
    try {
      setLoading(true);
      const response = await coursCollectifsApi.getAll();
      setClasses(response.data);
    } catch (error) {
      console.error("Erreur lors du chargement des cours:", error);
      toast.error("Impossible de charger les cours collectifs");
    } finally {
      setLoading(false);
    }
  };

  const fetchCoaches = async () => {
    try {
      const response = await coachsApi.getAll();
      setCoaches(response.data);
    } catch (error) {
      console.error("Erreur lors du chargement des coachs:", error);
    }
  };

  const fetchSessions = async () => {
    try {
      const response = await coursCollectifsApi.getAllSessions();
      const now = new Date();
      const upcoming = (response.data || []).filter((s: any) => {
        const d = new Date(s.dateSeance);
        return d.getTime() > now.getTime();
      });
      setSessions(upcoming);
    } catch (error) {
      console.error("Erreur lors du chargement des séances:", error);
    }
  };

  useEffect(() => {
    fetchClasses();
    fetchCoaches();
    fetchSessions();
  }, []);

  const handleDelete = async (id: number) => {
    try {
      await coursCollectifsApi.delete(id.toString());
      toast.success("Cours supprimé avec succès");
      setClassToDelete(null);
      fetchClasses();
    } catch (error) {
      console.error("Erreur lors de la suppression:", error);
      toast.error("Impossible de supprimer le cours");
    }
  };

  const handleEdit = (course: CollectiveClass) => {
    setEditingClass({ ...course });
    setIsEditDialogOpen(true);
  };

  const openSeanceDialog = (course: CollectiveClass) => {
    setSeanceForm({ coursCollectifId: course.id, dateSeance: "", placesDisponibles: "" });
    setIsSeanceDialogOpen(true);
  };

  const openEditSeanceDialog = (seance: Seance) => {
    setEditingSeance({ ...seance });
    setIsEditSeanceDialogOpen(true);
  };

  const handleCreateSeance = async () => {
    if (!seanceForm.coursCollectifId) {
      toast.error("Cours invalide");
      return;
    }
    if (!seanceForm.dateSeance) {
      toast.error("Date et heure de séance requises");
      return;
    }
    if (!seanceForm.placesDisponibles || parseInt(seanceForm.placesDisponibles) < 1) {
      toast.error("Nombre de places doit être au minimum 1");
      return;
    }

    try {
      const payload = {
        coursCollectifId: seanceForm.coursCollectifId,
        dateSeance: new Date(seanceForm.dateSeance).toISOString(),
        placesDisponibles: parseInt(seanceForm.placesDisponibles),
      };
      await coursCollectifsApi.createSeance(payload);
      toast.success("Séance créée avec succès");
      setIsSeanceDialogOpen(false);
      setSeanceForm({ coursCollectifId: null, dateSeance: "", placesDisponibles: "" });
      fetchSessions();
    } catch (error: any) {
      toast.error(error?.response?.data?.message || "Impossible de créer la séance");
    }
  };

  const handleUpdateSeance = async () => {
    if (!editingSeance) return;
    try {
      const payload: any = {};
      if (editingSeance.dateSeance) {
        payload.dateSeance = new Date(editingSeance.dateSeance).toISOString();
      }
      if (typeof editingSeance.placesDisponibles === 'number') {
        payload.placesDisponibles = editingSeance.placesDisponibles;
      }
      await coursCollectifsApi.updateSeance(editingSeance.id.toString(), payload);
      toast.success("Séance mise à jour");
      setIsEditSeanceDialogOpen(false);
      setEditingSeance(null);
      fetchSessions();
    } catch (error: any) {
      toast.error(error?.response?.data?.message || "Impossible de modifier la séance");
    }
  };

  const handleDeleteSeance = async (id: number) => {
    try {
      const seance = sessions.find(s => s.id === id);
      let coachId: number | null = null;
      if (seance) {
        try {
          const cours = await coursCollectifsApi.getById(seance.coursCollectifId);
          coachId = Number(cours.data?.coachId) || null;
        } catch {}
      }
      await coursCollectifsApi.deleteSeance(id.toString());
      if (coachId) {
        await smartNotify({
          destId: coachId,
          title: "Séance annulée",
          type: "SEANCE_ANNULEE",
          message: `Une séance a été annulée (ID ${id})`,
        });
      }
      toast.success("Séance supprimée");
      fetchSessions();
    } catch (error: any) {
      toast.error(error?.response?.data?.message || "Suppression impossible");
    }
  };

  const handleUpdateClass = async () => {
    if (!editingClass || !editingClass.id) {
      toast.error("Cours invalide");
      return;
    }

    try {
      // Validations complètes
      if (!editingClass.nom.trim()) {
        toast.error("Le nom du cours est requis");
        return;
      }
      if (!editingClass.jourSemaine) {
        toast.error("Veuillez sélectionner un jour");
        return;
      }
      if (!editingClass.heureDebut) {
        toast.error("Veuillez sélectionner une heure de début");
        return;
      }
      if (!editingClass.heureFin) {
        toast.error("Veuillez sélectionner une heure de fin");
        return;
      }
      if (!editingClass.capaciteMax || editingClass.capaciteMax < 1) {
        toast.error("La capacité maximale doit être au minimum 1");
        return;
      }

      // Préparer les données
      const updateData = {
        nom: editingClass.nom.trim(),
        description: editingClass.description?.trim() || "",
        coachId: editingClass.coachId.toString(),
        jourSemaine: editingClass.jourSemaine,
        heureDebut: editingClass.heureDebut.includes(":") && editingClass.heureDebut.length === 5
          ? editingClass.heureDebut + ":00"
          : editingClass.heureDebut,
        heureFin: editingClass.heureFin.includes(":") && editingClass.heureFin.length === 5
          ? editingClass.heureFin + ":00"
          : editingClass.heureFin,
        capaciteMax: editingClass.capaciteMax,
      };

      console.log("Données de modification:", JSON.stringify(updateData, null, 2));

      await coursCollectifsApi.update(editingClass.id.toString(), updateData);
      toast.success("Cours modifié avec succès");
      setIsEditDialogOpen(false);
      setEditingClass(null);
      fetchClasses();
    } catch (error: any) {
      console.error("Erreur lors de la modification:", error);
      console.error("Réponse du serveur:", error.response?.data);
      toast.error(
        error.response?.data?.message || "Impossible de modifier le cours"
      );
    }
  };

  const getJourLabel = (jour: string) => {
    const jours: Record<string, string> = {
      "Dimanche": "Dimanche",
      "Lundi": "Lundi",
      "Mardi": "Mardi",
      "Mercredi": "Mercredi",
      "Jeudi": "Jeudi",
      "Vendredi": "Vendredi",
      "Samedi": "Samedi",
    };
    return jours[jour] || jour;
  };

  const joursOptions = [
    { value: "Dimanche", label: "Dimanche" },
    { value: "Lundi", label: "Lundi" },
    { value: "Mardi", label: "Mardi" },
    { value: "Mercredi", label: "Mercredi" },
    { value: "Jeudi", label: "Jeudi" },
    { value: "Vendredi", label: "Vendredi" },
    { value: "Samedi", label: "Samedi" },
  ];

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
          <h1 className="text-3xl font-bold mb-2">Cours Collectifs</h1>
          <p className="text-muted-foreground">Gestion des cours disponibles</p>
        </div>
        <CreateCollectiveClassDialog onCreated={fetchClasses} />
      </div>

      <div className="grid md:grid-cols-2 gap-4">
        {classes.length === 0 ? (
          <p className="col-span-2 text-center text-muted-foreground py-8">
            Aucun cours collectif trouvé
          </p>
        ) : (
          classes.map((course) => (
            <Card key={course.id}>
              <CardHeader>
                <CardTitle className="flex items-center justify-between">
                  <span>{course.nom}</span>
                  <Badge variant={course.statut === "ACTIF" ? "default" : "secondary"}>
                    {course.statut}
                  </Badge>
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                {course.description && (
                  <p className="text-sm text-muted-foreground">{course.description}</p>
                )}
                <p className="text-sm text-muted-foreground">
                  Coach: {course.coachPrenom} {course.coachNom}
                </p>
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                  <Calendar className="h-4 w-4" />
                  <span>{getJourLabel(course.jourSemaine)}</span>
                </div>
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                  <Clock className="h-4 w-4" />
                  <span>
                    {course.heureDebut} - {course.heureFin}
                  </span>
                </div>
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                  <Users className="h-4 w-4" />
                  <span>Capacité: {course.capaciteMax} personnes</span>
                </div>

                <div className="mt-4 space-y-2">
                  <p className="text-sm font-semibold">Séances planifiées</p>
                  {sessions.filter((s) => s.coursCollectifId === course.id).length === 0 ? (
                    <p className="text-sm text-muted-foreground">Aucune séance créée</p>
                  ) : (
                    <div className="space-y-2">
                      {sessions
                        .filter((s) => s.coursCollectifId === course.id)
                        .sort((a, b) => new Date(a.dateSeance).getTime() - new Date(b.dateSeance).getTime())
                        .map((s) => (
                          <div key={s.id} className="flex items-center justify-between p-2 border rounded">
                            <div className="flex items-center gap-3 text-sm">
                              <Calendar className="h-4 w-4" />
                              <span>{new Date(s.dateSeance).toLocaleString()}</span>
                              <Badge variant={s.annulee ? "secondary" : "outline"}>
                                {s.annulee ? "Annulée" : `${s.placesDisponibles} places`}
                              </Badge>
                            </div>
                            <div className="flex items-center gap-2">
                              <Button size="sm" variant="outline" onClick={() => openEditSeanceDialog(s)}>
                                <Edit className="h-4 w-4" />
                              </Button>
                              <Button size="sm" variant="outline" onClick={() => handleDeleteSeance(s.id)}>
                                <Trash2 className="h-4 w-4 text-destructive" />
                              </Button>
                            </div>
                          </div>
                        ))}
                    </div>
                  )}
                </div>

                <div className="flex gap-2 mt-4">
                  <Button
                    size="sm"
                    variant="outline"
                    className="flex-1"
                    onClick={() => handleEdit(course)}
                  >
                    <Edit className="mr-2 h-4 w-4" />
                    Modifier
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    className="flex-1"
                    onClick={() => setClassToDelete(course.id)}
                  >
                    <Trash2 className="mr-2 h-4 w-4 text-destructive" />
                    Supprimer
                  </Button>
                  <Button
                    size="sm"
                    className="flex-1"
                    onClick={() => openSeanceDialog(course)}
                  >
                    <PlusCircle className="mr-2 h-4 w-4" />
                    Créer une séance
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))
        )}
      </div>

      {/* AlertDialog de suppression */}
      <AlertDialog
        open={classToDelete !== null}
        onOpenChange={(open) => !open && setClassToDelete(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Êtes-vous sûr?</AlertDialogTitle>
            <AlertDialogDescription>
              Cette action ne peut pas être annulée. Cela supprimera définitivement le cours
              {classToDelete && classes.find(c => c.id === classToDelete) && (
                <> <strong>{classes.find(c => c.id === classToDelete)?.nom}</strong></>
              )}.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={() => setClassToDelete(null)}>
              Annuler
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={() => {
                if (classToDelete) {
                  handleDelete(classToDelete);
                }
              }}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              Supprimer
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Dialog de modification */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>Modifier le cours collectif</DialogTitle>
            <DialogDescription>
              Modifiez les informations du cours
            </DialogDescription>
          </DialogHeader>
          {editingClass && (
            <div className="grid gap-4 py-4">
              <div className="grid gap-2">
                <Label htmlFor="edit-nom">Nom du cours *</Label>
                <Input
                  id="edit-nom"
                  value={editingClass.nom || ""}
                  onChange={(e) =>
                    setEditingClass({ ...editingClass, nom: e.target.value })
                  }
                  required
                />
              </div>

              <div className="grid gap-2">
                <Label htmlFor="edit-description">Description</Label>
                <Textarea
                  id="edit-description"
                  value={editingClass.description || ""}
                  onChange={(e) =>
                    setEditingClass({ ...editingClass, description: e.target.value })
                  }
                  rows={3}
                />
              </div>

              <div className="grid gap-2">
                <Label htmlFor="edit-coach">Coach *</Label>
                <Select
                  value={editingClass.coachId.toString()}
                  onValueChange={(value) =>
                    setEditingClass({ ...editingClass, coachId: parseInt(value) })
                  }
                >
                  <SelectTrigger id="edit-coach">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {coaches.map((coach) => (
                      <SelectItem key={coach.utilisateurId} value={coach.utilisateurId.toString()}>
                        {coach.prenom} {coach.nom}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="grid gap-2">
                <Label htmlFor="edit-jour">Jour de la semaine *</Label>
                <Select
                  value={editingClass.jourSemaine}
                  onValueChange={(value) =>
                    setEditingClass({ ...editingClass, jourSemaine: value })
                  }
                >
                  <SelectTrigger id="edit-jour">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {joursOptions.map((jour) => (
                      <SelectItem key={jour.value} value={jour.value}>
                        {jour.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="grid gap-2">
                  <Label htmlFor="edit-heureDebut">Heure début *</Label>
                  <Input
                    id="edit-heureDebut"
                    type="time"
                    value={editingClass.heureDebut ? editingClass.heureDebut.substring(0, 5) : ""}
                    onChange={(e) =>
                      setEditingClass({ ...editingClass, heureDebut: e.target.value })
                    }
                    required
                  />
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="edit-heureFin">Heure fin *</Label>
                  <Input
                    id="edit-heureFin"
                    type="time"
                    value={editingClass.heureFin ? editingClass.heureFin.substring(0, 5) : ""}
                    onChange={(e) =>
                      setEditingClass({ ...editingClass, heureFin: e.target.value })
                    }
                    required
                  />
                </div>
              </div>

              <div className="grid gap-2">
                <Label htmlFor="edit-capacite">Capacité maximale *</Label>
                <Input
                  id="edit-capacite"
                  type="number"
                  min="1"
                  value={editingClass.capaciteMax || ""}
                  onChange={(e) =>
                    setEditingClass({ ...editingClass, capaciteMax: parseInt(e.target.value) })
                  }
                  required
                />
              </div>
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsEditDialogOpen(false)}>
              Annuler
            </Button>
            <Button onClick={handleUpdateClass}>Enregistrer</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={isSeanceDialogOpen} onOpenChange={setIsSeanceDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>Créer une séance</DialogTitle>
            <DialogDescription>
              Planifiez une nouvelle séance pour le cours sélectionné
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid gap-2">
              <Label htmlFor="seance-date">Date et heure *</Label>
              <Input
                id="seance-date"
                type="datetime-local"
                value={seanceForm.dateSeance}
                onChange={(e) => setSeanceForm({ ...seanceForm, dateSeance: e.target.value })}
                required
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="seance-places">Places disponibles *</Label>
              <Input
                id="seance-places"
                type="number"
                min="1"
                value={seanceForm.placesDisponibles}
                onChange={(e) => setSeanceForm({ ...seanceForm, placesDisponibles: e.target.value })}
                required
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsSeanceDialogOpen(false)}>Annuler</Button>
            <Button onClick={handleCreateSeance}>Créer</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={isEditSeanceDialogOpen} onOpenChange={setIsEditSeanceDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>Modifier la séance</DialogTitle>
            <DialogDescription>Mettez à jour la date/heure ou les places</DialogDescription>
          </DialogHeader>
          {editingSeance && (
            <div className="grid gap-4 py-4">
              <div className="grid gap-2">
                <Label htmlFor="edit-seance-date">Date et heure</Label>
                <Input
                  id="edit-seance-date"
                  type="datetime-local"
                  value={editingSeance.dateSeance ? new Date(editingSeance.dateSeance).toISOString().slice(0,16) : ""}
                  onChange={(e) => setEditingSeance({ ...editingSeance, dateSeance: e.target.value })}
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="edit-seance-places">Places disponibles</Label>
                <Input
                  id="edit-seance-places"
                  type="number"
                  min="0"
                  value={editingSeance.placesDisponibles}
                  onChange={(e) => setEditingSeance({ ...editingSeance, placesDisponibles: parseInt(e.target.value || '0') })}
                />
              </div>
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsEditSeanceDialogOpen(false)}>Annuler</Button>
            <Button onClick={handleUpdateSeance}>Enregistrer</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default AdminCollectiveClasses;
