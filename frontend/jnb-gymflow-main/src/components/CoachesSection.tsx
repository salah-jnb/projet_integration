import { useEffect, useState } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { api, coachsApi } from "@/lib/api";
import { toast } from "sonner";
import { Loader2 } from "lucide-react";

interface Coach {
  utilisateurId: number;
  nom: string;
  prenom: string;
  specialite: string;
  photo?: string;
}

export const CoachesSection = () => {
  const [coaches, setCoaches] = useState<Coach[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchCoaches = async () => {
      try {
        const response = await coachsApi.getAll();
        setCoaches(response.data);
      } catch (error) {
        console.error("Erreur lors du chargement des coaches:", error);
        toast.error("Impossible de charger les coaches");
      } finally {
        setLoading(false);
      }
    };

    fetchCoaches();
  }, []);

  if (loading) {
    return (
      <section className="py-20 px-4 bg-background/50">
        <div className="max-w-7xl mx-auto flex justify-center">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      </section>
    );
  }

  return (
    <section className="py-20 px-4 bg-background/50">
      <div className="max-w-7xl mx-auto">
        <h2 className="text-4xl font-bold text-center mb-4 animate-fade-in">
          Nos Coachs Experts
        </h2>
        <p className="text-center text-muted-foreground mb-12 animate-fade-in">
          Des professionnels certifiés pour vous accompagner
        </p>
        <div className="grid md:grid-cols-3 gap-8">
          {coaches.map((coach) => (
            <Card key={coach.utilisateurId} className="hover-scale animate-fade-in">
              <CardContent className="p-6 text-center">
                {coach.photo ? (
                  <img 
                    src={coach.photo} 
                    alt={`${coach.prenom} ${coach.nom}`}
                    className="w-24 h-24 rounded-full mx-auto mb-4 object-cover"
                  />
                ) : (
                  <div className="w-24 h-24 rounded-full bg-gradient-primary mx-auto mb-4" />
                )}
                <h3 className="text-xl font-bold mb-2">
                  {coach.prenom} {coach.nom}
                </h3>
                <p className="text-muted-foreground">{coach.specialite}</p>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    </section>
  );
};
