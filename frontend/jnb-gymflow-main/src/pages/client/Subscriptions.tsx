import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Calendar, CheckCircle2, Loader2 } from "lucide-react";
import { SubscribeDialog } from "@/components/client/SubscribeDialog";
import { abonnementsApi, paiementsApi, parrainagesApi, cartesApi } from "@/lib/api";
import { smartNotify } from "@/lib/notify";
import { toast } from "sonner";
import { useAuth } from "@/contexts/AuthContext";

interface Subscription {
  id: number;
  clientId: number;
  typeAbonnementId: number;
  typeNom: string;
  dateDebut: string;
  dateFin: string;
  statut: string;
  seancesRestantes?: number;
  offrirParParrainage?: boolean;
}

const Subscriptions = () => {
  const { user } = useAuth();
  const [subscriptions, setSubscriptions] = useState<Subscription[]>([]);
  const [loading, setLoading] = useState(true);

  // Charge la liste des abonnements du client
  const fetchSubscriptions = async () => {
    if (!user?.utilisateurId) return;
    try {
      setLoading(true);
      const response = await abonnementsApi.getActiveByClient(user.utilisateurId.toString());
      const data = (response.data || []).filter((a: any) => a.statut === 'ACTIF');
      setSubscriptions(data);
      console.log(data);
    } catch (error) {
      console.error("Erreur lors du chargement des abonnements:", error);
      toast.error("Impossible de charger les abonnements");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user?.utilisateurId) {
      fetchSubscriptions();
    }
  }, [user?.utilisateurId]);

  // Notifier l'expiration des abonnements (une seule fois via localStorage)
  useEffect(() => {
    const run = async () => {
      if (!user?.utilisateurId) return;
      try {
        const res = await abonnementsApi.getByClient(user.utilisateurId.toString());
        const all = res.data || [];
        for (const sub of all) {
          const fin = sub.dateFin ? new Date(sub.dateFin) : null;
          const expired = fin ? fin.getTime() < Date.now() : false;
          const key = `jnb_exp_notified_${sub.id}`;
          if (expired && !localStorage.getItem(key)) {
            await smartNotify({
              destId: user.utilisateurId,
              title: "Abonnement expiré",
              type: "ABONNEMENT_EXPIRE",
              message: `Votre abonnement ${sub.typeNom || ''} a expiré le ${(fin || new Date()).toLocaleDateString('fr-FR')}`,
            });
            localStorage.setItem(key, "1");
          }
        }
      } catch {}
    };
    run();
  }, [user?.utilisateurId]);

  // Handler de souscription complet :
  const handleSubscribe = async ({
    typeAbonnementId,
    montant,
    methodePaiement, // string
  }: {
    typeAbonnementId: number;
    montant: number;
    methodePaiement: string;
  }) => {
    if (!user?.utilisateurId) return;
    const clientId = user.utilisateurId;
    const dateDebut = new Date().toISOString();

    try {
      // 0. Vérifier le solde de la carte du client et diminuer le montant
      const clientCardRes = await cartesApi.getByUser(clientId);
      const clientCard = clientCardRes.data;
      if (!clientCard?.id) {
        toast.error("Carte du client introuvable");
        return;
      }
      const solde = clientCard.soldeCent ?? 0;
      if (solde < montant) {
        toast.error("Solde insuffisant sur la carte");
        return;
      }
      await cartesApi.diminuer(clientCard.id, montant);

      // 1. Créer l'abonnement
      const abRes = await abonnementsApi.create({ clientId, typeAbonnementId, dateDebut });
      const abonnementId = abRes.data.id;

      // 2. Créer le paiement
      await paiementsApi.create({
        clientId,
        abonnementId,
        montant,
        methodePaiement,
      });

      // 3. Vérifier puis valider le parrainage si nécessaire (après enregistrement)
      try {
        const parrRes = await parrainagesApi.getByFilleul(clientId);
        if (parrRes.data && parrRes.data.id && parrRes.data.valide !== true) {
          await parrainagesApi.validate(parrRes.data.id);
          try {
            const parrainId = Number(parrRes.data.parrainId);
            if (parrainId) {
              await smartNotify({
                destId: parrainId,
                title: "Parrainage validé",
                type: "PARRAINAGE_VALIDE",
                message: `Votre filleul a validé son abonnement`,
              });
            }
          } catch {}
        }
      } catch {
        // Pas de parrainage ou erreur : ignorer
      }

      // 4. Recharger la carte de l'admin (utilisateur id = 1)
      try {
        const adminCardRes = await cartesApi.getByUser("1");
        const adminCard = adminCardRes.data;
        if (adminCard?.id) {
          await cartesApi.recharger(adminCard.id, montant);
        } else {
          toast.warning?.("Carte admin introuvable, recharge non effectuée");
        }
      } catch (e) {
        // Erreur de recharge admin : notifier mais ne pas bloquer
        toast.error("Erreur lors de la recharge de la carte admin");
      }

      toast.success("Abonnement et paiement enregistrés !");
      fetchSubscriptions();
    } catch (err: any) {
      toast.error(
        err.response?.data?.message ||
        err.message ||
        "Erreur lors de la souscription"
      );
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
          <h1 className="text-3xl font-bold mb-2">Mes Abonnements</h1>
          <p className="text-muted-foreground">Gérez vos abonnements actifs</p>
        </div>
        <SubscribeDialog onSubscribed={handleSubscribe} />
      </div>

      <div className="grid md:grid-cols-2 gap-4">
        {subscriptions.length === 0 ? (
          <p className="col-span-2 text-center text-muted-foreground py-8">
            Aucun abonnement trouvé
          </p>
        ) : (
          subscriptions.map((sub) => (
            <Card key={sub.id}>
              <CardHeader>
                <CardTitle className="flex items-center justify-between">
                  <span>{sub.typeNom}</span>
                  <Badge variant={sub.statut === "ACTIF" ? "default" : "secondary"}>
                    {sub.statut === "ACTIF" ? (
                      <>
                        <CheckCircle2 className="mr-1 h-3 w-3" />
                        Actif
                      </>
                    ) : (
                      sub.statut
                    )}
                  </Badge>
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-2">
                <div className="flex items-center text-sm text-muted-foreground">
                  <Calendar className="mr-2 h-4 w-4" />
                  Du {new Date(sub.dateDebut).toLocaleDateString()} au{" "}
                  {new Date(sub.dateFin).toLocaleDateString()}
                </div>
                {sub.seancesRestantes !== undefined && (
                  <p className="text-sm text-muted-foreground">
                    Séances restantes: {sub.seancesRestantes}
                  </p>
                )}
                {sub.offrirParParrainage && (
                  <Badge variant="outline" className="text-xs">
                    Offert par parrainage
                  </Badge>
                )}
              </CardContent>
            </Card>
          ))
        )}
      </div>
    </div>
  );
};

export default Subscriptions;
