import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Search, User, Calendar, Download, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { api, paiementsApi, clientsApi } from "@/lib/api";
import { toast } from "sonner";

interface Payment {
  abonnementId: number;
  clientId: number;
  abonnementNom: string;
  datePaiement: string;
  methodePaiement: string;
  montant: number;
  statut: string;
  clientNom?: string;
  clientPrenom?: string;
}

const Payments = () => {
  const [loading, setLoading] = useState(true);
  const [payments, setPayments] = useState<Payment[]>([]);
  const [searchTerm, setSearchTerm] = useState("");

  useEffect(() => {
    fetchPayments();
  }, []);

  const fetchPayments = async () => {
    try {
      setLoading(true);
      const response = await paiementsApi.getAll();
      const paymentsData = response.data;

      // Récupérer les informations des clients pour chaque paiement
      const paymentsWithClients = await Promise.all(
        paymentsData.map(async (payment: Payment) => {
          try {
            const clientResponse = await clientsApi.getByUserId(payment.clientId.toString());
            return {
              ...payment,
              clientNom: clientResponse.data.nom,
              clientPrenom: clientResponse.data.prenom,
            };
          } catch (error) {
            console.error(`Erreur lors du chargement du client ${payment.clientId}:`, error);
            return {
              ...payment,
              clientNom: "Inconnu",
              clientPrenom: "",
            };
          }
        })
      );

      setPayments(paymentsWithClients);
      console.log(paymentsWithClients);
    } catch (error) {
      console.error("Erreur lors du chargement des paiements:", error);
      toast.error("Impossible de charger les paiements");
    } finally {
      setLoading(false);
    }
  };

  const filteredPayments = payments.filter(
    (payment) =>
      payment.clientNom?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      payment.clientPrenom?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      payment.abonnementNom?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      payment.statut?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleExport = () => {
    try {
      const csvContent = [
        ["ID Abonnement", "Client", "Nom Abonnement", "Date Paiement", "Montant", "Statut", "Méthode"],
        ...filteredPayments.map((payment) => [
          payment.abonnementId,
          `${payment.clientPrenom} ${payment.clientNom}`,
          payment.abonnementNom,
          new Date(payment.datePaiement).toLocaleDateString("fr-FR"),
          `${payment.montant} TND`,
          payment.statut,
          payment.methodePaiement,
        ]),
      ]
        .map((row) => row.join(","))
        .join("\n");

      const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
      const link = document.createElement("a");
      const url = URL.createObjectURL(blob);
      link.setAttribute("href", url);
      link.setAttribute("download", `paiements_${new Date().toISOString().split("T")[0]}.csv`);
      link.style.visibility = "hidden";
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);

      toast.success("Paiements exportés avec succès");
    } catch (error) {
      console.error("Erreur lors de l'export:", error);
      toast.error("Impossible d'exporter les paiements");
    }
  };

  const getStatusVariant = (statut: string) => {
    switch (statut.toUpperCase()) {
      case "VALIDE":
        return "default";
      case "REMBOURSE":
        return "secondary";
      case "EN_ATTENTE":
        return "outline";
      default:
        return "outline";
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
          <h1 className="text-3xl font-bold mb-2">Gestion Paiements</h1>
          <p className="text-muted-foreground">Historique et suivi des paiements</p>
        </div>
        <Button variant="outline" onClick={handleExport} disabled={filteredPayments.length === 0}>
          <Download className="mr-2 h-4 w-4" />
          Exporter
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center gap-2">
            <Search className="h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Rechercher par client, abonnement ou statut..."
              className="max-w-sm"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {filteredPayments.length === 0 ? (
              <p className="text-center text-muted-foreground py-8">
                {payments.length === 0
                  ? "Aucun paiement trouvé"
                  : "Aucun résultat correspondant à votre recherche"}
              </p>
            ) : (
              filteredPayments.map((payment) => (
                <div
                  key={payment.abonnementId}
                  className="flex items-center justify-between p-4 border border-border rounded-lg hover:shadow-md transition-shadow"
                >
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <User className="h-4 w-4 text-muted-foreground" />
                      <p className="font-semibold">
                        {payment.clientPrenom} {payment.clientNom}
                      </p>
                    </div>
                    <p className="text-sm text-muted-foreground mb-2">
                      {payment.abonnementNom}
                    </p>
                    <div className="flex items-center gap-4 text-xs text-muted-foreground flex-wrap">
                      <div className="flex items-center gap-1">
                        <Calendar className="h-3 w-3" />
                        <span>
                          {new Date(payment.datePaiement).toLocaleDateString("fr-FR")}
                        </span>
                      </div>
                      <Badge variant="outline">{payment.methodePaiement}</Badge>
                    </div>
                  </div>
                  <div className="flex items-center gap-4">
                    <div className="text-right">
                      <p className="text-2xl font-bold">{payment.montant} TND</p>
                      <Badge variant={getStatusVariant(payment.statut)}>
                        {payment.statut}
                      </Badge>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default Payments;
