import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Calendar, User, Clock, Loader2, Star } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { BookCoachingDialog } from "@/components/client/BookCoachingDialog";
import { reservationsCoachingApi, api, notationsApi } from "@/lib/api";
import { toast } from "sonner";
import { useAuth } from "@/contexts/AuthContext";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";

interface Reservation {
  id: number;
  clientId: number;
  coachId: number;
  coachNom: string;
  coachPrenom: string;
  dateSeance: string;
  dureeMinutes: number;
  typeSeance: string;
  statut: string;
  dateReservation: string;
  montant: number;
}

const Coaching = () => {
  const { user } = useAuth();
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [loading, setLoading] = useState(true);
  const [rateOpen, setRateOpen] = useState(false);
  const [currentReservationId, setCurrentReservationId] = useState<number | null>(null);
  const [currentCoachId, setCurrentCoachId] = useState<number | null>(null);
  const [note, setNote] = useState<string>("5");
  const [comment, setComment] = useState<string>("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (user?.utilisateurId) {
      fetchReservations();
    }
  }, [user?.utilisateurId]);

  const fetchReservations = async () => {
    if (!user?.utilisateurId) return;

    try {
      setLoading(true);
      const response = await reservationsCoachingApi.getByClient(user.utilisateurId.toString());
      const all = response.data || [];
      const pending = all.filter((r: any) => r.statut === "EN_ATTENTE");
      const confirmed = all.filter((r: any) => r.statut === "CONFIRMEE");
      const finished = all.filter((r: any) => r.statut === "TERMINEE");

      const finishedNotRated = (
        await Promise.all(
          finished.map(async (r: any) => {
            try {
              const n = await notationsApi.getByReservation(r.id.toString());
              if (n && n.data) return null;
              return r;
            } catch {
              return r;
            }
          })
        )
      ).filter(Boolean);

      const filtered = [...pending, ...confirmed, ...finishedNotRated];
      setReservations(filtered as any);
    } catch (error) {
      console.error("Erreur lors du chargement des réservations:", error);
      toast.error("Impossible de charger les réservations");
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (id: number) => {
    try {
      await api.delete(`/api/ReservationsCoaching/${id}`);
      toast.success("Réservation annulée avec succès");
      fetchReservations();
    } catch (error) {
      console.error("Erreur lors de l'annulation:", error);
      toast.error("Impossible d'annuler la réservation");
    }
  };

  const openRateDialog = async (reservationId: number, coachId: number) => {
    try {
      const existing = await notationsApi.getByReservation(reservationId);
      if (existing?.data) {
        toast.info("Avis déjà envoyé pour cette séance");
        return;
      }
    } catch {}
    setCurrentReservationId(reservationId);
    setCurrentCoachId(coachId);
    setNote("5");
    setComment("");
    setRateOpen(true);
  };

  const submitRating = async () => {
    if (!user?.utilisateurId || !currentReservationId || !currentCoachId) return;
    try {
      setSubmitting(true);
      await notationsApi.create({
        clientId: user.utilisateurId,
        coachId: currentCoachId,
        reservationCoachingId: currentReservationId,
        note: parseInt(note, 10),
        commentaire: comment,
      });
      toast.success("Merci pour votre avis !");
      setRateOpen(false);
      setCurrentReservationId(null);
      setCurrentCoachId(null);
      fetchReservations();
    } catch (error: any) {
      toast.error(error.response?.data?.message || "Impossible d'envoyer l'avis");
    } finally {
      setSubmitting(false);
    }
  };

  const formatTime = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleTimeString('fr-FR', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR');
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
          <h1 className="text-3xl font-bold mb-2">Mes Séances de Coaching</h1>
          <p className="text-muted-foreground">Réservez et gérez vos séances</p>
        </div>
        <BookCoachingDialog />
      </div>

      <div className="grid gap-4">
        {reservations.length === 0 ? (
          <p className="text-center text-muted-foreground py-8">
            Aucune réservation de coaching trouvée
          </p>
        ) : (
          reservations.map((res) => (
            <Card key={res.id}>
              <CardHeader>
                <CardTitle className="flex items-center justify-between">
                  <span className="flex items-center gap-2">
                    <User className="h-5 w-5" />
                    {res.coachPrenom} {res.coachNom}
                  </span>
                  <Badge
                    variant={res.statut === "CONFIRMEE" ? "default" : "secondary"}
                  >
                    {res.statut}
                  </Badge>
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-2">
                <div className="flex items-center gap-4 text-sm text-muted-foreground flex-wrap">
                  <div className="flex items-center gap-1">
                    <Calendar className="h-4 w-4" />
                    {formatDate(res.dateSeance)}
                  </div>
                  <div className="flex items-center gap-1">
                    <Clock className="h-4 w-4" />
                    {formatTime(res.dateSeance)}
                  </div>
                  <Badge variant="outline">{res.typeSeance}</Badge>
                </div>
                {/* Montant retiré de l'affichage côté client */}
                <div className="flex gap-2 mt-4">
                  <Button size="sm" variant="outline">
                    Modifier
                  </Button>
                  <Button 
                    size="sm" 
                    variant="destructive"
                    onClick={() => handleCancel(res.id)}
                    disabled={res.statut === "ANNULEE"}
                  >
                    Annuler
                  </Button>
                  {res.statut === "TERMINEE" && (
                    <Button 
                      size="sm" 
                      onClick={() => openRateDialog(res.id, res.coachId)}
                    >
                      <Star className="h-4 w-4 mr-1" /> Noter
                    </Button>
                  )}
                </div>
              </CardContent>
            </Card>
          ))
        )}
      </div>

      <Dialog open={rateOpen} onOpenChange={setRateOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Noter le coach</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>Note</Label>
              <Select value={note} onValueChange={setNote}>
                <SelectTrigger>
                  <SelectValue placeholder="Choisir une note" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="5">5 - Excellent</SelectItem>
                  <SelectItem value="4">4 - Très bien</SelectItem>
                  <SelectItem value="3">3 - Bien</SelectItem>
                  <SelectItem value="2">2 - Passable</SelectItem>
                  <SelectItem value="1">1 - Faible</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Commentaire (optionnel)</Label>
              <Textarea
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                placeholder="Partagez votre expérience"
              />
            </div>
            <div className="flex justify-end gap-2">
              <Button variant="outline" onClick={() => setRateOpen(false)}>Annuler</Button>
              <Button onClick={submitRating} disabled={submitting}>
                {submitting ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : null}
                Envoyer
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default Coaching;
