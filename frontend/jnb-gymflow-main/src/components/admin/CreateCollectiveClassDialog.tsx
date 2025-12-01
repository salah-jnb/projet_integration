import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
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
import { Plus, Loader2 } from "lucide-react";
import { coursCollectifsApi, coachsApi } from "@/lib/api";
import { toast } from "sonner";
import { z } from "zod";

interface Coach {
  utilisateurId: number;
  nom: string;
  prenom: string;
}

interface CreateCollectiveClassDialogProps {
  onCreated: () => void;
}

export function CreateCollectiveClassDialog({ onCreated }: CreateCollectiveClassDialogProps) {
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [coaches, setCoaches] = useState<Coach[]>([]);
  const [formData, setFormData] = useState({
    nom: "",
    description: "",
    coachId: "",
    jourSemaine: "",
    heureDebut: "",
    heureFin: "",
    capaciteMax: "",
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [touched, setTouched] = useState<Record<string, boolean>>({
    nom: false,
    description: false,
    coachId: false,
    jourSemaine: false,
    heureDebut: false,
    heureFin: false,
    capaciteMax: false,
  });

  const toMinutes = (t: string) => {
    const parts = t.split(":");
    const h = parseInt(parts[0] || "0", 10);
    const m = parseInt(parts[1] || "0", 10);
    return h * 60 + m;
  };

  const classSchema = z
    .object({
      nom: z.string().min(1, "Nom requis"),
      description: z.string().optional().default(""),
      coachId: z.string().min(1, "Coach requis"),
      jourSemaine: z.string().min(1, "Jour requis"),
      heureDebut: z.string().regex(/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, "Format HH:MM requis"),
      heureFin: z.string().regex(/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, "Format HH:MM requis"),
      capaciteMax: z.string().refine((v) => parseInt(v, 10) >= 1, "La capacité maximale doit être au minimum 1"),
    })
    .refine((d) => toMinutes(d.heureFin) > toMinutes(d.heureDebut), {
      path: ["heureFin"],
      message: "L'heure de fin doit être après l'heure de début",
    });

  const normalize = (d: typeof formData) => ({
    nom: d.nom.trim(),
    description: d.description.trim(),
    coachId: d.coachId.trim(),
    jourSemaine: d.jourSemaine.trim(),
    heureDebut: d.heureDebut.trim(),
    heureFin: d.heureFin.trim(),
    capaciteMax: d.capaciteMax.trim(),
  });

  const validateField = (name: string, value: string) => {
    const data = normalize({ ...formData, [name]: value });
    const res = classSchema.safeParse(data);
    if (!res.success) {
      const err = res.error.errors.find((e) => String(e.path[0]) === name);
      return err ? err.message : "";
    }
    return "";
  };

  const isFormValid = classSchema.safeParse(normalize(formData)).success;

  useEffect(() => {
    if (open) {
      fetchCoaches();
    }
  }, [open]);

  const fetchCoaches = async () => {
    try {
      const response = await coachsApi.getAll();
      setCoaches(response.data);
    } catch (error) {
      console.error("Erreur lors du chargement des coachs:", error);
      toast.error("Impossible de charger la liste des coachs");
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setTouched({ nom: true, description: true, coachId: true, jourSemaine: true, heureDebut: true, heureFin: true, capaciteMax: true });
    setErrors({});
    const normalized = normalize(formData);
    const result = classSchema.safeParse(normalized);
    if (!result.success) {
      const fieldErrors: Record<string, string> = {};
      result.error.errors.forEach((err) => {
        if (err.path[0]) fieldErrors[String(err.path[0])] = err.message;
      });
      setErrors(fieldErrors);
      return;
    }

    setLoading(true);
    try {
      const dataToSend = {
        nom: normalized.nom,
        description: normalized.description,
        coachId: parseInt(normalized.coachId, 10),
        jourSemaine: normalized.jourSemaine,
        heureDebut: normalized.heureDebut + ":00",
        heureFin: normalized.heureFin + ":00",
        capaciteMax: parseInt(normalized.capaciteMax, 10),
      };
      await coursCollectifsApi.create(dataToSend);
      toast.success("Cours créé avec succès");
      setOpen(false);
      setFormData({ nom: "", description: "", coachId: "", jourSemaine: "", heureDebut: "", heureFin: "", capaciteMax: "" });
      onCreated();
    } catch (error: any) {
      toast.error(error.response?.data?.message || "Impossible de créer le cours");
    } finally {
      setLoading(false);
    }
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

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button>
          <Plus className="mr-2 h-4 w-4" />
          Nouveau Cours
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>Créer un cours collectif</DialogTitle>
          <DialogDescription>
            Ajoutez un nouveau cours collectif au planning
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit}>
          <div className="grid gap-4 py-4">
            <div className="grid gap-2">
              <Label htmlFor="nom">Nom du cours *</Label>
              <Input
                id="nom"
                value={formData.nom}
                onChange={(e) => {
                  const v = e.target.value.trim();
                  setFormData({ ...formData, nom: v });
                  setErrors((p) => ({ ...p, nom: validateField("nom", v) }));
                }}
                onBlur={() => setTouched((p) => ({ ...p, nom: true }))}
                className={touched.nom && errors.nom ? "border-destructive" : ""}
                required
              />
            </div>

            <div className="grid gap-2">
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                value={formData.description}
                onChange={(e) => {
                  const v = e.target.value;
                  setFormData({ ...formData, description: v });
                  setErrors((p) => ({ ...p, description: validateField("description", v) }));
                }}
                onBlur={() => setTouched((p) => ({ ...p, description: true }))}
                rows={3}
              />
            </div>

            <div className="grid gap-2">
              <Label htmlFor="coach">Coach *</Label>
              <Select
                value={formData.coachId}
                onValueChange={(value) => {
                  setFormData({ ...formData, coachId: value });
                  setErrors((p) => ({ ...p, coachId: validateField("coachId", value) }));
                }}
                onOpenChange={(open) => {
                  if (!open) setTouched((p) => ({ ...p, coachId: true }));
                }}
                required
              >
                <SelectTrigger id="coach">
                  <SelectValue placeholder="Sélectionner un coach" />
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
              <Label htmlFor="jour">Jour de la semaine *</Label>
              <Select
                value={formData.jourSemaine}
                onValueChange={(value) => {
                  setFormData({ ...formData, jourSemaine: value });
                  setErrors((p) => ({ ...p, jourSemaine: validateField("jourSemaine", value) }));
                }}
                onOpenChange={(open) => {
                  if (!open) setTouched((p) => ({ ...p, jourSemaine: true }));
                }}
                required
              >
                <SelectTrigger id="jour">
                  <SelectValue placeholder="Sélectionner un jour" />
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
                <Label htmlFor="heureDebut">Heure début *</Label>
                <Input
                  id="heureDebut"
                  type="time"
                  value={formData.heureDebut}
                  onChange={(e) => {
                    const v = e.target.value;
                    setFormData({ ...formData, heureDebut: v });
                    setErrors((p) => ({ ...p, heureDebut: validateField("heureDebut", v), heureFin: validateField("heureFin", formData.heureFin) }));
                  }}
                  onBlur={() => setTouched((p) => ({ ...p, heureDebut: true }))}
                  className={touched.heureDebut && errors.heureDebut ? "border-destructive" : ""}
                  required
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="heureFin">Heure fin *</Label>
                <Input
                  id="heureFin"
                  type="time"
                  value={formData.heureFin}
                  onChange={(e) => {
                    const v = e.target.value;
                    setFormData({ ...formData, heureFin: v });
                    setErrors((p) => ({ ...p, heureFin: validateField("heureFin", v) }));
                  }}
                  onBlur={() => setTouched((p) => ({ ...p, heureFin: true }))}
                  className={touched.heureFin && errors.heureFin ? "border-destructive" : ""}
                  required
                />
              </div>
            </div>

            <div className="grid gap-2">
              <Label htmlFor="capacite">Capacité maximale *</Label>
              <Input
                id="capacite"
                type="number"
                min="1"
                value={formData.capaciteMax}
                onChange={(e) => {
                  const v = e.target.value;
                  setFormData({ ...formData, capaciteMax: v });
                  setErrors((p) => ({ ...p, capaciteMax: validateField("capaciteMax", v) }));
                }}
                onBlur={() => setTouched((p) => ({ ...p, capaciteMax: true }))}
                className={touched.capaciteMax && errors.capaciteMax ? "border-destructive" : ""}
                required
              />
            </div>
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>
              Annuler
            </Button>
            <Button type="submit" disabled={loading || !isFormValid}>
              {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Créer
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
