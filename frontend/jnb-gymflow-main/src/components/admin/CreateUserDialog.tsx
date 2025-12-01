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
import { UserPlus } from "lucide-react";
import { usersApi, clientsApi, parrainagesApi } from "@/lib/api";

const userSchema = z
  .object({
    email: z.string().email("Email invalide"),
    motDePasse: z.string().min(6, "Au moins 6 caractères"),
    nom: z.string().min(2, "Nom requis"),
    prenom: z.string().min(2, "Prénom requis"),
    telephone: z.string().min(8, "Téléphone requis"),
    adresse: z.string().min(5, "Adresse requise"),
    typeUtilisateur: z.enum(["CLIENT", "COACH", "ADMINISTRATEUR"]),
    // Champs optionnels, requis seulement pour COACH
    codeParrainage: z.string().optional(),
    specialites: z.string().optional(),
    description: z.string().optional(),
  })
  .superRefine((data, ctx) => {
    if (data.typeUtilisateur === "COACH") {
      if (!data.specialites?.trim()) {
        ctx.addIssue({ code: z.ZodIssueCode.custom, message: "Spécialités requises pour un coach", path: ["specialites"] });
      }
      if (!data.description?.trim()) {
        ctx.addIssue({ code: z.ZodIssueCode.custom, message: "Description requise pour un coach", path: ["description"] });
      }
    }
  });

type UserFormData = z.infer<typeof userSchema>;

// Mapping enum frontend -> backend (numeric) pour TypeUtilisateur
const typeUtilisateurEnumMap: Record<"CLIENT" | "COACH", number> = {
  CLIENT: 0,
  COACH: 1,

};

export const CreateUserDialog = ({ onUserCreated }: { onUserCreated?: () => void }) => {
  const [open, setOpen] = useState(false);
  const { register, handleSubmit, formState: { errors }, setValue, reset, watch } = useForm<UserFormData>({
    resolver: zodResolver(userSchema),
  });
  const selectedType = watch("typeUtilisateur");

  const onSubmit = async (data: UserFormData) => {
    try {
      const rawCode = (data.codeParrainage || "").trim().toUpperCase();
      let parrainUtilisateurId: string | null = null;

      if (data.typeUtilisateur === "CLIENT" && rawCode) {
        try {
          const { data: parrainClient } = await clientsApi.getByParrainageCode(rawCode);
          parrainUtilisateurId = String(parrainClient.utilisateurId);
        } catch (err: any) {
          toast.error("Code de parrainage invalide");
          return;
        }
      }

      const payload: any = {
        email: data.email,
        motDePasse: data.motDePasse,
        nom: data.nom,
        prenom: data.prenom,
        telephone: data.telephone,
        adresse: data.adresse,
        // Envoyer la valeur numérique de l'énumération attendue par le backend
        typeUtilisateur: typeUtilisateurEnumMap[data.typeUtilisateur],
        // Champs requis implicitement par le backend (non-nullable)
        photo: "",
        codeParrainage: rawCode,
        // Champs spécifiques au coach, sinon envoyer vide pour satisfaire le modèle
        specialites: data.typeUtilisateur === "COACH" ? (data.specialites || "").trim() : "",
        description: data.typeUtilisateur === "COACH" ? (data.description || "").trim() : "",
      };

      // Debug: inspect payload being sent
      console.debug("CreateUser payload:", payload);

      const res = await usersApi.create(payload);
      const newUserId = String(res.data?.id);

      if (parrainUtilisateurId && data.typeUtilisateur === "CLIENT" && newUserId) {
        try {
          await parrainagesApi.create({ parrainId: parrainUtilisateurId, filleulId: newUserId });
        } catch (err) {
          // Ne bloque pas la création de l'utilisateur si la création du parrainage échoue
        }
      }
      toast.success("Utilisateur créé avec succès !");
      setOpen(false);
      reset();
      onUserCreated?.();
    } catch (error: any) {
      // Surface backend validation errors more clearly (e.g., ASP.NET ProblemDetails)
      const data = error?.response?.data;
      let message = data?.message || data?.title;
      if (data?.errors && typeof data.errors === "object") {
        try {
          const entries = Object.entries(data.errors);
          const msgs = entries.flatMap(([key, val]) => {
            const arr = Array.isArray(val) ? val : [val];
            return arr.map((m) => `${key}: ${m}`);
          });
          if (msgs.length) message = msgs.join(" | ");
        } catch {}
      } else if (!message && data) {
        try { message = JSON.stringify(data); } catch {}
      }
      try { console.error("CreateUser error:", JSON.stringify(data ?? error)); } catch { console.error("CreateUser error:", data ?? error); }
      toast.error(message || "Erreur lors de la création de l'utilisateur");
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button>
          <UserPlus className="mr-2 h-4 w-4" />
          Créer un utilisateur
        </Button>
      </DialogTrigger>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Créer un nouvel utilisateur</DialogTitle>
          <DialogDescription>
            Remplissez les informations pour créer un nouveau compte
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="prenom">Prénom *</Label>
              <Input id="prenom" {...register("prenom")} />
              {errors.prenom && (
                <p className="text-sm text-destructive">{errors.prenom.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="nom">Nom *</Label>
              <Input id="nom" {...register("nom")} />
              {errors.nom && (
                <p className="text-sm text-destructive">{errors.nom.message}</p>
              )}
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="email">Email *</Label>
            <Input id="email" type="email" {...register("email")} />
            {errors.email && (
              <p className="text-sm text-destructive">{errors.email.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="motDePasse">Mot de passe *</Label>
            <Input id="motDePasse" type="password" {...register("motDePasse")} />
            {errors.motDePasse && (
              <p className="text-sm text-destructive">{errors.motDePasse.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="telephone">Téléphone *</Label>
            <Input id="telephone" {...register("telephone")} />
            {errors.telephone && (
              <p className="text-sm text-destructive">{errors.telephone.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="adresse">Adresse *</Label>
            <Input id="adresse" {...register("adresse")} />
            {errors.adresse && (
              <p className="text-sm text-destructive">{errors.adresse.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="typeUtilisateur">Type d'utilisateur *</Label>
            <Select onValueChange={(value) => setValue("typeUtilisateur", value as any, { shouldValidate: true })}>
              <SelectTrigger>
                <SelectValue placeholder="Sélectionner un type" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="CLIENT">Client</SelectItem>
                <SelectItem value="COACH">Coach</SelectItem>
              
              </SelectContent>
            </Select>
            {errors.typeUtilisateur && (
              <p className="text-sm text-destructive">{errors.typeUtilisateur.message}</p>
            )}
          </div>

          {selectedType === "CLIENT" && (
            <div className="space-y-2">
              <Label htmlFor="codeParrainage">Code de parrainage</Label>
              <Input id="codeParrainage" {...register("codeParrainage")} />
              {errors.codeParrainage && (
                <p className="text-sm text-destructive">{errors.codeParrainage.message}</p>
              )}
            </div>
          )}

          {selectedType === "COACH" && (
            <div className="grid md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="specialites">Spécialités *</Label>
                <Input id="specialites" {...register("specialites")} />
                {errors.specialites && (
                  <p className="text-sm text-destructive">{errors.specialites.message}</p>
                )}
              </div>
              <div className="space-y-2">
                <Label htmlFor="description">Description *</Label>
                <Input id="description" {...register("description")} />
                {errors.description && (
                  <p className="text-sm text-destructive">{errors.description.message}</p>
                )}
              </div>
            </div>
          )}

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
