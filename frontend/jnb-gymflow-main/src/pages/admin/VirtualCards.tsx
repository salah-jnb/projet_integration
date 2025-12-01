import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Search, User, CreditCard, Plus, Loader2 } from "lucide-react";
import { cartesApi } from "@/lib/api";
import { smartNotify } from "@/lib/notify";
import { toast } from "sonner";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";

interface Card {
  id: number;
  libelle: string;
  numero: string;
  soldeCent: number;
  devise: string;
  statut: string;
  utilisateurId?: number;
}

const VirtualCards = () => {
  const [loading, setLoading] = useState(true);
  const [cards, setCards] = useState<Card[]>([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [isRechargeDialogOpen, setIsRechargeDialogOpen] = useState(false);
  const [selectedCard, setSelectedCard] = useState<Card | null>(null);
  const [rechargeAmount, setRechargeAmount] = useState("");
  const [isRecharging, setIsRecharging] = useState(false);

  useEffect(() => {
    fetchCards();
  }, []);

  const fetchCards = async () => {
    try {
      setLoading(true);
      const response = await cartesApi.getAll();
      setCards(response.data);
      console.log(response.data);
    } catch (error) {
      console.error("Erreur lors du chargement des cartes:", error);
      toast.error("Impossible de charger les cartes");
    } finally {
      setLoading(false);
    }
  };

  const filteredCards = cards.filter((card) =>
    card.libelle?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    card.numero?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleRecharge = (card: Card) => {
    setSelectedCard(card);
    setRechargeAmount("");
    setIsRechargeDialogOpen(true);
  };

  const handleSubmitRecharge = async () => {
    if (!selectedCard) {
      toast.error("Erreur: carte invalide");
      return;
    }

    if (!rechargeAmount || parseFloat(rechargeAmount) <= 0) {
      toast.error("Veuillez entrer un montant valide");
      return;
    }

    setIsRecharging(true);

    try {
      const montant = parseFloat(rechargeAmount);
      
      console.log(`Appel API: POST /api/Cartes/${selectedCard.id}/recharger`);
      console.log(`Montant: ${montant}`);

      await cartesApi.recharger(selectedCard.id.toString(), montant);

      toast.success(`Carte rechargée avec succès de ${montant} TND`);
      await smartNotify({
        destId: selectedCard.utilisateurId ?? null,
        title: "Recharge de carte",
        type: "RECHARGE_CARTE",
        message: `Votre carte a été rechargée de ${montant} TND`,
      });
      setIsRechargeDialogOpen(false);
      setSelectedCard(null);
      setRechargeAmount("");
      fetchCards();
    } catch (error: any) {
      console.error("Erreur lors de la recharge:", error);
      console.error("URL appelée:", error.config?.url);
      console.error("Réponse du serveur:", error.response?.data);
      toast.error(
        error.response?.data?.message || "Impossible de recharger la carte"
      );
    } finally {
      setIsRecharging(false);
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
      <div>
        <h1 className="text-3xl font-bold mb-2">Gestion Cartes Virtuelles</h1>
        <p className="text-muted-foreground">Vue d'ensemble des cartes clients</p>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center gap-2">
            <Search className="h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Rechercher par libellé ou numéro..."
              className="max-w-sm"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {filteredCards.length === 0 ? (
              <p className="text-center text-muted-foreground py-8">
                Aucune carte trouvée
              </p>
            ) : (
              filteredCards.map((card) => (
                <div
                  key={card.id}
                  className="flex items-center justify-between p-4 border border-border rounded-lg hover:bg-accent/50 transition-colors"
                >
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <User className="h-4 w-4 text-muted-foreground" />
                      <p className="font-semibold">{card.libelle}</p>
                    </div>
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <CreditCard className="h-4 w-4" />
                      <span>Carte #{card.numero}</span>
                    </div>
                  </div>
                  <div className="flex items-center gap-4">
                    <div className="text-right">
                      <p className="text-2xl font-bold">
                        {card.soldeCent} {card.devise}
                      </p>
                      <Badge variant="default">Actif</Badge>
                    </div>
                    <Button
                      size="sm"
                      onClick={() => handleRecharge(card)}
                    >
                      <Plus className="mr-2 h-4 w-4" />
                      Recharger
                    </Button>
                  </div>
                </div>
              ))
            )}
          </div>
        </CardContent>
      </Card>

      {/* Dialog de recharge */}
      <Dialog open={isRechargeDialogOpen} onOpenChange={setIsRechargeDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Recharger la carte</DialogTitle>
            <DialogDescription>
              Recharger la carte <strong>{selectedCard?.libelle}</strong> (#{selectedCard?.numero})
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid gap-2">
              <Label htmlFor="recharge-amount">Montant à recharger (TND) *</Label>
              <Input
                id="recharge-amount"
                type="number"
                min="0"
                step="0.01"
                placeholder="Entrez le montant"
                value={rechargeAmount}
                onChange={(e) => setRechargeAmount(e.target.value)}
                disabled={isRecharging}
              />
            </div>
            <div className="bg-blue-50 p-3 rounded-lg text-sm text-blue-800">
              <p className="font-semibold mb-1">Résumé de la recharge</p>
              <p>Solde actuel: <strong>{selectedCard?.soldeCent} {selectedCard?.devise}</strong></p>
              <p>Montant à ajouter: <strong>{rechargeAmount || "0"} {selectedCard?.devise}</strong></p>
              <p className="mt-2 pt-2 border-t border-blue-200">
                Nouveau solde: <strong>
                  {(parseFloat(selectedCard?.soldeCent.toString() || "0") + parseFloat(rechargeAmount || "0")).toFixed(2)} {selectedCard?.devise}
                </strong>
              </p>
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setIsRechargeDialogOpen(false)}
              disabled={isRecharging}
            >
              Annuler
            </Button>
            <Button onClick={handleSubmitRecharge} disabled={isRecharging}>
              {isRecharging && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Confirmer la recharge
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default VirtualCards;
