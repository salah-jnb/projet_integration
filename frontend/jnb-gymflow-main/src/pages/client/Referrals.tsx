import { useEffect, useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Users, Copy, CheckCircle2, Gift, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { parrainagesApi } from "@/lib/api";
import { useAuth } from "@/contexts/AuthContext";

interface ApiParrainage {
  id: number;
  parrainId: number;
  filleulId: number;
  filleulNom: string;
  filleulPrenom: string;
  filleulEmail: string;
  dateInscriptionFilleul: string;
  valide: boolean;
  dateValidation?: string;
  moisGratuitAttribue?: boolean;
  codeParrainage?: string; // au cas où l’API le renvoie ici
}

interface Filleul {
  id: number;
  nomComplet: string;
  email: string;
  statut: "VALIDE" | "EN_ATTENTE";
  dateCreation: string;
  dateValidation?: string;
  moisGratuitAttribue?: boolean;
}

const TARGET_VALIDATIONS = 5;

const Referrals = () => {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [referralCode, setReferralCode] = useState<string>("");
  const [referrals, setReferrals] = useState<Filleul[]>([]);
  const [validatedCount, setValidatedCount] = useState<number>(0);

  useEffect(() => {
    if (!user?.utilisateurId) return;
    fetchData(user.utilisateurId.toString());
  }, [user?.utilisateurId]);

  const fetchData = async (parrainId: string) => {
    try {
      setLoading(true);
      const [listRes, countRes] = await Promise.all([
        parrainagesApi.getByParrain(parrainId),
        parrainagesApi.getCount(parrainId),
      ]);

      console.log("Parrainages - brut:", listRes.data);
      console.log("Parrainages - count:", countRes.data);

      const list: Filleul[] = (listRes.data as ApiParrainage[]).map((r) => ({
        id: r.id,
        nomComplet: `${r.filleulPrenom} ${r.filleulNom}`.trim(),
        email: r.filleulEmail,
        statut: r.valide ? "VALIDE" : "EN_ATTENTE",
        dateCreation: r.dateInscriptionFilleul,
        dateValidation: r.dateValidation,
        moisGratuitAttribue: r.moisGratuitAttribue,
      }));

      console.log("Parrainages - mappés:", list);

      // Récupère le code de parrainage via l'API dédiée
      try {
        const codeRes = await parrainagesApi.getCode(parrainId);
        setReferralCode(String(codeRes.data || ""));
      } catch (err: any) {
        // 404 si pas trouvé: laisse vide sans bloquer
        if (err?.response?.status !== 404) {
          console.error("Erreur code parrainage:", err);
        }
      }

      setReferrals(list);
      setValidatedCount(Number(countRes.data) || 0);
    } catch (e) {
      console.error("Erreur parrainage:", e);
      toast.error("Impossible de charger les données de parrainage");
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = async () => {
    try {
      if (!referralCode) {
        toast.error("Aucun code de parrainage à copier");
        return;
      }
      console.log("Code copié:", referralCode);
      await navigator.clipboard.writeText(referralCode);
      toast.success("Code copié dans le presse-papier !");
    } catch (e) {
      console.error("Erreur copie:", e);
      toast.error("Impossible de copier le code");
    }
  };

  const validatedLocal = useMemo(
    () => referrals.filter((r) => r.statut === "VALIDE").length,
    [referrals]
  );
  const progressCount = validatedCount || validatedLocal;
  const progress = Math.min(100, Math.round((progressCount / TARGET_VALIDATIONS) * 100));

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
        <h1 className="text-3xl font-bold mb-2">Mon Parrainage</h1>
        <p className="text-muted-foreground">
          Parrainez vos amis et gagnez des récompenses
        </p>
      </div>

      <Card className="bg-gradient-accent">
        <CardContent className="pt-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-white/80 mb-2">Votre Code de Parrainage</p>
              <p className="text-3xl font-bold text-white mb-4">{referralCode || "—"}</p>
              <Button variant="secondary" size="sm" onClick={handleCopy} disabled={!referralCode}>
                <Copy className="mr-2 h-4 w-4" />
                Copier le code
              </Button>
            </div>
            <Users className="h-16 w-16 text-white/80" />
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Gift className="h-5 w-5" />
            Récompense
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-muted-foreground">
            Parrainez {TARGET_VALIDATIONS} amis et recevez <strong>1 mois gratuit</strong> d'abonnement !
          </p>
          <div className="mt-4">
            <div className="flex items-center gap-2 mb-2">
              <span className="text-sm text-muted-foreground">Progression</span>
              <Badge>{progressCount}/{TARGET_VALIDATIONS}</Badge>
            </div>
            <div className="w-full bg-secondary rounded-full h-2">
              <div
                className="bg-accent h-2 rounded-full transition-all"
                style={{ width: `${progress}%` }}
              />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Mes Filleuls</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {referrals.length === 0 ? (
              <p className="text-muted-foreground">Aucun filleul pour le moment</p>
            ) : (
              referrals.map((ref) => (
                <div
                  key={ref.id}
                  className="flex items-center justify-between p-3 border border-border rounded-lg"
                >
                  <div>
                    <p className="font-medium">{ref.nomComplet}</p>
                   
                    <p className="text-sm text-muted-foreground">
                      {new Date(ref.dateCreation).toLocaleDateString("fr-FR")}
                    </p>
                    {ref.dateValidation && (
                      <p className="text-xs text-muted-foreground">
                        Validé le {new Date(ref.dateValidation).toLocaleDateString("fr-FR")}
                      </p>
                    )}
                    {ref.moisGratuitAttribue && (
                      <Badge className="mt-1" variant="outline">Mois gratuit attribué</Badge>
                    )}
                  </div>
                  <Badge variant={ref.statut === "VALIDE" ? "default" : "secondary"}>
                    {ref.statut === "VALIDE" && <CheckCircle2 className="mr-1 h-3 w-3" />}
                    {ref.statut}
                  </Badge>
                </div>
              ))
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default Referrals;
