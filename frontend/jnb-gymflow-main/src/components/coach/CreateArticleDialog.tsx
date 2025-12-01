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
import { toast } from "sonner";
import { Plus } from "lucide-react";
import { api } from "@/lib/api";
import { useAuth } from "@/contexts/AuthContext";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

const articleSchema = z.object({
  titre: z.string().min(5, "Titre trop court"),
  contenu: z.string().min(1, "Contenu requis"),
  statut: z.enum(["BROUILLON", "EN_ATTENTE_VALIDATION"]).default("EN_ATTENTE_VALIDATION"),
});

type ArticleFormData = z.infer<typeof articleSchema>;

export const CreateArticleDialog = ({ onCreated }: { onCreated?: () => void }) => {
  const [open, setOpen] = useState(false);
  const { user } = useAuth();
  const [file, setFile] = useState<File | null>(null);
  const [fileError, setFileError] = useState<string | null>(null);
  const { register, handleSubmit, formState: { errors }, reset, setValue, watch } = useForm<ArticleFormData>({
    resolver: zodResolver(articleSchema),
  });
  const statut = watch("statut", "EN_ATTENTE_VALIDATION");

  const onSubmit = async (data: ArticleFormData) => {
    try {
      if (!user?.utilisateurId) {
        toast.error("Vous devez être connecté");
        return;
      }
      if (fileError) {
        toast.error(fileError);
        return;
      }
      const formData = new FormData();
      formData.append("coachId", String(user.utilisateurId));
      formData.append("titre", data.titre);
      formData.append("contenu", data.contenu);
      formData.append("statut", data.statut);
      if (file) {
        formData.append("image", file);
      }
      await api.post(`/api/Articles`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      toast.success(statut === "EN_ATTENTE_VALIDATION" ? "Article envoyé pour validation" : "Article enregistré en brouillon");
      setOpen(false);
      reset();
      setFile(null);
      setFileError(null);
      onCreated?.();
    } catch (error: any) {
      toast.error(error.response?.data?.message || "Erreur lors de la création");
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button>
          <Plus className="mr-2 h-4 w-4" />
          Nouvel article
        </Button>
      </DialogTrigger>
      <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Créer un nouvel article</DialogTitle>
          <DialogDescription>
            Votre article sera soumis à modération avant publication
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="titre">Titre *</Label>
            <Input id="titre" {...register("titre")} placeholder="Un titre accrocheur..." />
            {errors.titre && (
              <p className="text-sm text-destructive">{errors.titre.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="statut">Statut *</Label>
            <Select value={statut} onValueChange={(v) => setValue("statut", v as any)}>
              <SelectTrigger>
                <SelectValue placeholder="Choisir le statut" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="EN_ATTENTE_VALIDATION">Envoyer pour validation</SelectItem>
                <SelectItem value="BROUILLON">Enregistrer en brouillon</SelectItem>
              </SelectContent>
            </Select>
            {errors.statut && (
              <p className="text-sm text-destructive">{errors.statut.message as string}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="image">Image (optionnel)</Label>
            <Input
              id="image"
              type="file"
              accept="image/*"
              onChange={(e) => {
                const f = e.target.files?.[0] || null;
                if (!f) {
                  setFile(null);
                  setFileError(null);
                  return;
                }
                const allowed = ["image/jpeg", "image/png", "image/webp"];
                const maxSize = 5 * 1024 * 1024;
                if (!allowed.includes(f.type)) {
                  setFile(null);
                  setFileError("Format d'image invalide (JPEG, PNG, WEBP)");
                  return;
                }
                if (f.size > maxSize) {
                  setFile(null);
                  setFileError("Image trop volumineuse (max 5MB)");
                  return;
                }
                setFileError(null);
                setFile(f);
              }}
            />
            {fileError && <p className="text-sm text-destructive">{fileError}</p>}
          </div>

          <div className="space-y-2">
            <Label htmlFor="contenu">Contenu *</Label>
            <Textarea
              id="contenu"
              {...register("contenu")}
              rows={12}
              placeholder="Rédigez votre article ici..."
            />
            {errors.contenu && (
              <p className="text-sm text-destructive">{errors.contenu.message}</p>
            )}
          </div>

          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>
              Annuler
            </Button>
            <Button type="submit">Publier</Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};
