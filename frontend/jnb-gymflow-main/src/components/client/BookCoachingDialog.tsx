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
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { toast } from "sonner";
import { Calendar } from "lucide-react";
import { reservationsCoachingApi, coachsApi } from "@/lib/api";
import { smartNotify } from "@/lib/notify";
import { useAuth } from "@/contexts/AuthContext";

const bookingSchema = z.object({
  coachId: z.string().min(1, "Coach requis"),
  dateSeance: z.string().min(1, "Date requise"),
  heureDebut: z.string().regex(/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, "Format HH:MM requis"),
  typeSeance: z.enum(["PRESENTIEL", "EN_LIGNE"]),
});

type BookingFormData = z.infer<typeof bookingSchema>;

export const BookCoachingDialog = ({ onBooked }: { onBooked?: () => void }) => {
  const [open, setOpen] = useState(false);
  const [coaches, setCoaches] = useState<Array<{ utilisateurId: number; nom: string; prenom: string; specialites?: string }>>([]);
  const [loadingCoaches, setLoadingCoaches] = useState(false);
  const [selectedCoachId, setSelectedCoachId] = useState<string | null>(null);
  const [coachDetails, setCoachDetails] = useState<any | null>(null);
  const [loadingDetails, setLoadingDetails] = useState(false);
  const [isSlotAvailable, setIsSlotAvailable] = useState<boolean | null>(null);
  const { user } = useAuth();
  const { register, handleSubmit, formState: { errors, touchedFields }, setValue, reset, watch } = useForm<BookingFormData>({
    resolver: zodResolver(bookingSchema),
  });
  const dateSeanceWatch = watch("dateSeance");
  const heureDebutWatch = watch("heureDebut");
  const coachIdWatch = watch("coachId");
  const typeSeanceWatch = watch("typeSeance");

  const normalize = (v: Partial<BookingFormData>) => ({
    coachId: (v.coachId || "").trim(),
    dateSeance: (v.dateSeance || "").trim(),
    heureDebut: (v.heureDebut || "").trim(),
    typeSeance: (v.typeSeance as any) || undefined,
  } as BookingFormData);
  const isFormValid = bookingSchema.safeParse(normalize({ coachId: coachIdWatch, dateSeance: dateSeanceWatch, heureDebut: heureDebutWatch, typeSeance: typeSeanceWatch })).success;

  const onSubmit = async (data: BookingFormData) => {
    try {
      if (!user?.utilisateurId) {
        toast.error("Vous devez être connecté");
        return;
      }
      await verifyAvailability();
      if (isSlotAvailable === false) {
        toast.error("Le créneau choisi est hors disponibilités ou déjà occupé");
        return;
      }
      // Combine date and time for API
      const dateTimeSeance = `${data.dateSeance}T${data.heureDebut}:00`;
      
      await reservationsCoachingApi.create({
        clientId: user.utilisateurId,
        coachId: parseInt(data.coachId, 10),
        dateSeance: dateTimeSeance,
        typeSeance: data.typeSeance,
      });
      await smartNotify({
        destId: parseInt(data.coachId, 10),
        title: "Nouvelle réservation coaching",
        type: "RESERVATION_COACHING",
        message: `Une nouvelle réservation est planifiée le ${new Date(dateTimeSeance).toLocaleString('fr-FR')}`,
      });
      toast.success("Séance de coaching réservée avec succès !");
      setOpen(false);
      reset();
      onBooked?.();
    } catch (error: any) {
      toast.error(error.response?.data?.message || "Erreur lors de la réservation");
    }
  };

  // Charger les coachs réels depuis l'API
  useEffect(() => {
    const loadCoaches = async () => {
      try {
        setLoadingCoaches(true);
        const resp = await coachsApi.getAll();
        const list = (resp.data || []) as Array<any>;
        setCoaches(
          list.map((c) => ({
            utilisateurId: c.utilisateurId,
            nom: c.nom,
            prenom: c.prenom,
            specialites: c.specialites,
          }))
        );
      } catch (e: any) {
        toast.error(e.response?.data?.message || "Impossible de charger les coachs");
      } finally {
        setLoadingCoaches(false);
      }
    };
    loadCoaches();
  }, []);

  // Charger les détails et disponibilités du coach sélectionné
  useEffect(() => {
    const loadDetails = async () => {
      if (!selectedCoachId) {
        setCoachDetails(null);
        return;
      }
      try {
        setLoadingDetails(true);
        const resp = await coachsApi.getDetails(selectedCoachId);
        setCoachDetails(resp.data);
      } catch (e: any) {
        toast.error(e.response?.data?.message || "Impossible de charger le profil du coach");
        setCoachDetails(null);
      } finally {
        setLoadingDetails(false);
      }
    };
    loadDetails();
  }, [selectedCoachId]);

  // Vérifier la disponibilité du créneau choisi (durée par défaut: 60 minutes)
  const verifyAvailability = async () => {
    const coachId = selectedCoachId;
    const date = dateSeanceWatch;
    const time = heureDebutWatch;
    if (!coachId || !date || !time) {
      setIsSlotAvailable(null);
      return;
    }
    if (!coachDetails || !Array.isArray(coachDetails.disponibilites)) {
      setIsSlotAvailable(null);
      return;
    }
    const jours = ["DIMANCHE", "LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI", "SAMEDI"];
    const [yy, mm, dd] = (date || "").split("-").map((x) => parseInt(x, 10));
    const jour = Number.isFinite(yy) && Number.isFinite(mm) && Number.isFinite(dd)
      ? jours[new Date(yy, (mm - 1), dd).getDay()]
      : jours[new Date(date).getDay()];
    const toMinutes = (t: string) => {
      const parts = t.split(":");
      const h = parseInt(parts[0] || "0", 10);
      const m = parseInt(parts[1] || "0", 10);
      return h * 60 + m;
    };
    const selectedMinutes = toMinutes(time);
    const fullDuration = 60;
    const dispoForDay = coachDetails.disponibilites.filter((d: any) => d.actif && String(d.jourSemaine).toUpperCase() === jour);
    const inRange = dispoForDay.some((d: any) => {
      const debStr = typeof d.heureDebut === 'string' ? d.heureDebut : String(d.heureDebut);
      const finStr = typeof d.heureFin === 'string' ? d.heureFin : String(d.heureFin);
      const deb = toMinutes(debStr);
      const fin = toMinutes(finStr);
      return selectedMinutes >= deb && (selectedMinutes + fullDuration) <= fin;
    });
    if (!inRange) {
      setIsSlotAvailable(false);
      return;
    }
    try {
      const dateTimeSeance = `${date}T${time}:00`;
      const res = await reservationsCoachingApi.checkAvailability(coachId, dateTimeSeance, 60);
      const dispo = typeof res.data === 'object' && res.data !== null && 'disponible' in res.data
        ? Boolean((res.data as any).disponible)
        : Boolean(res.data);
      setIsSlotAvailable(dispo);
    } catch {
      setIsSlotAvailable(null);
    }
  };

  useEffect(() => {
    verifyAvailability();
  }, [selectedCoachId, dateSeanceWatch, heureDebutWatch, coachDetails]);

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button>
          <Calendar className="mr-2 h-4 w-4" />
          Réserver une séance
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Réserver une séance de coaching</DialogTitle>
          <DialogDescription>
            Sélectionnez votre coach et le créneau souhaité
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="coachId">Coach *</Label>
            <Select onValueChange={(value) => { setValue("coachId", value, { shouldTouch: true }); setSelectedCoachId(value); setIsSlotAvailable(null); setTimeout(() => verifyAvailability(), 0); }}>
              <SelectTrigger>
                <SelectValue placeholder="Sélectionner un coach" />
              </SelectTrigger>
              <SelectContent>
                {loadingCoaches ? (
                  <SelectItem value="loading" disabled>
                    Chargement...
                  </SelectItem>
                ) : (
                  coaches.map((coach) => (
                    <SelectItem key={coach.utilisateurId} value={coach.utilisateurId.toString()}>
                      {coach.prenom} {coach.nom}{coach.specialites ? ` - ${coach.specialites}` : ""}
                    </SelectItem>
                  ))
                )}
              </SelectContent>
            </Select>
            {touchedFields.coachId && errors.coachId && (
              <p className="text-sm text-destructive">{errors.coachId.message}</p>
            )}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="dateSeance">Date *</Label>
              <Input
                id="dateSeance"
                type="date"
                {...register("dateSeance")}
                onChange={(e) => {
                  setValue("dateSeance", e.target.value, { shouldDirty: true, shouldTouch: true });
                  setIsSlotAvailable(null);
                  verifyAvailability();
                }}
              />
              {touchedFields.dateSeance && errors.dateSeance && (
                <p className="text-sm text-destructive">{errors.dateSeance.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="heureDebut">Heure *</Label>
              <Input
                id="heureDebut"
                type="time"
                {...register("heureDebut")}
                onChange={(e) => {
                  setValue("heureDebut", e.target.value, { shouldDirty: true, shouldTouch: true });
                  setIsSlotAvailable(null);
                  verifyAvailability();
                }}
              />
              {touchedFields.heureDebut && errors.heureDebut && (
                <p className="text-sm text-destructive">{errors.heureDebut.message}</p>
              )}
            </div>
          </div>

          {selectedCoachId && (
            <div className="space-y-3 border rounded-md p-3">
              <p className="font-semibold">Profil du coach</p>
              {loadingDetails ? (
                <p className="text-sm text-muted-foreground">Chargement du profil...</p>
              ) : coachDetails ? (
                <div className="space-y-2 text-sm text-muted-foreground">
                  <p>
                    <span className="font-medium">Spécialités:</span> {coachDetails.specialites || "—"}
                  </p>
                  <p>
                    <span className="font-medium">Description:</span> {coachDetails.description || "—"}
                  </p>
                  <p>
                    <span className="font-medium">Note:</span> {coachDetails.noteGlobale ?? "—"} ({coachDetails.nombreAvis ?? 0} avis)
                  </p>
                  <div>
                    <p className="font-semibold mb-1">Disponibilités</p>
                    {Array.isArray(coachDetails.disponibilites) && coachDetails.disponibilites.length > 0 ? (
                      <div className="grid md:grid-cols-2 gap-2">
                        {coachDetails.disponibilites.map((d: any) => (
                          <div key={d.id} className="flex items-center justify-between rounded border p-2">
                            <span>{d.jourSemaine}</span>
                            <span>{String(d.heureDebut)} - {String(d.heureFin)}</span>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <p className="text-sm">Aucune disponibilité renseignée</p>
                    )}
                  </div>
                </div>
              ) : (
                <p className="text-sm text-muted-foreground">Sélectionnez un coach pour voir son profil</p>
              )}
            </div>
          )}

          {selectedCoachId && (
            <div className="flex items-center gap-2">
              <Button
                type="button"
                variant="outline"
                onClick={verifyAvailability}
              >
                Vérifier la disponibilité
              </Button>
              {isSlotAvailable === true && (
                <span className="text-green-600 text-sm">Disponible</span>
              )}
              {isSlotAvailable === false && (
                <span className="text-destructive text-sm">Indisponible</span>
              )}
            </div>
          )}

          {/* Durée retirée côté UI; la durée par défaut est gérée serveur */}

          <div className="space-y-2">
            <Label htmlFor="typeSeance">Type de séance *</Label>
            <Select onValueChange={(value) => setValue("typeSeance", value as any, { shouldTouch: true })}>
              <SelectTrigger>
                <SelectValue placeholder="Sélectionner le type" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="PRESENTIEL">Présentiel</SelectItem>
                <SelectItem value="EN_LIGNE">En ligne</SelectItem>
              </SelectContent>
            </Select>
            {touchedFields.typeSeance && errors.typeSeance && (
              <p className="text-sm text-destructive">{errors.typeSeance.message}</p>
            )}
          </div>

          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>
              Annuler
            </Button>
            <Button type="submit" disabled={!isFormValid || isSlotAvailable === false}>Réserver</Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};
