import { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Zap } from "lucide-react";
import { z } from "zod";

const loginSchema = z.object({
  email: z.string().email("Email invalide"),
  motDePasse: z.string().min(1, "Mot de passe requis"),
});

const Login = () => {
  const navigate = useNavigate();
  const { login, user } = useAuth();
  const [email, setEmail] = useState("");
  const [motDePasse, setMotDePasse] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState<{ email?: string; motDePasse?: string }>({});
  const [touched, setTouched] = useState<{ email: boolean; motDePasse: boolean }>({ email: false, motDePasse: false });

  const normalizeEmail = (v: string) => v.trim().toLowerCase();
  const normalizeText = (v: string) => v.trim();
  const validateEmail = (v: string) => {
    const res = z.string().email("Email invalide").safeParse(v);
    return res.success ? undefined : res.error.errors[0]?.message;
  };
  const validateMotDePasse = (v: string) => {
    const res = z.string().min(1, "Mot de passe requis").safeParse(v.trim());
    return res.success ? undefined : res.error.errors[0]?.message;
  };
  const isFormValid = loginSchema.safeParse({ email: normalizeEmail(email), motDePasse: normalizeText(motDePasse) }).success;

  // Redirect if already logged in
  useEffect(() => {
    if (user) {
      const dashboardRoute = 
        user.typeUtilisateur === "CLIENT" ? "/client/dashboard" :
        user.typeUtilisateur === "COACH" ? "/coach/dashboard" :
        "/admin/dashboard";
      navigate(dashboardRoute, { replace: true });
    }
  }, [user, navigate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setTouched({ email: true, motDePasse: true });
    setErrors({});

    // Validation
    const normalized = { email: normalizeEmail(email), motDePasse: normalizeText(motDePasse) };
    const result = loginSchema.safeParse(normalized);
    if (!result.success) {
      const fieldErrors: any = {};
      result.error.errors.forEach((err) => {
        if (err.path[0]) fieldErrors[err.path[0]] = err.message;
      });
      setErrors(fieldErrors);
      return;
    }

    setIsLoading(true);
    try {
      await login(normalized.email, normalized.motDePasse);
      // Navigation will be handled by the redirect effect above
    } catch (error) {
      // Error handled by AuthContext
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary via-primary-glow to-primary p-4">
      <Card className="w-full max-w-md shadow-2xl animate-scale-in">
        <CardHeader className="text-center">
          <div className="flex justify-center mb-4">
            <div className="p-3 bg-accent/10 rounded-full">
              <Zap className="h-10 w-10 text-accent" />
            </div>
          </div>
          <CardTitle className="text-3xl font-bold">Connexion</CardTitle>
          <CardDescription>
            Accédez à votre espace JNB Fitness
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                placeholder="votre@email.com"
                value={email}
                onChange={(e) => {
                  const next = normalizeEmail(e.target.value);
                  setEmail(next);
                  setErrors((prev) => ({ ...prev, email: validateEmail(next) }));
                }}
                onBlur={() => {
                  setTouched((prev) => ({ ...prev, email: true }));
                  setErrors((prev) => ({ ...prev, email: validateEmail(normalizeEmail(email)) }));
                }}
                className={touched.email && errors.email ? "border-destructive" : ""}
              />
              {touched.email && errors.email && (
                <p className="text-sm text-destructive">{errors.email}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">Mot de passe</Label>
              <Input
                id="password"
                type="password"
                placeholder="••••••••"
                value={motDePasse}
                onChange={(e) => {
                  const next = normalizeText(e.target.value);
                  setMotDePasse(next);
                  setErrors((prev) => ({ ...prev, motDePasse: validateMotDePasse(next) }));
                }}
                onBlur={() => {
                  setTouched((prev) => ({ ...prev, motDePasse: true }));
                  setErrors((prev) => ({ ...prev, motDePasse: validateMotDePasse(motDePasse) }));
                }}
                className={touched.motDePasse && errors.motDePasse ? "border-destructive" : ""}
              />
              {touched.motDePasse && errors.motDePasse && (
                <p className="text-sm text-destructive">{errors.motDePasse}</p>
              )}
            </div>

            <Button 
              type="submit" 
              className="w-full" 
              size="lg"
              disabled={isLoading || !isFormValid}
            >
              {isLoading ? "Connexion..." : "Se connecter"}
            </Button>
          </form>

          <div className="mt-6 text-center space-y-2">
            <p className="text-sm text-muted-foreground">
              Pas encore de compte ?{" "}
              <Link to="/register" className="text-accent font-semibold hover:underline">
                S'inscrire
              </Link>
            </p>
            <Link to="/" className="text-sm text-muted-foreground hover:text-accent transition-colors block">
              ← Retour à l'accueil
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default Login;
