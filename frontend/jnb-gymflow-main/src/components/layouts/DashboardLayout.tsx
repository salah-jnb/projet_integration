import { ReactNode, useEffect, useState } from "react";
import { useNavigate, Link, useLocation } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";
import { usersApi } from "@/lib/api";
import { 
  LayoutDashboard, 
  User, 
  CreditCard, 
  Calendar, 
  Users, 
  Gift, 
  Bell, 
  LogOut,
  Dumbbell,
  FileText,
  Settings,
  Home,
  Zap
} from "lucide-react";
import logo from "@/assets/jnb-logo.png";

interface MenuItem {
  label: string;
  icon: any;
  path: string;
}

interface DashboardLayoutProps {
  children: ReactNode;
  menuItems: MenuItem[];
}

const DashboardLayout = ({ children, menuItems }: DashboardLayoutProps) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [photoUrl, setPhotoUrl] = useState<string | undefined>(undefined);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  useEffect(() => {
    const fetchPhoto = async () => {
      try {
        if (user?.utilisateurId) {
          const res = await usersApi.getPhoto(user.utilisateurId);
          const objectUrl = URL.createObjectURL(res.data);
          setPhotoUrl(objectUrl);
        }
      } catch {
        setPhotoUrl(undefined);
      }
    };
    fetchPhoto();
  }, [user?.utilisateurId]);

  const getInitials = (prenom?: string, nom?: string) => {
    const p = (prenom || "").trim();
    const n = (nom || "").trim();
    if (p && n) return `${p[0]}${n[0]}`.toUpperCase();
    const full = `${p} ${n}`.trim();
    return full.substring(0, 2).toUpperCase();
  };

  return (
    <div className="min-h-screen bg-background">
      {/* Sidebar */}
      <aside className="fixed inset-y-0 left-0 w-64 bg-card border-r border-border flex flex-col">
        <div className="p-6 border-b border-border flex items-center justify-center">
          <Link to="/">
            <img src={logo} alt="JNB FITNESS" className="h-14 w-auto animate-fade-in" />
          </Link>
        </div>

        <div className="p-4 border-b border-border">
          <div className="flex items-center gap-3">
            <Avatar className="h-10 w-10">
              <AvatarImage src={photoUrl} alt={`${user?.prenom} ${user?.nom}`} />
              <AvatarFallback className="font-bold">
                {getInitials(user?.prenom, user?.nom)}
              </AvatarFallback>
            </Avatar>
            <div>
              <p className="font-semibold text-foreground">{user?.prenom} {user?.nom}</p>
              <p className="text-xs text-muted-foreground">{user?.typeUtilisateur}</p>
            </div>
          </div>
        </div>

        <nav className="flex-1 p-4 space-y-1">
          {menuItems.map((item) => {
            const isActive = location.pathname === item.path;
            return (
              <Link key={item.path} to={item.path}>
                <Button
                  variant={isActive ? "secondary" : "ghost"}
                  className={`w-full justify-start transition-all ${
                    isActive 
                      ? "bg-accent text-accent-foreground hover:bg-accent/90" 
                      : "text-foreground hover:bg-muted"
                  }`}
                >
                  <item.icon className="h-5 w-5 mr-3" />
                  {item.label}
                </Button>
              </Link>
            );
          })}
        </nav>

        <div className="p-4 border-t border-border">
          <Button
            variant="ghost"
            className="w-full justify-start text-destructive hover:bg-destructive/20"
            onClick={handleLogout}
          >
            <LogOut className="h-5 w-5 mr-3" />
            Déconnexion
          </Button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="ml-64 min-h-screen overflow-y-auto">
        <div className="max-w-7xl mx-auto">
          {children}
        </div>
      </main>
    </div>
  );
};

export default DashboardLayout;

// Predefined menu items for each user type
export const clientMenuItems: MenuItem[] = [
  { label: "Tableau de Bord", icon: LayoutDashboard, path: "/client/dashboard" },
  { label: "Mon Profil", icon: User, path: "/client/profile" },
  { label: "Mes Abonnements", icon: Dumbbell, path: "/client/subscriptions" },
  { label: "Ma Carte", icon: CreditCard, path: "/client/card" },
  { label: "Coaching", icon: Calendar, path: "/client/coaching" },
  { label: "Cours Collectifs", icon: Users, path: "/client/classes" },
  { label: "Articles", icon: FileText, path: "/client/articles" },
  { label: "Produits", icon: Home, path: "/client/products" },
  { label: "Parrainage", icon: Gift, path: "/client/referrals" },
  { label: "Notifications", icon: Bell, path: "/client/notifications" },
];

export const coachMenuItems: MenuItem[] = [
  { label: "Tableau de Bord", icon: LayoutDashboard, path: "/coach/dashboard" },
  { label: "Mon Profil", icon: User, path: "/coach/profile" },
  { label: "Disponibilités", icon: Calendar, path: "/coach/availability" },
  { label: "Réservations", icon: Users, path: "/coach/bookings" },
  { label: "Cours Collectifs", icon: Dumbbell, path: "/coach/classes" },
  { label: "Mes Articles", icon: FileText, path: "/coach/articles" },
  { label: "Produits", icon: Home, path: "/coach/products" },
  { label: "Notifications", icon: Bell, path: "/coach/notifications" },
];

export const adminMenuItems: MenuItem[] = [
  { label: "Tableau de Bord", icon: LayoutDashboard, path: "/admin/dashboard" },
  { label: "Utilisateurs", icon: Users, path: "/admin/users" },
  { label: "Abonnements", icon: Dumbbell, path: "/admin/subscriptions" },
  { label: "Cours Collectifs", icon: Calendar, path: "/admin/classes" },
  { label: "Articles", icon: FileText, path: "/admin/articles" },
  { label: "Produits", icon: Home, path: "/admin/products" },
  { label: "Paiements", icon: CreditCard, path: "/admin/payments" },
  { label: "Cartes", icon: CreditCard, path: "/admin/cards" },
  { label: "Paramètres", icon: Settings, path: "/admin/settings" },
];
