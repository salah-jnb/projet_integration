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
import { toast } from "sonner";
import { authApi } from "@/lib/api";
import { Lock } from "lucide-react";

const passwordSchema = z.object({
  ancienMotDePasse: z.string().min(6, "Au moins 6 caractères"),
  nouveauMotDePasse: z.string().min(6, "Au moins 6 caractères"),
  confirmMotDePasse: z.string(),
}).refine((data) => data.nouveauMotDePasse === data.confirmMotDePasse, {
  message: "Les mots de passe ne correspondent pas",
  path: ["confirmMotDePasse"],
});

type PasswordFormData = z.infer<typeof passwordSchema>;

interface ChangePasswordDialogProps {
  userId: string;
}

export const ChangePasswordDialog = ({ userId }: ChangePasswordDialogProps) => {
  const [open, setOpen] = useState(false);
  const { register, handleSubmit, formState: { errors }, reset } = useForm<PasswordFormData>({
    resolver: zodResolver(passwordSchema),
  });

  const onSubmit = async (data: PasswordFormData) => {
    try {
      await authApi.changePassword(userId, data.ancienMotDePasse, data.nouveauMotDePasse);
      toast.success("Mot de passe modifié avec succès !");
      setOpen(false);
      reset();
    } catch (error: any) {
      toast.error(error.response?.data?.message || "Erreur lors du changement de mot de passe");
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="outline">
          <Lock className="mr-2 h-4 w-4" />
          Changer le mot de passe
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Changer le mot de passe</DialogTitle>
          <DialogDescription>
            Saisissez votre ancien mot de passe et votre nouveau mot de passe
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="ancienMotDePasse">Ancien mot de passe</Label>
            <Input
              id="ancienMotDePasse"
              type="password"
              {...register("ancienMotDePasse")}
            />
            {errors.ancienMotDePasse && (
              <p className="text-sm text-destructive">{errors.ancienMotDePasse.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="nouveauMotDePasse">Nouveau mot de passe</Label>
            <Input
              id="nouveauMotDePasse"
              type="password"
              {...register("nouveauMotDePasse")}
            />
            {errors.nouveauMotDePasse && (
              <p className="text-sm text-destructive">{errors.nouveauMotDePasse.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="confirmMotDePasse">Confirmer le mot de passe</Label>
            <Input
              id="confirmMotDePasse"
              type="password"
              {...register("confirmMotDePasse")}
            />
            {errors.confirmMotDePasse && (
              <p className="text-sm text-destructive">{errors.confirmMotDePasse.message}</p>
            )}
          </div>

          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>
              Annuler
            </Button>
            <Button type="submit">Enregistrer</Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};
