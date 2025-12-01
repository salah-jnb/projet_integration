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
import { Textarea } from "@/components/ui/textarea";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { toast } from "sonner";
import { Plus } from "lucide-react";
import { abonnementsApi } from "@/lib/api";

const subscriptionSchema = z
  .object({
    nom: z
      .string()
      .min(2, "Nom requis")
      .max(60, "Nom trop long")
      .regex(/^[\p{L}\d][\p{L}\d ' -]{1,60}$/u, "Nom invalide (lettres, chiffres, espaces, -, ')")
      ,
    type: z.enum(["SALLE", "COURS_COLLECTIFS", "PACK_COACHING_5", "PACK_COACHING_10", "PACK_COACHING_20"]),
    description: z
      .string()
      .min(20, "Description trop courte")
      .max(500, "Description trop longue")
      .refine((v) => /[\p{L}\d]/u.test(v), "La description doit contenir du texte"),
    dureeEnMois: z.number().int().min(1, "Durée minimale 1 mois").max(36, "Durée maximale 36 mois").optional(),
    nombreSeances: z.number().int().min(1, "Minimum 1 séance").max(100, "Maximum 100 séances").optional(),
    prix: z.number().min(1, "Prix requis").max(10000, "Prix trop élevé"),
  })
  .superRefine((data, ctx) => {
    if (data.type === "SALLE" || data.type === "COURS_COLLECTIFS") {
      if (!data.dureeEnMois || data.dureeEnMois < 1) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: "Durée en mois requise",
          path: ["dureeEnMois"],
        });
      }
    }
    if (data.type.startsWith("PACK_COACHING")) {
      const expected = data.type === "PACK_COACHING_5" ? 5 : data.type === "PACK_COACHING_10" ? 10 : 20;
      if (!data.nombreSeances) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: "Nombre de séances requis",
          path: ["nombreSeances"],
        });
      } else if (data.nombreSeances !== expected) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: `Doit être exactement ${expected} séances`,
          path: ["nombreSeances"],
        });
      }
    }
  });

type SubscriptionFormData = z.infer<typeof subscriptionSchema>;

export const CreateSubscriptionTypeDialog = ({ onCreated }: { onCreated?: () => void }) => {
  const [open, setOpen] = useState(false);
  const { register, handleSubmit, formState: { errors }, setValue, watch, reset } = useForm<SubscriptionFormData>({
    resolver: zodResolver(subscriptionSchema),
  });

  const type = watch("type");

  const onSubmit = async (data: SubscriptionFormData) => {
    try {
      await abonnementsApi.createType({
        type: data.type,
        nom: data.nom,
        description: data.description,
        dureeEnMois: data.dureeEnMois ?? null,
        nombreSeances: data.nombreSeances ?? null,
        prix: data.prix,
      });
      toast.success("Type d'abonnement créé avec succès !");
      setOpen(false);
      reset();
      onCreated?.();
    } catch (error: any) {
      toast.error("Erreur lors de la création");
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button>
          <Plus className="mr-2 h-4 w-4" />
          Nouveau type d'abonnement
        </Button>
      </DialogTrigger>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Créer un type d'abonnement</DialogTitle>
          <DialogDescription>
            Définissez les caractéristiques de ce nouveau type d'abonnement
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="nom">Nom de l'abonnement *</Label>
            <Input id="nom" {...register("nom")} placeholder="Ex: Abonnement Mensuel Salle" />
            {errors.nom && (
              <p className="text-sm text-destructive">{errors.nom.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="type">Type *</Label>
            <Select onValueChange={(value) => setValue("type", value as any)}>
              <SelectTrigger>
                <SelectValue placeholder="Sélectionner un type" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="SALLE">Accès Salle</SelectItem>
                <SelectItem value="COURS_COLLECTIFS">Cours Collectifs</SelectItem>
                <SelectItem value="PACK_COACHING_5">Pack Coaching (5)</SelectItem>
                <SelectItem value="PACK_COACHING_10">Pack Coaching (10)</SelectItem>
                <SelectItem value="PACK_COACHING_20">Pack Coaching (20)</SelectItem>
              </SelectContent>
            </Select>
            {errors.type && (
              <p className="text-sm text-destructive">{errors.type.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">Description *</Label>
            <Textarea id="description" {...register("description")} rows={3} />
            {errors.description && (
              <p className="text-sm text-destructive">{errors.description.message}</p>
            )}
          </div>

          <div className="grid md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="dureeEnMois">Durée (mois)</Label>
              <Input
                id="dureeEnMois"
                type="number"
                {...register("dureeEnMois", { valueAsNumber: true })}
              />
              {errors.dureeEnMois && (
                <p className="text-sm text-destructive">{errors.dureeEnMois.message}</p>
              )}
            </div>

            {(type?.startsWith("PACK_COACHING")) && (
              <div className="space-y-2">
                <Label htmlFor="nombreSeances">Nombre de séances</Label>
                <Input
                  id="nombreSeances"
                  type="number"
                  {...register("nombreSeances", { valueAsNumber: true })}
                />
                {errors.nombreSeances && (
                  <p className="text-sm text-destructive">{errors.nombreSeances.message}</p>
                )}
              </div>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="prix">Prix (TND) *</Label>
            <Input
              id="prix"
              type="number"
              step="0.01"
              {...register("prix", { valueAsNumber: true })}
            />
            {errors.prix && (
              <p className="text-sm text-destructive">{errors.prix.message}</p>
            )}
          </div>

          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>
              Annuler
            </Button>
            <Button type="submit">Créer</Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};
