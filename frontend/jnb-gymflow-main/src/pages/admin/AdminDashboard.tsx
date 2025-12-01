import { useState, useEffect } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Users, DollarSign, Activity, TrendingUp, CreditCard, Calendar, FileText } from "lucide-react";
import { usersApi, clientsApi, abonnementsApi, paiementsApi, coursCollectifsApi, articlesApi } from "@/lib/api";
import { LineChart, Line, AreaChart, Area, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts";
import { format, subDays, subMonths, startOfMonth, endOfMonth, parseISO } from "date-fns";

interface Stats {
  totalUsers: number;
  totalClients: number;
  totalAbonnements: number;
  totalAbonnementsActifs: number;
  totalRevenus: number;
  revenusMois: number;
  totalReservations: number;
  reservationsSemaine: number;
  totalArticles: number;
}

const AdminDashboard = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState<Stats>({
    totalUsers: 0,
    totalClients: 0,
    totalAbonnements: 0,
    totalAbonnementsActifs: 0,
    totalRevenus: 0,
    revenusMois: 0,
    totalReservations: 0,
    reservationsSemaine: 0,
    totalArticles: 0,
  });
  const [loading, setLoading] = useState(true);
  const [usersChartData, setUsersChartData] = useState<any[]>([]);
  const [revenueChartData, setRevenueChartData] = useState<any[]>([]);
  const [subscriptionsChartData, setSubscriptionsChartData] = useState<any[]>([]);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        // Récupérer toutes les données nécessaires
        const [usersResponse, clientsResponse, abonnementsResponse, paiementsResponse, coursResponse, articlesResponse] = await Promise.all([
          usersApi.getAll().catch(() => ({ data: [] })),
          clientsApi.getAll().catch(() => ({ data: [] })),
          abonnementsApi.getAll().catch(() => ({ data: [] })),
          paiementsApi.getAll().catch(() => ({ data: [] })),
          coursCollectifsApi.getAll().catch(() => ({ data: [] })),
          articlesApi.getAll().catch(() => ({ data: [] })),
        ]);

        const users = usersResponse.data || [];
        const clients = clientsResponse.data || [];
        const abonnements = abonnementsResponse.data || [];
        const paiements = paiementsResponse.data || [];
        const articles = articlesResponse.data || [];

        // Calculer les statistiques
        const totalUsers = users.length;
        const totalClients = clients.length;
        const totalAbonnements = abonnements.length;
        const totalAbonnementsActifs = abonnements.filter((a: any) => a.statut === 'ACTIF').length;
        const totalRevenus = paiements.reduce((sum: number, p: any) => sum + (p.montant || 0), 0);
        
        // Revenus du mois en cours
        const maintenant = new Date();
        const debutMois = startOfMonth(maintenant);
        const revenusMois = paiements
          .filter((p: any) => {
            try {
              const datePaiement = parseISO(p.datePaiement || p.dateCreation);
              return datePaiement >= debutMois;
            } catch {
              return false;
            }
          })
          .reduce((sum: number, p: any) => sum + (p.montant || 0), 0);

        // Données pour graphiques des utilisateurs (30 derniers jours)
        const userChartData = [];
        for (let i = 29; i >= 0; i--) {
          const date = subDays(new Date(), i);
          const dateStr = format(date, 'dd/MM');
          const usersCeJour = users.filter((u: any) => {
            try {
              const dateInscription = parseISO(u.dateInscription || u.dateCreation);
              return format(dateInscription, 'yyyy-MM-dd') === format(date, 'yyyy-MM-dd');
            } catch {
              return false;
            }
          }).length;
          userChartData.push({ date: dateStr, utilisateurs: usersCeJour });
        }
        setUsersChartData(userChartData);

        // Données pour graphique des revenus (12 derniers mois)
        const revenueChartData = [];
        for (let i = 11; i >= 0; i--) {
          const date = subMonths(new Date(), i);
          const moisStr = format(date, 'MMM yyyy');
          const debutMois = startOfMonth(date);
          const finMois = endOfMonth(date);
          const revenusCeMois = paiements
            .filter((p: any) => {
              try {
                const datePaiement = parseISO(p.datePaiement || p.dateCreation);
                return datePaiement >= debutMois && datePaiement <= finMois;
              } catch {
                return false;
              }
            })
            .reduce((sum: number, p: any) => sum + (p.montant || 0), 0);
          revenueChartData.push({ mois: moisStr, revenus: revenusCeMois });
        }
        setRevenueChartData(revenueChartData);

        // Données pour graphique des abonnements par statut
        const subscriptionsByStatus = abonnements.reduce((acc: any, a: any) => {
          const statut = a.statut || 'AUTRE';
          acc[statut] = (acc[statut] || 0) + 1;
          return acc;
        }, {});
        const subscriptionsChartData = Object.entries(subscriptionsByStatus).map(([statut, count]) => ({
          statut: statut.charAt(0) + statut.slice(1).toLowerCase(),
          nombre: count as number,
        }));
        setSubscriptionsChartData(subscriptionsChartData);

        setStats({
          totalUsers,
          totalClients,
          totalAbonnements,
          totalAbonnementsActifs,
          totalRevenus,
          revenusMois,
          totalReservations: 0, // À calculer si endpoint disponible
          reservationsSemaine: 0,
          totalArticles: articles.length,
        });
      } catch (error: any) {
        console.error("Erreur lors du chargement des statistiques:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, []);

  if (loading) {
    return (
      <div className="p-6 flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-muted-foreground">Chargement des statistiquessss..</p>
        </div>
      </div>
    );
  }

  const growthRate = stats.totalAbonnements > 0 
    ? ((stats.totalAbonnementsActifs / stats.totalAbonnements) * 100).toFixed(1)
    : 0;

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <div>
        <h1 className="text-3xl font-bold mb-2">
          Administration - {user?.prenom} {user?.nom}
        </h1>
        <p className="text-muted-foreground">
          Vue d'ensemble de la salle de sport
        </p>
      </div>

      {/* Cards de statistiques */}
      <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="hover:shadow-lg transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Total Utilisateurs</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.totalUsers}</div>
            <p className="text-xs text-muted-foreground">{stats.totalClients} clients</p>
          </CardContent>
        </Card>

        <Card className="hover:shadow-lg transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Abonnements</CardTitle>
            <CreditCard className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.totalAbonnementsActifs}</div>
            <p className="text-xs text-muted-foreground">
              {stats.totalAbonnements} total • {growthRate}% actifs
            </p>
          </CardContent>
        </Card>

        <Card className="hover:shadow-lg transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Revenus</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.revenusMois.toFixed(0)} TND</div>
            <p className="text-xs text-muted-foreground">Ce mois • {stats.totalRevenus.toFixed(0)} TND total</p>
          </CardContent>
        </Card>

        <Card className="hover:shadow-lg transition-shadow">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Articles</CardTitle>
            <FileText className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.totalArticles}</div>
            <p className="text-xs text-muted-foreground">Articles publiés</p>
          </CardContent>
        </Card>
      </div>

      {/* Graphiques */}
      <div className="grid md:grid-cols-2 gap-4">
        {/* Graphique des nouveaux utilisateurs */}
        <Card>
          <CardHeader>
            <CardTitle>Nouveaux Utilisateurs (30 derniers jours)</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <AreaChart data={usersChartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip />
                <Area type="monotone" dataKey="utilisateurs" stroke="#8884d8" fill="#8884d8" fillOpacity={0.6} />
              </AreaChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Graphique des revenus */}
        <Card>
          <CardHeader>
            <CardTitle>Revenus (12 derniers mois)</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={revenueChartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="mois" />
                <YAxis />
                <Tooltip formatter={(value: any) => `${value.toFixed(0)} TND`} />
                <Legend />
                <Line type="monotone" dataKey="revenus" stroke="#82ca9d" strokeWidth={2} />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Graphique des abonnements par statut */}
        <Card className="md:col-span-2">
          <CardHeader>
            <CardTitle>Répartition des Abonnements par Statut</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={subscriptionsChartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="statut" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="nombre" fill="#8884d8" />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default AdminDashboard;
