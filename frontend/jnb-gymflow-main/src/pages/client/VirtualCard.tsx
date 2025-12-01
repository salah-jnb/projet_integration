import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { CreditCard, Plus, ArrowUpDown, Loader2 } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { api, cartesApi, transfertsApi, usersApi } from "@/lib/api";
import { toast } from "sonner";
import { useAuth } from "@/contexts/AuthContext";

interface VirtualCardData {
  soldeCent: number;
  id: number;
  statut: string;
}

interface Transfer {
  id: number;
  montantCent: number;
  motif: string;
  dateTransfert: string;
  statut: string;
  emetteurCarteId: number;
  recepteurCarteId: number;
}

const VirtualCard = () => {
  const { user } = useAuth();
  const [card, setCard] = useState<VirtualCardData | null>(null);
  const [transfers, setTransfers] = useState<Transfer[]>([]);
  const [loading, setLoading] = useState(true);
  const [isTransferDialogOpen, setIsTransferDialogOpen] = useState(false);

  // Recherche / sélection recepteur
  const [searchTerm, setSearchTerm] = useState("");
  const [users, setUsers] = useState<any[]>([]);
  const [selectedUser, setSelectedUser] = useState<any | null>(null);
  const [receiverCardId, setReceiverCardId] = useState<number | null>(null);

  // Champs de transfert
  const [amount, setAmount] = useState<string>("");
  const [motif, setMotif] = useState<string>("");
  const [devise, setDevise] = useState<string>("TND");
  const [submitting, setSubmitting] = useState(false);

  const fetchCard = async () => {
    if (!user?.utilisateurId) return;
    
    try {
      setLoading(true);
      const cardResponse = await cartesApi.getByUser(user.utilisateurId);
      setCard(cardResponse.data);
     
      if (cardResponse.data?.id) {
        const transfersResponse = await transfertsApi.getByCarte(cardResponse.data.id);
        setTransfers(transfersResponse.data);
      }
    } catch (error) {
      console.error("Erreur lors du chargement de la carte:", error);
      toast.error("Impossible de charger les données de la carte");
    } finally {
      setLoading(false);
    }
  };

  const openTransferDialog = async () => {
    try {
      setIsTransferDialogOpen(true);
      // Charger liste utilisateurs pour recherche
      const usersResp = await usersApi.getAll();
      setUsers(usersResp.data || []);
    } catch (error) {
      console.error("Erreur chargement utilisateurs:", error);
      toast.error("Impossible de charger les utilisateurs");
    }
  };

  const filteredUsers = users.filter((u) => {
    const term = searchTerm.toLowerCase();
    return (
      u?.nom?.toLowerCase().includes(term) ||
      u?.prenom?.toLowerCase().includes(term) ||
      u?.email?.toLowerCase().includes(term) ||
      (u?.telephone || "").toLowerCase().includes(term)
    );
  });

  const handleSelectUser = async (u: any) => {
    setSelectedUser(u);
    try {
      const carteResp = await cartesApi.getByUser(u.id);
      const c = carteResp.data;
      if (!c?.id) {
        setReceiverCardId(null);
        toast.error("Aucune carte virtuelle pour cet utilisateur");
        return;
      }
      setReceiverCardId(c.id);
    } catch (error) {
      console.error("Erreur récupération carte recepteur:", error);
      toast.error("Impossible de récupérer la carte du recepteur");
    }
  };

  const handleSubmitTransfer = async () => {
    if (!card?.id) {
      toast.error("Votre carte virtuelle est introuvable");
      return;
    }
    if (!receiverCardId) {
      toast.error("Veuillez sélectionner un recepteur avec carte valide");
      return;
    }
    if (!amount || parseFloat(amount) <= 0) {
      toast.error("Veuillez entrer un montant valide");
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        emetteurCarteId: card.id,
        recepteurCarteId: receiverCardId,
        montantEuro: parseFloat(amount),
        motif: motif || "Transfert",
        devise,
      };
      const resp = await transfertsApi.create(payload);
      toast.success("Transfert effectué avec succès");
      setIsTransferDialogOpen(false);
      setSelectedUser(null);
      setReceiverCardId(null);
      setAmount("");
      setMotif("");
      setDevise("TND");
      // Rafraîchir liste des transferts et carte
      await fetchCard();
    } catch (error: any) {
      console.error("Erreur transfert:", error);
      toast.error(error?.response?.data?.message || "Échec du transfert");
    } finally {
      setSubmitting(false);
    }
  };

  useEffect(() => {
    if (user?.utilisateurId) {
      fetchCard();
    }
  }, [user?.utilisateurId]);

  if (loading) {
    return (
      <div className="p-6 flex justify-center items-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (!card) {
    return (
      <div className="p-6">
        <p className="text-center text-muted-foreground py-8">
          Aucune carte virtuelle trouvée
        </p>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <div>
        <h1 className="text-3xl font-bold mb-2">Ma Carte Virtuelle</h1>
        <p className="text-muted-foreground">Gérez votre solde et transactions</p>
      </div>

      <Card className="bg-gradient-accent">
        <CardContent className="pt-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-white/80 mb-1">Solde Disponible</p>
              <p className="text-4xl font-bold text-white">{(card.soldeCent ?? 0).toFixed(2)} TND</p>
              <p className="text-sm text-white/60 mt-1">
               
              </p>
            </div>
            <CreditCard className="h-12 w-12 text-white/80" />
          </div>
          <div className="flex gap-2 mt-6">
            <Button variant="secondary" size="sm" onClick={openTransferDialog}>
              <ArrowUpDown className="mr-2 h-4 w-4" />
              Transférer
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Dialog de transfert */}
      <Dialog open={isTransferDialogOpen} onOpenChange={setIsTransferDialogOpen}>
        <DialogContent className="sm:max-w-[560px]">
          <DialogHeader>
            <DialogTitle>Transférer des fonds</DialogTitle>
            <DialogDescription>
              Sélectionnez un recepteur puis renseignez le montant et le motif.
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            {/* Recherche recepteur */}
            <div className="grid gap-2">
              <Label htmlFor="search-user">Rechercher (nom, email, téléphone)</Label>
              <Input
                id="search-user"
                placeholder="Ex: ahmed, example@mail.com, 55..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
              <div className="max-h-48 overflow-y-auto border rounded-md">
                {filteredUsers.length === 0 ? (
                  <p className="text-sm text-muted-foreground p-3">Aucun utilisateur</p>
                ) : (
                  filteredUsers.map((u) => (
                    <button
                      key={u.id}
                      type="button"
                      className={`w-full text-left px-3 py-2 hover:bg-accent ${selectedUser?.id === u.id ? 'bg-accent' : ''}`}
                      onClick={() => handleSelectUser(u)}
                    >
                      <div className="flex justify-between">
                        <span className="font-medium">{u.prenom} {u.nom}</span>
                        <span className="text-xs text-muted-foreground">{u.email}</span>
                      </div>
                      {u.telephone && (
                        <p className="text-xs text-muted-foreground">{u.telephone}</p>
                      )}
                    </button>
                  ))
                )}
              </div>
              {selectedUser && (
                <p className="text-sm">Recepteur: <strong>{selectedUser.prenom} {selectedUser.nom}</strong> {receiverCardId ? `(Carte #${receiverCardId})` : '(carte introuvable)'}</p>
              )}
            </div>

            {/* Montant, devise, motif */}
            <div className="grid md:grid-cols-2 gap-4">
              <div className="grid gap-2">
                <Label htmlFor="amount">Montant (TND)</Label>
                <Input id="amount" type="number" min="0" step="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="devise">Devise</Label>
                <Select value={devise} onValueChange={(v) => setDevise(v)}>
                  <SelectTrigger id="devise">
                    <SelectValue placeholder="Sélectionner" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="TND">TND</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
            <div className="grid gap-2">
              <Label htmlFor="motif">Motif</Label>
              <Input id="motif" value={motif} onChange={(e) => setMotif(e.target.value)} placeholder="Ex: Remboursement, cadeau" />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsTransferDialogOpen(false)} disabled={submitting}>Annuler</Button>
            <Button onClick={handleSubmitTransfer} disabled={submitting || !receiverCardId}>
              {submitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Confirmer le transfert
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Card>
        <CardHeader>
          <CardTitle>Historique des Transactions</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {transfers.length === 0 ? (
              <p className="text-center text-muted-foreground py-4">
                Aucune transaction
              </p>
            ) : (
              transfers.map((trans) => {
                const isEmetteur = trans.emetteurCarteId === card.id;
                const isRecepteur = trans.recepteurCarteId === card.id;
                
                return (
                  <div
                    key={trans.id}
                    className="flex items-center justify-between p-3 border border-border rounded-lg"
                  >
                    <div>
                      <p className="font-medium">{trans.motif}</p>
                      <p className="text-sm text-muted-foreground">
                        {new Date(trans.dateTransfert).toLocaleDateString()}
                      </p>
                    </div>
                    <div className="text-right">
                      <p 
                        className={`font-bold ${
                          isEmetteur 
                            ? 'text-red-500' 
                            : isRecepteur 
                            ? 'text-green-500' 
                            : 'text-muted-foreground'
                        }`}
                      >
                        {isEmetteur && '-'}
                        {isRecepteur && '+'}
                        {(trans.montantCent ?? 0).toFixed(2)} TND
                      </p>
                      <Badge variant="outline" className="text-xs">
                        {trans.statut}
                      </Badge>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default VirtualCard;
