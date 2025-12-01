import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Calendar, Clock, User, Loader2 } from "lucide-react";
import { reservationsCoachingApi, usersApi, api } from "@/lib/api";
import { toast } from "sonner";
import { useAuth } from "@/contexts/AuthContext";

interface CoachingReservation {
  id: number;
  clientId: number;
  coachId: number;
  dateSeance: string;
  dureeMinutes: number;
  typeSeance: string;
  statut: string;
  dateReservation: string;
  montant: number;
}

interface ReservationWithClientInfo extends CoachingReservation {
  clientNom?: string;
  clientPrenom?: string;
}

const CoachReservations = () => {
  const { user } = useAuth();
  const [reservations, setReservations] = useState<ReservationWithClientInfo[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (user?.utilisateurId) {
      fetchReservations();
    }
  }, [user?.utilisateurId]);

  const fetchReservations = async () => {
    if (!user?.utilisateurId) return;

    try {
      setLoading(true);
      const response = await reservationsCoachingApi.getByCoach(user.utilisateurId.toString());
      const reservationsData = (response.data || []).filter(
        (r: any) => r.statut === 'CONFIRMEE' || r.statut === 'EN_ATTENTE'
      );


      // Récupérer les informations des clients pour chaque réservation
      const reservationsWithClients = await Promise.all(
        reservationsData.map(async (reservation: CoachingReservation) => {
          try {
            const clientResponse = await usersApi.getById(reservation.clientId.toString());
            return {
              ...reservation,
              clientNom: clientResponse.data.nom,
              clientPrenom: clientResponse.data.prenom,
            };
          } catch (error) {
            console.error(`Erreur lors du chargement du client ${reservation.clientId}:`, error);
            return {
              ...reservation,
              clientNom: "Inconnu",
              clientPrenom: "",
            };
          }
        })
      );

      setReservations(reservationsWithClients);
      console.log(reservationsWithClients);
    } catch (error) {
      console.error("Erreur lors du chargement des réservations:", error);
      toast.error("Impossible de charger les réservations");
    } finally {
      setLoading(false);
    }
  };

  // Affichage uniquement des réservations de coaching — aucune séance collective ici

  const handleConfirm = async (id: number) => {
    try {
      await api.put(`/api/ReservationsCoaching/${id}/confirmer`);
      toast.success("Réservation confirmée avec succès");
      fetchReservations();
    } catch (error) {
      console.error("Erreur lors de la confirmation:", error);
      toast.error("Impossible de confirmer la réservation");
    }
  };

  const handleComplete = async (id: number) => {
    try {
      await reservationsCoachingApi.complete(id.toString());
      toast.success("Séance marquée comme terminée");
      fetchReservations();
    } catch (error) {
      console.error("Erreur lors de la complétion:", error);
      toast.error("Impossible de marquer la séance comme terminée");
    }
  };

  const handleReject = async (id: number) => {
    try {
      await api.delete(`/api/ReservationsCoaching/${id}`);
      toast.success("Réservation refusée avec succès");
      fetchReservations();
    } catch (error) {
      console.error("Erreur lors du refus:", error);
      toast.error("Impossible de refuser la réservation");
    }
  };

  const formatDateTime = (dateString: string) => {
    const date = new Date(dateString);
    return {
      date: date.toLocaleDateString('fr-FR'),
      time: date.toLocaleTimeString('fr-FR', { 
        hour: '2-digit', 
        minute: '2-digit' 
      })
    };
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
      <div>
        <h1 className="text-3xl font-bold mb-2">Mes Réservations</h1>
        <p className="text-muted-foreground">Planning de vos séances coaching</p>
      </div>

      {/* Section cours collectifs supprimée pour ne montrer que le coaching */}

      <div className="grid gap-4">
        {reservations.length === 0 ? (
          <p className="text-center text-muted-foreground py-8">
            Aucune réservation trouvée
          </p>
        ) : (
          reservations.map((res) => {
            const { date, time } = formatDateTime(res.dateSeance);

            return (
              <Card key={res.id}>
                <CardHeader>
                  <CardTitle className="flex items-center justify-between">
                    <span className="flex items-center gap-2">
                      <User className="h-5 w-5" />
                      {res.clientPrenom} {res.clientNom}
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
                      {date}
                    </div>
                    <div className="flex items-center gap-1">
                      <Clock className="h-4 w-4" />
                      {time} - {res.dureeMinutes} min
                    </div>
                    <Badge variant="outline">{res.typeSeance}</Badge>
                  </div>
                  {res.montant && (
                    <p className="text-sm text-muted-foreground">
                      Montant: {res.montant} TND
                    </p>
                  )}
                  {res.statut === "EN_ATTENTE" && (
                    <div className="flex gap-2 mt-4">
                      <Button 
                        size="sm"
                        onClick={() => handleConfirm(res.id)}
                      >
                        Confirmer
                      </Button>
                      <Button 
                        size="sm" 
                        variant="outline"
                        onClick={() => handleReject(res.id)}
                      >
                        Refuser
                      </Button>
                    </div>
                  )}
                  {res.statut === "CONFIRMEE" && (
                    <div className="flex gap-2 mt-4">
                      <Button 
                        size="sm"
                        variant="outline"
                        onClick={() => handleComplete(res.id)}
                        disabled={new Date(res.dateSeance).getTime() > Date.now() - 30 * 60 * 1000}
                      >
                        Terminer
                      </Button>
                    </div>
                  )}
                </CardContent>
              </Card>
            );
          })
        )}
      </div>
    </div>
  );
};

export default CoachReservations;
