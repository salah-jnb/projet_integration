import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Bell, CheckCheck } from "lucide-react";
import { useEffect, useState } from "react";
import { notificationsApi } from "@/lib/api";

interface Notification {
  id: number;
  destinataireId: number;
  titre: string;
  message: string;
  type: string;
  dateEnvoi: string;
  lue: boolean;
  emailEnvoye: boolean;
}

const CoachNotifications = () => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);
  
  const utilisateurId = localStorage.getItem("utilisateurId") || "4";

  useEffect(() => {
    fetchNotifications();
  }, []);

  const fetchNotifications = async () => {
    try {
      setLoading(true);
      const response = await notificationsApi.getUnreadByUser(utilisateurId);
      setNotifications(response.data);
    } catch (error) {
      console.error("Erreur lors du chargement des notifications:", error);
    } finally {
      setLoading(false);
    }
  };

  const markAllAsRead = async () => {
    try {
      const unreadNotifications = notifications.filter(n => !n.lue);
      
      // ✅ Convertir l'ID en string
      await Promise.all(
        unreadNotifications.map(notif => 
          notificationsApi.markAsRead(notif.id.toString())
        )
      );
      
      await fetchNotifications();
    } catch (error) {
      console.error("Erreur lors du marquage des notifications:", error);
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("fr-FR", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit"
    });
  };

  const getTypeColor = (type: string) => {
    const colors: Record<string, string> = {
      COACHING: "bg-blue-500",
      ARTICLE: "bg-green-500",
      NOTATION: "bg-yellow-500",
      RAPPEL: "bg-purple-500",
      PARRAINAGE: "bg-pink-500",
    };
    return colors[type] || "bg-gray-500";
  };

  if (loading) {
    return (
      <div className="p-6 flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold mb-2">Notifications</h1>
          <p className="text-muted-foreground">
            {notifications.filter((n) => !n.lue).length} non lues
          </p>
        </div>
        <Button 
          variant="outline" 
          size="sm"
          onClick={markAllAsRead}
          disabled={notifications.filter(n => !n.lue).length === 0}
        >
          <CheckCheck className="mr-2 h-4 w-4" />
          Tout marquer comme lu
        </Button>
      </div>

      {notifications.length === 0 ? (
        <Card>
          <CardContent className="pt-6 text-center text-muted-foreground">
            <Bell className="h-12 w-12 mx-auto mb-4 opacity-50" />
            <p>Aucune notification pour le moment</p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-3">
          {notifications.filter((n) => !n.lue).map((notif) => (
            <Card
              key={notif.id}
              className={`transition-all hover:shadow-md ${
                !notif.lue ? "border-accent border-l-4" : ""
              }`}
            >
              <CardContent className="pt-6">
                <div className="flex items-start justify-between">
                  <div className="flex gap-3 flex-1">
                    <div
                      className={`mt-1 ${
                        !notif.lue ? "text-accent" : "text-muted-foreground"
                      }`}
                    >
                      <Bell className="h-5 w-5" />
                    </div>
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1 flex-wrap">
                        <h3 className="font-semibold">{notif.titre}</h3>
                        {!notif.lue && (
                          <Badge variant="default">Nouveau</Badge>
                        )}
                        <Badge 
                          variant="outline" 
                          className={`${getTypeColor(notif.type)} text-white border-0`}
                        >
                          {notif.type}
                        </Badge>
                      </div>
                      <p className="text-sm text-muted-foreground mb-2">
                        {notif.message}
                      </p>
                      <div className="flex items-center gap-4 text-xs text-muted-foreground">
                        <span>{formatDate(notif.dateEnvoi)}</span>
                        {notif.emailEnvoye && (
                          <Badge variant="secondary" className="text-xs">
                            Email envoyé
                          </Badge>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};

export default CoachNotifications;
