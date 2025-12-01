import { useState, useEffect } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Bell, CheckCheck, Loader2 } from "lucide-react";
import { notificationsApi } from "@/lib/api";
import { toast } from "sonner";
import { useAuth } from "@/contexts/AuthContext";

interface Notification {
  id: number;
  utilisateurId: number;
  titre: string;
  message: string;
  type: string;
  dateEnvoi: string;
  estLu: boolean;
}

const Notifications = () => {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);
  const [marking, setMarking] = useState(false);

  useEffect(() => {
    if (user?.utilisateurId) {
      fetchNotifications();
    }
  }, [user?.utilisateurId]);

  const fetchNotifications = async () => {
    if (!user?.utilisateurId) return;

    try {
      setLoading(true);
      const response = await notificationsApi.getUnreadByUser(user.utilisateurId.toString());
      console.log("Notifications (API):", response.data);
      setNotifications(response.data);
    } catch (error) {
      console.error("Erreur lors du chargement des notifications:", error);
      toast.error("Impossible de charger les notifications");
    } finally {
      setLoading(false);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      setMarking(true);
      const unread = notifications.filter((n) => !n.estLu);

      // Exécuter les mises à jour en parallèle
      await Promise.all(unread.map((n) => notificationsApi.markAsRead(n.id.toString())));

      toast.success("Toutes les notifications ont été marquées comme lues");
      // Mise à jour locale immédiate
      setNotifications((prev) => prev.map((n) => ({ ...n, estLu: true })));
    } catch (error) {
      console.error("Erreur lors du marquage des notifications:", error);
      toast.error("Impossible de marquer les notifications comme lues");
    } finally {
      setMarking(false);
    }
  };

  const handleMarkAsRead = async (id: number) => {
    try {
      await notificationsApi.markAsRead(id.toString());
      setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, estLu: true } : n)));
    } catch (error) {
      console.error("Erreur lors du marquage de la notification:", error);
      toast.error("Impossible de marquer la notification comme lue");
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("fr-FR", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  if (loading) {
    return (
      <div className="p-6 flex justify-center items-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  const unreadCount = notifications.filter((n) => !n.estLu).length;

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold mb-2">Notifications</h1>
          <p className="text-muted-foreground">{unreadCount} non lue(s)</p>
        </div>
        {unreadCount > 0 && (
          <Button variant="outline" size="sm" onClick={handleMarkAllAsRead} disabled={marking}>
            <CheckCheck className="mr-2 h-4 w-4" />
            {marking ? "Marquage..." : "Tout marquer comme lu"}
          </Button>
        )}
      </div>

      <div className="space-y-3">
        {notifications.length === 0 ? (
          <p className="text-center text-muted-foreground py-8">Aucune notification</p>
        ) : (
          notifications
            .filter((n) => !n.estLu)
            .map((notif) => (
            <Card
              key={notif.id}
              className={`${!notif.estLu ? "border-accent" : ""} cursor-pointer`}
              onClick={() => !notif.estLu && handleMarkAsRead(notif.id)}
            >
              <CardContent className="pt-6">
                <div className="flex items-start justify-between">
                  <div className="flex gap-3 flex-1">
                    <div
                      className={`mt-1 ${
                        !notif.estLu ? "text-accent" : "text-muted-foreground"
                      }`}
                    >
                      <Bell className="h-5 w-5" />
                    </div>
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <h3 className="font-semibold">{notif.titre}</h3>
                        {!notif.estLu && <Badge variant="default">Nouveau</Badge>}
                      </div>
                      <p className="text-sm text-muted-foreground mb-2">{notif.message}</p>
                      <div className="flex items-center gap-2">
                        <p className="text-xs text-muted-foreground">{formatDate(notif.dateEnvoi)}</p>
                        <Badge variant="outline" className="text-xs">
                          {notif.type}
                        </Badge>
                      </div>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))
        )}
      </div>
    </div>
  );
};

export default Notifications;
