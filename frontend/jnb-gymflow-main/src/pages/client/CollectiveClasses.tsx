import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Calendar, Clock, User, Users, Loader2 } from "lucide-react";
import { api, coursCollectifsApi, reservationsCoursApi } from "@/lib/api";
import { smartNotify } from "@/lib/notify";
import { toast } from "sonner";
import { useAuth } from "@/contexts/AuthContext";

interface AvailableSeance {
  id: number;
  coursCollectifId: number;
  coursNom: string;
  dateSeance: string;
  placesDisponibles: number;
  annulee: boolean;
}

type ReservationBySeance = Record<number, { reservationId: number; statut: string; delaiAnnulationHeures?: number } | undefined>;

const CollectiveClasses = () => {
  const { user } = useAuth();
  const [seances, setSeances] = useState<AvailableSeance[]>([]);
  const [reservationsBySeance, setReservationsBySeance] = useState<ReservationBySeance>({});
  const [loading, setLoading] = useState(true);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [seancesRes, myResRes] = await Promise.all([
        coursCollectifsApi.getAvailableSessions(),
        user?.utilisateurId ? reservationsCoursApi.getByClient(user.utilisateurId) : Promise.resolve({ data: [] }),
      ]);

      setSeances(seancesRes.data);
      const map: ReservationBySeance = {};
      (myResRes.data || [])
        .filter((r: any) => r.statut !== "ANNULEE")
        .forEach((r: any) => {
          map[r.seanceCoursCollectifId] = {
            reservationId: r.id,
            statut: r.statut,
            delaiAnnulationHeures: r.delaiAnnulationHeures,
          };
        });
      setReservationsBySeance(map);
    } catch (error) {
      console.error("Erreur lors du chargement des données:", error);
      toast.error("Impossible de charger les séances");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [user?.utilisateurId]);

  const handleReserve = async (seanceId: number) => {
    if (!user?.utilisateurId) {
      toast.error("Vous devez être connecté pour réserver");
      return;
    }
    try {
      const seance = seances.find(s => s.id === seanceId);
      let coachId: number | null = null;
      if (seance) {
        try {
          const coursResp = await coursCollectifsApi.getById(seance.coursCollectifId);
          coachId = Number(coursResp.data?.coachId) || null;
        } catch {}
      }
      await reservationsCoursApi.create({
        clientId: user.utilisateurId,
        seanceCoursCollectifId: seanceId,
        delaiAnnulationHeures: 24,
      });
      if (coachId) {
        await smartNotify({
          destId: coachId,
          title: "Nouvelle réservation cours collectif",
          type: "RESERVATION_COURS",
          message: `Réservation effectuée pour la séance #${seanceId}`,
        });
      }
      toast.success("Séance réservée avec succès");
      await fetchData();
    } catch (error: any) {
      if (error?.response?.status === 409) {
        toast.error("Vous avez déjà réservé cette séance");
      } else {
        toast.error(error?.response?.data?.message || "Impossible de réserver la séance");
      }
      console.error("Erreur lors de la réservation:", error);
    }
  };

  const handleCancel = async (seanceId: number) => {
    const resInfo = reservationsBySeance[seanceId];
    if (!resInfo) return;
    try {
      await api.delete(`/api/ReservationsCours/${resInfo.reservationId}`);
      const seance = seances.find(s => s.id === seanceId);
      let coachId: number | null = null;
      if (seance) {
        try {
          const coursResp = await coursCollectifsApi.getById(seance.coursCollectifId);
          coachId = Number(coursResp.data?.coachId) || null;
        } catch {}
      }
      if (coachId) {
        await smartNotify({
          destId: coachId,
          title: "Réservation annulée",
          type: "RESERVATION_COURS_ANNULEE",
          message: `Réservation annulée pour la séance #${seanceId}`,
        });
      }
      toast.success("Réservation annulée avec succès");
      await fetchData();
    } catch (error) {
      console.error("Erreur lors de l'annulation:", error);
      toast.error("Impossible d'annuler la réservation");
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
        <h1 className="text-3xl font-bold mb-2">Séances de cours collectifs disponibles</h1>
        <p className="text-muted-foreground">Réservez une séance. Annulation possible jusqu'à 24h avant l'heure de séance.</p>
      </div>

      <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
        {seances.length === 0 ? (
          <p className="col-span-full text-center text-muted-foreground py-8">
            Aucune séance disponible
          </p>
        ) : (
          seances.map((seance) => {
            const { date, time } = formatDateTime(seance.dateSeance);
            const reservationInfo = reservationsBySeance[seance.id];
            const hasCancelledOnce = reservationInfo?.statut === "ANNULEE";
            const reserved = Boolean(reservationInfo && reservationInfo.statut !== "ANNULEE");

            return (
              <Card key={seance.id}>
                <CardHeader>
                  <CardTitle className="flex items-center justify-between">
                    <span>{seance.coursNom}</span>
                    <Badge variant={seance.placesDisponibles > 0 ? "default" : "secondary"}>
                      {seance.placesDisponibles} places restantes
                    </Badge>
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  <div className="flex items-center gap-2 text-sm text-muted-foreground">
                    <Calendar className="h-4 w-4" />
                    <span>{date}</span>
                  </div>
                  <div className="flex items-center gap-2 text-sm text-muted-foreground">
                    <Clock className="h-4 w-4" />
                    <span>{time}</span>
                  </div>
                  {reserved && reservationInfo?.delaiAnnulationHeures && (
                    <p className="text-xs text-muted-foreground">
                      Annulation possible jusqu'à {reservationInfo.delaiAnnulationHeures}h avant
                    </p>
                  )}
                  {reserved ? (
                    <Button
                      className="w-full mt-4"
                      variant="destructive"
                      size="sm"
                      onClick={() => handleCancel(seance.id)}
                      disabled={false}
                    >
                      Annuler la réservation
                    </Button>
                  ) : (
                    <Button
                      className="w-full mt-4"
                      variant="default"
                      size="sm"
                      onClick={() => handleReserve(seance.id)}
                      disabled={seance.placesDisponibles <= 0 || seance.annulee || hasCancelledOnce}
                    >
                      Réserver cette séance
                    </Button>
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

export default CollectiveClasses;
