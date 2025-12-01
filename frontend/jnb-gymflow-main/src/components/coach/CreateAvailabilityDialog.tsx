import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { toast } from "sonner";
import { Plus } from "lucide-react";
import { disponibilitesApi } from "@/lib/api";
import { useAuth } from "@/contexts/AuthContext";

const availabilitySchema = z.object({
  jourSemaine: z.enum(["LUNDI","MARDI","MERCREDI","JEUDI","VENDREDI","SAMEDI","DIMANCHE"]),
  heureDebut: z.string().regex(/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, "Format HH:MM requis"),
  heureFin: z.string().regex(/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, "Format HH:MM requis"),
});

type AvailabilityFormData = z.infer<typeof availabilitySchema>;

export const CreateAvailabilityDialog = ({ onCreated }: { onCreated?: () => void }) => {
  const [open, setOpen] = useState(false);
  const { register, handleSubmit, formState: { errors }, setValue, reset } = useForm<AvailabilityFormData>({
    resolver: zodResolver(availabilitySchema),
  });
  const { user } = useAuth();

  const toApiTime = (t: string) => (t && t.length === 5 ? `${t}:00` : t || "");

  const onSubmit = async (data: AvailabilityFormData) => {
    try {
      if (!user?.utilisateurId) {
        toast.error("Utilisateur non authentifié");
        return;
      }
      const payload = {
        jourSemaine: data.jourSemaine,
        heureDebut: toApiTime(data.heureDebut),
        heureFin: toApiTime(data.heureFin),
        actif: true,
      };
      await disponibilitesApi.create(user.utilisateurId.toString(), payload);
      toast.success("Disponibilité ajoutée avec succès !");
      setOpen(false);
      reset();
      onCreated?.();
    } catch (error: any) {
      const msg = error?.response?.data || "Erreur lors de l'ajout";
      toast.error(typeof msg === "string" ? msg : "Erreur lors de l'ajout");
    }
  };

  const jours = [
    { value: "LUNDI", label: "Lundi" },
    { value: "MARDI", label: "Mardi" },
    { value: "MERCREDI", label: "Mercredi" },
    { value: "JEUDI", label: "Jeudi" },
    { value: "VENDREDI", label: "Vendredi" },
    { value: "SAMEDI", label: "Samedi" },
    { value: "DIMANCHE", label: "Dimanche" },
  ];

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button>
          <Plus className="mr-2 h-4 w-4" />
          Ajouter une disponibilité
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Ajouter une disponibilité</DialogTitle>
          <DialogDescription>
            Définissez un créneau horaire hebdomadaire
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="jourSemaine">Jour de la semaine *</Label>
            <Select onValueChange={(value) => setValue("jourSemaine", value as AvailabilityFormData["jourSemaine"]) }>
              <SelectTrigger>
                <SelectValue placeholder="Sélectionner un jour" />
              </SelectTrigger>
              <SelectContent>
                {jours.map((jour) => (
                  <SelectItem key={jour.value} value={jour.value}>
                    {jour.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.jourSemaine && (
              <p className="text-sm text-destructive">{errors.jourSemaine.message}</p>
            )}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="heureDebut">Heure début *</Label>
              <Input id="heureDebut" type="time" {...register("heureDebut")} />
              {errors.heureDebut && (
                <p className="text-sm text-destructive">{errors.heureDebut.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="heureFin">Heure fin *</Label>
              <Input id="heureFin" type="time" {...register("heureFin")} />
              {errors.heureFin && (
                <p className="text-sm text-destructive">{errors.heureFin.message}</p>
              )}
            </div>
          </div>

          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>
              Annuler
            </Button>
            <Button type="submit">Ajouter</Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};
