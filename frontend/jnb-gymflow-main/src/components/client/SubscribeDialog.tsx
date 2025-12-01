import { useEffect, useState } from "react";
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
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { toast } from "sonner";
import { Plus } from "lucide-react";
import { abonnementsApi } from "@/lib/api";
import { useAuth } from "@/contexts/AuthContext";

const subscriptionSchema = z.object({
  typeAbonnementId: z.string().min(1, "Type d'abonnement requis"),
  methodePaiement: z.enum(["CARTE_BANCAIRE", "ESPECES", "CHEQUE"]),
});

type SubscriptionFormData = z.infer<typeof subscriptionSchema>;

interface SubscriptionType {
  id: number;
  nom: string;
  prix: number;
}

export const SubscribeDialog = ({ onSubscribed }: { onSubscribed?: (payload: { typeAbonnementId: number; montant: number; methodePaiement: string; }) => void }) => {
  const [open, setOpen] = useState(false);
  const [types, setTypes] = useState<SubscriptionType[]>([]);
  const [loadingTypes, setLoadingTypes] = useState(false);
  const { user } = useAuth();
  const { handleSubmit, formState: { errors }, setValue, watch, reset } = useForm<SubscriptionFormData>({
    resolver: zodResolver(subscriptionSchema),
  });

  const selectedType = watch("typeAbonnementId");

  // Charger les types d'abonnement depuis l'API
  useEffect(() => {
    const fetchTypes = async () => {
      try {
        setLoadingTypes(true);
        const res = await abonnementsApi.getTypes();
        // On suppose que l'API retourne [{ id, nom, prix, ... }]
        setTypes(res.data || []);
      } catch (err: any) {
        // En cas d'erreur, garder la liste vide
      } finally {
        setLoadingTypes(false);
      }
    };
    if (open) {
      fetchTypes();
    }
  }, [open]);

  const onSubmit = async (data: SubscriptionFormData) => {
    try {
      if (!user?.utilisateurId) {
        toast.error("Vous devez être connecté");
        return;
      }
      const selected = types.find((t) => String(t.id) === data.typeAbonnementId);
      if (!selected) {
        toast.error("Veuillez sélectionner un type d'abonnement");
        return;
      }
      onSubscribed?.({
        typeAbonnementId: selected.id,
        montant: selected.prix,
        methodePaiement: data.methodePaiement,
      });
      setOpen(false);
      reset();
    } catch (error: any) {
      toast.error(error.response?.data?.message || "Erreur lors de la souscription");
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button>
          <Plus className="mr-2 h-4 w-4" />
          Souscrire un abonnement
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Souscrire à un abonnement</DialogTitle>
          <DialogDescription>
            Choisissez votre formule et votre mode de paiement
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="typeAbonnementId">Type d'abonnement *</Label>
            <Select onValueChange={(value) => setValue("typeAbonnementId", value)}>
              <SelectTrigger>
                <SelectValue placeholder={loadingTypes ? "Chargement..." : "Sélectionner un abonnement"} />
              </SelectTrigger>
              <SelectContent>
                {types.map((sub) => (
                  <SelectItem key={sub.id} value={String(sub.id)}>
                    {sub.nom} - {sub.prix} TND
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.typeAbonnementId && (
              <p className="text-sm text-destructive">{errors.typeAbonnementId.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label>Mode de paiement *</Label>
            <RadioGroup onValueChange={(value) => setValue("methodePaiement", value as any)}>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="CARTE_BANCAIRE" id="carte" />
                <Label htmlFor="carte" className="font-normal cursor-pointer">
                  Carte bancaire
                </Label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="ESPECES" id="especes" />
                <Label htmlFor="especes" className="font-normal cursor-pointer">
                  Espèces
                </Label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="CHEQUE" id="cheque" />
                <Label htmlFor="cheque" className="font-normal cursor-pointer">
                  Chèque
                </Label>
              </div>
            </RadioGroup>
            {errors.methodePaiement && (
              <p className="text-sm text-destructive">{errors.methodePaiement.message}</p>
            )}
          </div>

          {selectedType && (
            <div className="p-4 bg-muted rounded-lg">
              <p className="text-sm font-medium">
                Total à payer :{" "}
                <span className="text-lg text-primary">
                  {types.find((s) => String(s.id) === selectedType)?.prix} TND
                </span>
              </p>
            </div>
          )}

          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>
              Annuler
            </Button>
            <Button type="submit">Confirmer</Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};
