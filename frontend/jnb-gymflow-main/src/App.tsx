import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "@/contexts/AuthContext";
import ProtectedRoute from "@/components/ProtectedRoute";
import DashboardLayout, { clientMenuItems, coachMenuItems, adminMenuItems } from "@/components/layouts/DashboardLayout";

// Pages
import Landing from "./pages/Landing";
import Login from "./pages/Login";
import Register from "./pages/Register";
import ClientDashboard from "./pages/client/ClientDashboard";
import CoachDashboard from "./pages/coach/CoachDashboard";
import AdminDashboard from "./pages/admin/AdminDashboard";
import NotFound from "./pages/NotFound";

// Client Pages
import ClientProfile from "./pages/client/Profile";
import ClientArticles from "./pages/client/Articles";
import ClientProducts from "./pages/client/Products";
import Subscriptions from "./pages/client/Subscriptions";
import VirtualCard from "./pages/client/VirtualCard";
import Coaching from "./pages/client/Coaching";
import CollectiveClasses from "./pages/client/CollectiveClasses";
import Referrals from "./pages/client/Referrals";
import ClientNotifications from "./pages/client/Notifications";

// Coach Pages
import CoachProfile from "./pages/coach/Profile";
import Availability from "./pages/coach/Availability";
import CoachReservations from "./pages/coach/Reservations";
import CoachCollectiveClasses from "./pages/coach/CollectiveClasses";
import CoachArticles from "./pages/coach/Articles";
import CoachProducts from "./pages/coach/Products";
import CoachNotifications from "./pages/coach/Notifications";

// Admin Pages
import Users from "./pages/admin/Users";
import AdminSubscriptions from "./pages/admin/Subscriptions";
import AdminCollectiveClasses from "./pages/admin/CollectiveClasses";
import AdminArticles from "./pages/admin/Articles";
import AdminProducts from "./pages/admin/Products";
import Payments from "./pages/admin/Payments";
import VirtualCards from "./pages/admin/VirtualCards";
import Settings from "./pages/admin/Settings";

const queryClient = new QueryClient();

const App = () => (
  <QueryClientProvider client={queryClient}>
    <AuthProvider>
      <TooltipProvider>
        <Toaster />
        <Sonner />
        <BrowserRouter>
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<Landing />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />

            {/* Client Routes */}
            <Route path="/client/*" element={
              <ProtectedRoute allowedRoles={["CLIENT"]}>
                <DashboardLayout menuItems={clientMenuItems}>
                  <Routes>
                    <Route path="dashboard" element={<ClientDashboard />} />
                    <Route path="profile" element={<ClientProfile />} />
                    <Route path="subscriptions" element={<Subscriptions />} />
                    <Route path="card" element={<VirtualCard />} />
                    <Route path="coaching" element={<Coaching />} />
                    <Route path="classes" element={<CollectiveClasses />} />
                    <Route path="articles" element={<ClientArticles />} />
                    <Route path="products" element={<ClientProducts />} />
                    <Route path="referrals" element={<Referrals />} />
                    <Route path="notifications" element={<ClientNotifications />} />
                  </Routes>
                </DashboardLayout>
              </ProtectedRoute>
            } />

            {/* Coach Routes */}
            <Route path="/coach/*" element={
              <ProtectedRoute allowedRoles={["COACH"]}>
                <DashboardLayout menuItems={coachMenuItems}>
                  <Routes>
                    <Route path="dashboard" element={<CoachDashboard />} />
                    <Route path="profile" element={<CoachProfile />} />
                    <Route path="availability" element={<Availability />} />
                    <Route path="bookings" element={<CoachReservations />} />
                    <Route path="classes" element={<CoachCollectiveClasses />} />
                    <Route path="articles" element={<CoachArticles />} />
                    <Route path="products" element={<CoachProducts />} />
                    <Route path="notifications" element={<CoachNotifications />} />
                  </Routes>
                </DashboardLayout>
              </ProtectedRoute>
            } />

            {/* Admin Routes */}
            <Route path="/admin/*" element={
              <ProtectedRoute allowedRoles={["ADMINISTRATEUR"]}>
                <DashboardLayout menuItems={adminMenuItems}>
                  <Routes>
                    <Route path="dashboard" element={<AdminDashboard />} />
                    <Route path="users" element={<Users />} />
                    <Route path="subscriptions" element={<AdminSubscriptions />} />
                    <Route path="classes" element={<AdminCollectiveClasses />} />
                    <Route path="articles" element={<AdminArticles />} />
                    <Route path="products" element={<AdminProducts />} />
                    <Route path="payments" element={<Payments />} />
                    <Route path="cards" element={<VirtualCards />} />
                    <Route path="settings" element={<Settings />} />
                  </Routes>
                </DashboardLayout>
              </ProtectedRoute>
            } />

            {/* 404 */}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </BrowserRouter>
      </TooltipProvider>
    </AuthProvider>
  </QueryClientProvider>
);

export default App;
