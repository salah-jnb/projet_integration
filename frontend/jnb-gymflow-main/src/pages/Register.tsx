import { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Zap } from "lucide-react";
import { clientsApi, parrainagesApi } from "@/lib/api";
import { z } from "zod";

const registerSchema = z.object({
  email: z.string().email("Email invalide"),
  motDePasse: z.string().min(6, "Le mot de passe doit contenir au moins 6 caractères"),
  nom: z.string().min(1, "Nom requis"),
  prenom: z.string().min(1, "Prénom requis"),
  telephone: z.string().min(8, "Numéro de téléphone invalide"),
  adresse: z.string().min(5, "Adresse requise"),
  codeParrainage: z.string().optional(),
});

const Register = () => {
  const navigate = useNavigate();
  const { register, user } = useAuth();
  const [formData, setFormData] = useState({
    email: "",
    motDePasse: "",
    nom: "",
    prenom: "",
    telephone: "",
    adresse: "",
    codeParrainage: "",
  });
  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [touched, setTouched] = useState<Record<string, boolean>>({
    email: false,
    motDePasse: false,
    nom: false,
    prenom: false,
    telephone: false,
    adresse: false,
    codeParrainage: false,
  });

  const normalizeEmail = (v: string) => v.trim().toLowerCase();
  const normalizeText = (v: string) => v.trim();
  const normalizeTelephone = (v: string) => v.trim();
  const validateField = (name: string, value: string) => {
    const data = {
      ...formData,
      [name]: name === "email" ? normalizeEmail(value) : name === "telephone" ? normalizeTelephone(value) : normalizeText(value),
    };
    const res = registerSchema.safeParse(data);
    if (!res.success) {
      const err = res.error.errors.find((e) => String(e.path[0]) === name);
      return err ? err.message : undefined;
    }
    return undefined;
  };
  const isFormValid = registerSchema.safeParse({
    email: normalizeEmail(formData.email),
    motDePasse: normalizeText(formData.motDePasse),
    nom: normalizeText(formData.nom),
    prenom: normalizeText(formData.prenom),
    telephone: normalizeTelephone(formData.telephone),
    adresse: normalizeText(formData.adresse),
    codeParrainage: normalizeText(formData.codeParrainage),
  }).success;

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

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    const next = name === "email" ? normalizeEmail(value) : name === "telephone" ? normalizeTelephone(value) : normalizeText(value);
    setFormData({ ...formData, [name]: next });
    setErrors((prev) => ({ ...prev, [name]: validateField(name, next) || "" }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setTouched({
      email: true,
      motDePasse: true,
      nom: true,
      prenom: true,
      telephone: true,
      adresse: true,
      codeParrainage: Boolean(formData.codeParrainage),
    });
    setErrors({});

    // Validation
    const normalized = {
      email: normalizeEmail(formData.email),
      motDePasse: normalizeText(formData.motDePasse),
      nom: normalizeText(formData.nom),
      prenom: normalizeText(formData.prenom),
      telephone: normalizeTelephone(formData.telephone),
      adresse: normalizeText(formData.adresse),
      codeParrainage: normalizeText(formData.codeParrainage),
    };
    const result = registerSchema.safeParse(normalized);
    if (!result.success) {
      const fieldErrors: Record<string, string> = {};
      result.error.errors.forEach((err) => {
        if (err.path[0]) fieldErrors[err.path[0] as string] = err.message;
      });
      setErrors(fieldErrors);
      return;
    }

    setIsLoading(true);
    try {
      // 1) Vérifier le code de parrainage s'il est saisi
      const code = (normalized.codeParrainage || "").trim().toUpperCase();
      let parrainUtilisateurId: string | null = null;

      if (code) {
        try {
          const { data } = await clientsApi.getByParrainageCode(code);
          // L'API retourne un utilisateur client; on récupère son utilisateurId
          parrainUtilisateurId = String(data.utilisateurId);
        } catch (err: any) {
          // Code invalide: bloquer l'inscription et afficher erreur
          setErrors((prev) => ({ ...prev, codeParrainage: "Code de parrainage invalide" }));
          return; // stop submit
        }
      }

      // 2) Procéder à l'inscription
      await register(normalized);

      // 3) Si code valide, créer le parrainage (parrain -> nouveau client)
      if (parrainUtilisateurId) {
        const storedUser = localStorage.getItem("jnb_user");
        const newUser = storedUser ? JSON.parse(storedUser) : null;
        const filleulId = newUser?.utilisateurId ? String(newUser.utilisateurId) : null;

        if (filleulId) {
          try {
            await parrainagesApi.create({ parrainId: parrainUtilisateurId, filleulId });
          } catch (err) {
            // Échec de création du parrainage – ne bloque pas la navigation
          }
        }
      }
      // Navigation sera gérée par le useEffect
    } catch (error) {
      // Erreurs gérées par AuthContext
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary via-primary-glow to-primary p-4">
      <Card className="w-full max-w-2xl shadow-2xl animate-scale-in my-8">
        <CardHeader className="text-center">
          <div className="flex justify-center mb-4">
            <div className="p-3 bg-accent/10 rounded-full">
              <Zap className="h-10 w-10 text-accent" />
            </div>
          </div>
          <CardTitle className="text-3xl font-bold">Inscription</CardTitle>
          <CardDescription>
            Rejoignez JNB Fitness et commencez votre transformation
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="prenom">Prénom *</Label>
                <Input
                  id="prenom"
                  name="prenom"
                  placeholder="Votre prénom"
                  value={formData.prenom}
                  onChange={handleChange}
                  onBlur={() => setTouched((p) => ({ ...p, prenom: true }))}
                  className={touched.prenom && errors.prenom ? "border-destructive" : ""}
                />
                {touched.prenom && errors.prenom && (
                  <p className="text-sm text-destructive">{errors.prenom}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="nom">Nom *</Label>
                <Input
                  id="nom"
                  name="nom"
                  placeholder="Votre nom"
                  value={formData.nom}
                  onChange={handleChange}
                  onBlur={() => setTouched((p) => ({ ...p, nom: true }))}
                  className={touched.nom && errors.nom ? "border-destructive" : ""}
                />
                {touched.nom && errors.nom && (
                  <p className="text-sm text-destructive">{errors.nom}</p>
                )}
              </div>
            </div>

              <div className="space-y-2">
                <Label htmlFor="email">Email *</Label>
                <Input
                  id="email"
                  name="email"
                  type="email"
                  placeholder="votre@email.com"
                  value={formData.email}
                  onChange={handleChange}
                  onBlur={() => setTouched((p) => ({ ...p, email: true }))}
                  className={touched.email && errors.email ? "border-destructive" : ""}
                />
              {touched.email && errors.email && (
                <p className="text-sm text-destructive">{errors.email}</p>
              )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="motDePasse">Mot de passe *</Label>
                <Input
                  id="motDePasse"
                  name="motDePasse"
                  type="password"
                  placeholder="Min. 6 caractères"
                  value={formData.motDePasse}
                  onChange={handleChange}
                  onBlur={() => setTouched((p) => ({ ...p, motDePasse: true }))}
                  className={touched.motDePasse && errors.motDePasse ? "border-destructive" : ""}
                />
              {touched.motDePasse && errors.motDePasse && (
                <p className="text-sm text-destructive">{errors.motDePasse}</p>
              )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="telephone">Téléphone *</Label>
                <Input
                  id="telephone"
                  name="telephone"
                  type="tel"
                  placeholder="Votre numéro"
                  value={formData.telephone}
                  onChange={handleChange}
                  onBlur={() => setTouched((p) => ({ ...p, telephone: true }))}
                  className={touched.telephone && errors.telephone ? "border-destructive" : ""}
                />
              {touched.telephone && errors.telephone && (
                <p className="text-sm text-destructive">{errors.telephone}</p>
              )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="adresse">Adresse *</Label>
                <Input
                  id="adresse"
                  name="adresse"
                  placeholder="Votre adresse"
                  value={formData.adresse}
                  onChange={handleChange}
                  onBlur={() => setTouched((p) => ({ ...p, adresse: true }))}
                  className={touched.adresse && errors.adresse ? "border-destructive" : ""}
                />
              {touched.adresse && errors.adresse && (
                <p className="text-sm text-destructive">{errors.adresse}</p>
              )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="codeParrainage">Code de parrainage (optionnel)</Label>
                <Input
                  id="codeParrainage"
                  name="codeParrainage"
                  placeholder="Code de parrainage"
                  value={formData.codeParrainage}
                  onChange={handleChange}
                  onBlur={() => setTouched((p) => ({ ...p, codeParrainage: true }))}
                  className={touched.codeParrainage && errors.codeParrainage ? "border-destructive" : ""}
                />
              {touched.codeParrainage && errors.codeParrainage && (
                <p className="text-sm text-destructive">{errors.codeParrainage}</p>
              )}
              </div>

            <Button 
              type="submit" 
              className="w-full" 
              size="lg"
              variant="hero"
              disabled={isLoading || !isFormValid}
            >
              {isLoading ? "Inscription..." : "S'inscrire"}
            </Button>
          </form>

          <div className="mt-6 text-center space-y-2">
            <p className="text-sm text-muted-foreground">
              Déjà un compte ?{" "}
              <Link to="/login" className="text-accent font-semibold hover:underline">
                Se connecter
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

export default Register;
