import { useState, useEffect } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { toast } from "sonner";
import { UserCircle } from "lucide-react";
import { ChangePasswordDialog } from "@/components/ChangePasswordDialog";
import { ProfilePhotoUpload } from "@/components/ProfilePhotoUpload";
import { usersApi } from "@/lib/api";
import { z } from "zod";

const Profile = () => {
  const { user } = useAuth();
  const [isEditing, setIsEditing] = useState(false);
  const [loading, setLoading] = useState(false);
  const [newsletter, setNewsletter] = useState(false);
  const [formData, setFormData] = useState({
    nom: "",
    prenom: "",
    email: "",
    telephone: "",
    adresse: "",
    photo: "",
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [touched, setTouched] = useState<Record<string, boolean>>({
    prenom: false,
    nom: false,
    telephone: false,
    adresse: false,
  });
  const [photoUrl, setPhotoUrl] = useState<string | undefined>(undefined);

  useEffect(() => {
    const fetchUserData = async () => {
      if (user?.utilisateurId) {
        try {
          const response = await usersApi.getById(user.utilisateurId);
          const userData = response.data;
          setFormData({
            nom: userData.nom || "",
            prenom: userData.prenom || "",
            email: userData.email || "",
            telephone: userData.telephone || "",
            adresse: userData.adresse || "",
            photo: userData.photo || "",
          });
          setNewsletter(!!userData.abonneNewsletter);
          // Charger la photo via l'API sécurisée
          try {
            const photoRes = await usersApi.getPhoto(user.utilisateurId);
            const objectUrl = URL.createObjectURL(photoRes.data);
            setPhotoUrl(objectUrl);
          } catch {
            setPhotoUrl(undefined);
          }
        } catch (error: any) {
          toast.error("Erreur lors du chargement des données");
        }
      }
    };
    fetchUserData();
  }, [user]);

  const normalizeText = (v: string) => v.trim();
  const normalizeTelephone = (v: string) => v.replace(/\s+/g, "").trim();
  const profileSchema = z.object({
    prenom: z.string().min(1, "Prénom requis"),
    nom: z.string().min(1, "Nom requis"),
    telephone: z
      .string()
      .regex(/^\+?[0-9]{8,15}$/i, "Téléphone invalide (8-15 chiffres, optionnel +)"),
    adresse: z.string().min(5, "Adresse trop courte"),
  });
  const validateField = (name: keyof typeof touched, value: string) => {
    const candidate = {
      prenom: name === "prenom" ? normalizeText(value) : normalizeText(formData.prenom),
      nom: name === "nom" ? normalizeText(value) : normalizeText(formData.nom),
      telephone:
        name === "telephone" ? normalizeTelephone(value) : normalizeTelephone(formData.telephone),
      adresse: name === "adresse" ? normalizeText(value) : normalizeText(formData.adresse),
    };
    const res = profileSchema.safeParse(candidate);
    if (!res.success) {
      const err = res.error.errors.find((e) => String(e.path[0]) === name);
      return err ? err.message : "";
    }
    return "";
  };
  const isFormValid = profileSchema.safeParse({
    prenom: normalizeText(formData.prenom),
    nom: normalizeText(formData.nom),
    telephone: normalizeTelephone(formData.telephone),
    adresse: normalizeText(formData.adresse),
  }).success;

  const handlePhotoUpload = async (file: File) => {
    if (!user?.utilisateurId) return;
    try {
      await usersApi.uploadPhoto(user.utilisateurId, file);
      // Recharger la photo depuis l'API pour obtenir le Content-Type correct
      const photoRes = await usersApi.getPhoto(user.utilisateurId);
      const objectUrl = URL.createObjectURL(photoRes.data);
      setPhotoUrl(objectUrl);
    } catch (error: any) {
      toast.error(error.response?.data?.message || "Erreur lors du chargement de la photo");
    }
  };

  const handleSave = async () => {
    if (!user?.utilisateurId) return;
    setTouched({ prenom: true, nom: true, telephone: true, adresse: true });
    setErrors({});
    const normalized = {
      nom: normalizeText(formData.nom),
      prenom: normalizeText(formData.prenom),
      telephone: normalizeTelephone(formData.telephone),
      adresse: normalizeText(formData.adresse),
    };
    const result = profileSchema.safeParse(normalized);
    if (!result.success) {
      const fieldErrors: Record<string, string> = {};
      result.error.errors.forEach((err) => {
        if (err.path[0]) fieldErrors[String(err.path[0])] = err.message;
      });
      setErrors(fieldErrors);
      return;
    }

    setLoading(true);
    try {
      await usersApi.update(user.utilisateurId, normalized);
      toast.success("Profil mis à jour avec succès !");
      setIsEditing(false);
    } catch (error: any) {
      toast.error(error.response?.data?.message || "Erreur lors de la mise à jour");
    } finally {
      setLoading(false);
    }
  };

  const handleToggleNewsletter = async (checked: boolean) => {
    if (!user?.utilisateurId) return;
    try {
      await usersApi.updateNewsletter(user.utilisateurId, checked);
      setNewsletter(checked);
      toast.success("Préférence newsletter mise à jour");
    } catch (error: any) {
      toast.error(error.response?.data?.message || "Erreur lors de la mise à jour newsletter");
    }
  };

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <div>
        <h1 className="text-3xl font-bold mb-2">Mon Profil</h1>
        <p className="text-muted-foreground">Gérez vos informations personnelles</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Photo de Profil</CardTitle>
        </CardHeader>
        <CardContent>
          <ProfilePhotoUpload
            currentPhoto={photoUrl}
            userName={`${formData.prenom} ${formData.nom}`}
            onPhotoUpload={handlePhotoUpload}
          />
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle className="flex items-center gap-2">
            <UserCircle className="h-5 w-5" />
            Informations Personnelles
          </CardTitle>
          <Button
            variant={isEditing ? "outline" : "default"}
            onClick={() => setIsEditing(!isEditing)}
          >
            {isEditing ? "Annuler" : "Modifier"}
          </Button>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="prenom">Prénom</Label>
              <Input
                id="prenom"
                value={formData.prenom}
                disabled={!isEditing}
                onChange={(e) => {
                  const v = normalizeText(e.target.value);
                  setFormData({ ...formData, prenom: v });
                  setErrors((p) => ({ ...p, prenom: validateField("prenom", v) }));
                }}
                onBlur={() => setTouched((p) => ({ ...p, prenom: true }))}
                className={touched.prenom && errors.prenom ? "border-destructive" : ""}
              />
              {touched.prenom && errors.prenom && (
                <p className="text-sm text-destructive">{errors.prenom}</p>
              )}
            </div>
            <div className="space-y-2">
              <Label htmlFor="nom">Nom</Label>
              <Input
                id="nom"
                value={formData.nom}
                disabled={!isEditing}
                onChange={(e) => {
                  const v = normalizeText(e.target.value);
                  setFormData({ ...formData, nom: v });
                  setErrors((p) => ({ ...p, nom: validateField("nom", v) }));
                }}
                onBlur={() => setTouched((p) => ({ ...p, nom: true }))}
                className={touched.nom && errors.nom ? "border-destructive" : ""}
              />
              {touched.nom && errors.nom && (
                <p className="text-sm text-destructive">{errors.nom}</p>
              )}
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="email">Email</Label>
            <Input
              id="email"
              type="email"
              value={formData.email}
              disabled
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="telephone">Téléphone</Label>
              <Input
                id="telephone"
                value={formData.telephone}
                disabled={!isEditing}
                onChange={(e) => {
                  const v = normalizeTelephone(e.target.value);
                  setFormData({ ...formData, telephone: v });
                  setErrors((p) => ({ ...p, telephone: validateField("telephone", v) }));
                }}
                onBlur={() => setTouched((p) => ({ ...p, telephone: true }))}
                className={touched.telephone && errors.telephone ? "border-destructive" : ""}
              />
              {touched.telephone && errors.telephone && (
                <p className="text-sm text-destructive">{errors.telephone}</p>
              )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="adresse">Adresse</Label>
              <Input
                id="adresse"
                value={formData.adresse}
                disabled={!isEditing}
                onChange={(e) => {
                  const v = normalizeText(e.target.value);
                  setFormData({ ...formData, adresse: v });
                  setErrors((p) => ({ ...p, adresse: validateField("adresse", v) }));
                }}
                onBlur={() => setTouched((p) => ({ ...p, adresse: true }))}
                className={touched.adresse && errors.adresse ? "border-destructive" : ""}
              />
              {touched.adresse && errors.adresse && (
                <p className="text-sm text-destructive">{errors.adresse}</p>
              )}
          </div>

          {isEditing && (
            <Button onClick={handleSave} className="w-full" disabled={loading || !isFormValid}>
              {loading ? "Enregistrement..." : "Enregistrer les modifications"}
            </Button>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Newsletter</CardTitle>
        </CardHeader>
        <CardContent className="flex items-center justify-between">
          <div>
            <p className="text-sm text-muted-foreground">Recevoir les offres et actualités</p>
          </div>
          <Switch checked={newsletter} onCheckedChange={handleToggleNewsletter} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Sécurité</CardTitle>
        </CardHeader>
        <CardContent>
          <ChangePasswordDialog userId={user?.utilisateurId || ""} />
        </CardContent>
      </Card>
    </div>
  );
};

export default Profile;
