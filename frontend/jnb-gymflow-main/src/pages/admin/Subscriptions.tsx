import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Search, BadgePercent, Clock, ListChecks, Loader2, Edit, Trash2 } from "lucide-react";
import { CreateSubscriptionTypeDialog } from "@/components/admin/CreateSubscriptionTypeDialog";
import { abonnementsApi } from "@/lib/api";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
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
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { toast } from "sonner";

interface SubscriptionType {
  id: number;
  type: string;
  nom: string;
  description: string;
  dureeEnMois?: number | null;
  nombreSeances?: number | null;
  prix: number;
}

const AdminSubscriptions = () => {
  const [types, setTypes] = useState<SubscriptionType[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [editOpen, setEditOpen] = useState(false);
  const [editingType, setEditingType] = useState<SubscriptionType | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const editSchema = z
  .object({
    type: z.enum(["SALLE", "COURS_COLLECTIFS", "PACK_COACHING_5", "PACK_COACHING_10", "PACK_COACHING_20"]),
    nom: z
      .string()
      .min(2, "Nom requis")
      .max(60, "Nom trop long")
      .regex(/^[\p{L}\d][\p{L}\d ' -]{1,60}$/u, "Nom invalide (lettres, chiffres, espaces, -, ')")
      ,
    description: z
      .string()
      .min(20, "Description trop courte")
      .max(500, "Description trop longue")
      .refine((v) => /[\p{L}\d]/u.test(v), "La description doit contenir du texte"),
    dureeEnMois: z.number().int().min(1, "Durée minimale 1 mois").max(36, "Durée maximale 36 mois").optional().nullable(),
    nombreSeances: z.number().int().min(1, "Minimum 1 séance").max(100, "Maximum 100 séances").optional().nullable(),
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
      if (data.nombreSeances == null) {
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
  type EditFormData = z.infer<typeof editSchema>;
  const { register, handleSubmit, formState: { errors }, reset, setValue, watch } = useForm<EditFormData>({
    resolver: zodResolver(editSchema),
  });
  const typeValue = watch("type");

  useEffect(() => {
    fetchSubscriptions();
  }, []);

  const fetchSubscriptions = async () => {
    try {
      setLoading(true);
      const response = await abonnementsApi.getTypes();
      const typesData = response.data || [];
      setTypes(typesData);
    } catch (error) {
      console.error("Erreur lors du chargement des types:", error);
      toast.error("Impossible de charger les types d'abonnement");
    } finally {
      setLoading(false);
    }
  };

  const filteredTypes = types.filter((t) =>
    (t.nom || "").toLowerCase().includes(searchTerm.toLowerCase()) ||
    (t.type || "").toLowerCase().includes(searchTerm.toLowerCase())
  );

  const openEdit = (t: SubscriptionType) => {
    setEditingType(t);
    setEditOpen(true);
    reset({
      type: (t.type as any) || "SALLE",
      nom: t.nom,
      description: t.description,
      dureeEnMois: (t.dureeEnMois as any) ?? null,
      nombreSeances: (t.nombreSeances as any) ?? null,
      prix: Number(t.prix),
    });
  };

  const submitEdit = async (data: EditFormData) => {
    if (!editingType) return;
    try {
      const payload = {
        type: data.type,
        nom: data.nom,
        description: data.description,
        dureeEnMois: data.dureeEnMois ?? null,
        nombreSeances: data.nombreSeances ?? null,
        prix: data.prix,
      };
      await abonnementsApi.updateType(editingType.id.toString(), payload);
      toast.success("Type mis à jour");
      setEditOpen(false);
      setEditingType(null);
      fetchSubscriptions();
    } catch {
      toast.error("Échec de la mise à jour");
    }
  };

  const confirmDelete = async (id: number) => {
    try {
      await abonnementsApi.deleteType(id.toString());
      toast.success("Type supprimé");
      setDeleteId(null);
      fetchSubscriptions();
    } catch {
      toast.error("Échec de suppression");
    }
  };

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
          <h1 className="text-3xl font-bold mb-2">Types d'abonnement</h1>
          <p className="text-muted-foreground">Catalogue des abonnements disponibles</p>
        </div>
        <CreateSubscriptionTypeDialog />
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center gap-2">
            <Search className="h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Rechercher par nom ou type..."
              className="max-w-sm"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {filteredTypes.length === 0 ? (
              <p className="text-center text-muted-foreground py-8">
                Aucun type trouvé
              </p>
            ) : (
              filteredTypes.map((t) => (
                <div
                  key={t.id}
                  className="flex items-start justify-between p-4 border border-border rounded-lg hover:bg-accent/50 transition-colors"
                >
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1 flex-wrap">
                      <p className="font-semibold">{t.nom}</p>
                      <Badge variant="outline">{t.type}</Badge>
                    </div>
                    <p className="text-sm text-muted-foreground mb-2">
                      {t.description}
                    </p>
                    <div className="flex items-center gap-4 text-xs text-muted-foreground flex-wrap">
                      <div className="flex items-center gap-1">
                        <BadgePercent className="h-3 w-3" />
                        {Number(t.prix).toFixed(2)} TND
                      </div>
                      {t.dureeEnMois ? (
                        <div className="flex items-center gap-1">
                          <Clock className="h-3 w-3" />
                          {t.dureeEnMois} mois
                        </div>
                      ) : null}
                      {t.nombreSeances ? (
                        <div className="flex items-center gap-1">
                          <ListChecks className="h-3 w-3" />
                          {t.nombreSeances} séances
                        </div>
                      ) : null}
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <Button size="sm" variant="outline" onClick={() => openEdit(t)}>
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button size="sm" variant="outline" onClick={() => setDeleteId(t.id)}>
                      <Trash2 className="h-4 w-4 text-destructive" />
                    </Button>
                  </div>
                </div>
              ))
            )}
          </div>
        </CardContent>
      </Card>
      <Dialog open={editOpen} onOpenChange={setEditOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Modifier le type</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit(submitEdit)} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="type">Type *</Label>
              <Select value={typeValue} onValueChange={(v) => setValue("type", v as any)}>
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
                <p className="text-sm text-destructive">{errors.type.message as string}</p>
              )}
            </div>
            <div className="space-y-2">
              <Label htmlFor="nom">Nom *</Label>
              <Input id="nom" {...register("nom")} />
              {errors.nom && (
                <p className="text-sm text-destructive">{errors.nom.message}</p>
              )}
            </div>
            <div className="space-y-2">
              <Label htmlFor="description">Description *</Label>
              <Textarea id="description" rows={3} {...register("description")} />
              {errors.description && (
                <p className="text-sm text-destructive">{errors.description.message}</p>
              )}
            </div>
            <div className="grid md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="dureeEnMois">Durée (mois)</Label>
                <Input id="dureeEnMois" type="number" {...register("dureeEnMois", { valueAsNumber: true })} />
                {errors.dureeEnMois && (
                  <p className="text-sm text-destructive">{errors.dureeEnMois.message}</p>
                )}
              </div>
              {(typeValue?.startsWith("PACK_COACHING")) && (
                <div className="space-y-2">
                  <Label htmlFor="nombreSeances">Nombre de séances</Label>
                  <Input id="nombreSeances" type="number" {...register("nombreSeances", { valueAsNumber: true })} />
                  {errors.nombreSeances && (
                    <p className="text-sm text-destructive">{errors.nombreSeances.message}</p>
                  )}
                </div>
              )}
            </div>
            <div className="space-y-2">
              <Label htmlFor="prix">Prix (TND) *</Label>
              <Input id="prix" type="number" step="0.01" {...register("prix", { valueAsNumber: true })} />
              {errors.prix && (
                <p className="text-sm text-destructive">{errors.prix.message}</p>
              )}
            </div>
            <div className="flex justify-end gap-2">
              <Button type="button" variant="outline" onClick={() => setEditOpen(false)}>Annuler</Button>
              <Button type="submit">Enregistrer</Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
      <AlertDialog 
        open={deleteId !== null}
        onOpenChange={(open) => !open && setDeleteId(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Êtes-vous sûr ?</AlertDialogTitle>
            <AlertDialogDescription>
              Cette action est irréversible. Le type
              {deleteId !== null && types.find(t => t.id === deleteId) && (
                <> <strong> {types.find(t => t.id === deleteId)?.nom} </strong></>
              )}
              sera définitivement supprimé.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={() => setDeleteId(null)}>
              Annuler
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={() => {
                if (deleteId !== null) {
                  confirmDelete(deleteId);
                }
              }}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              Supprimer
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
};

export default AdminSubscriptions;
