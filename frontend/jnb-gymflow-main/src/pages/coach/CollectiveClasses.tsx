import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Calendar, Users, Plus, Loader2 } from "lucide-react";
import { coursCollectifsApi, reservationsCoursApi, usersApi } from "@/lib/api";
import { toast } from "sonner";
import { useAuth } from "@/contexts/AuthContext";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";

interface SeanceCoursCollectifDto {
  id: number;
  coursCollectifId: number;
  coursNom: string;
  dateSeance: string;
  placesDisponibles: number;
  annulee: boolean;
}

interface ParticipantReservation {
  id: number;
  clientId: number;
  statut: string;
  clientNom?: string;
  clientPrenom?: string;
}

const CoachCollectiveClasses = () => {
  const { user } = useAuth();
  const [sessions, setSessions] = useState<SeanceCoursCollectifDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [participantsOpen, setParticipantsOpen] = useState(false);
  const [participants, setParticipants] = useState<ParticipantReservation[]>([]);
  const [loadingParticipants, setLoadingParticipants] = useState(false);
  const [selectedSeance, setSelectedSeance] = useState<SeanceCoursCollectifDto | null>(null);

  const fetchSessions = async () => {
    if (!user?.utilisateurId) return;
    try {
      setLoading(true);
      const response = await coursCollectifsApi.getAvailableSessionsByCoach(user.utilisateurId.toString());
      setSessions(response.data);
    } catch (error) {
      console.error("Erreur lors du chargement des séances:", error);
      toast.error("Impossible de charger les séances");
    } finally {
      setLoading(false);
    }
  };

  const openParticipants = async (seance: SeanceCoursCollectifDto) => {
    setSelectedSeance(seance);
    setParticipantsOpen(true);
    setLoadingParticipants(true);
    try {
      const res = await reservationsCoursApi.getBySeance(seance.id);
      const reservations: ParticipantReservation[] = res.data.map((r: any) => ({ id: r.id, clientId: r.clientId, statut: r.statut }));
      const withClients = await Promise.all(
        reservations.map(async (r) => {
          try {
            const clientRes = await usersApi.getById(r.clientId.toString());
            return { ...r, clientNom: clientRes.data.nom, clientPrenom: clientRes.data.prenom };
          } catch {
            return { ...r, clientNom: "Inconnu", clientPrenom: "" };
          }
        })
      );
      setParticipants(withClients.filter((p) => p.statut === 'CONFIRMEE'));
    } catch (e) {
      console.error("Erreur lors du chargement des participants:", e);
      toast.error("Impossible de charger les participants");
    } finally {
      setLoadingParticipants(false);
    }
  };

  useEffect(() => {
    fetchSessions();
  }, [user?.utilisateurId]);

  const formatDate = (dateIso: string) => {
    const d = new Date(dateIso);
    return d.toLocaleString();
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
          <h1 className="text-3xl font-bold mb-2">Mes Séances de Cours Collectifs</h1>
          <p className="text-muted-foreground">Gérez vos séances et participants</p>
        </div>
      
      </div>

      <div className="grid md:grid-cols-2 gap-4">
        {sessions.length === 0 ? (
          <p className="col-span-2 text-center text-muted-foreground py-8">
            Aucune séance trouvée
          </p>
        ) : (
          sessions.map((seance) => (
            <Card key={seance.id}>
              <CardHeader>
                <CardTitle className="flex items-center justify-between">
                  <span>{seance.coursNom}</span>
                  <Badge variant={seance.annulee ? "secondary" : "default"}>
                    {seance.annulee ? "ANNULÉE" : "DISPONIBLE"}
                  </Badge>
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                  <Calendar className="h-4 w-4" />
                  <span>{formatDate(seance.dateSeance)}</span>
                </div>
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                  <Users className="h-4 w-4" />
                  <span>{seance.placesDisponibles} places disponibles</span>
                </div>
                <div className="flex gap-2 mt-4">
                  <Button size="sm" variant="outline" className="flex-1" onClick={() => openParticipants(seance)}>
                    Voir participants
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))
        )}
      </div>

      <Dialog open={participantsOpen} onOpenChange={setParticipantsOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Participants</DialogTitle>
            <DialogDescription>
              {selectedSeance ? `${selectedSeance.coursNom} - ${formatDate(selectedSeance.dateSeance)}` : "Séance"}
            </DialogDescription>
          </DialogHeader>
          {loadingParticipants ? (
            <div className="flex justify-center items-center py-6">
              <Loader2 className="h-6 w-6 animate-spin" />
            </div>
          ) : (
            <div className="space-y-2">
              {participants.length === 0 ? (
                <p className="text-muted-foreground">Aucun participant</p>
              ) : (
                participants.map((p) => (
                  <div key={p.id} className="flex items-center justify-between border rounded-md p-3">
                    <div className="flex items-center gap-2">
                      <Users className="h-4 w-4" />
                      <span>{p.clientPrenom} {p.clientNom}</span>
                    </div>
                    <Badge variant="outline">{p.statut}</Badge>
                  </div>
                ))
              )}
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default CoachCollectiveClasses;
