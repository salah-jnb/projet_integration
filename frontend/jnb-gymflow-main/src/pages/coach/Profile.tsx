import { useState, useEffect } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { toast } from "sonner";
import { UserCircle, Star } from "lucide-react";
import { ChangePasswordDialog } from "@/components/ChangePasswordDialog";
import { ProfilePhotoUpload } from "@/components/ProfilePhotoUpload";
import { usersApi, coachsApi, reservationsCoachingApi, notationsApi } from "@/lib/api";
import { z } from "zod";

const CoachProfile = () => {
  const { user } = useAuth();
  const [isEditing, setIsEditing] = useState(false);
  const [loading, setLoading] = useState(false);
  const [coachStats, setCoachStats] = useState<Record<string, unknown> | null>(null);
  const [noteGlobale, setNoteGlobale] = useState<number | null>(null);
  const [nombreAvis, setNombreAvis] = useState<number>(0);
  const [totalSessionsThisYear, setTotalSessionsThisYear] = useState<number>(0);
  const [formData, setFormData] = useState({
    nom: "",
    prenom: "",
    telephone: "",
    adresse: "",
    specialites: "",
    bio: "",
    photo: "",
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [touched, setTouched] = useState<Record<string, boolean>>({
    prenom: false,
    nom: false,
    telephone: false,
    adresse: false,
    specialites: false,
    bio: false,
  });
  const [photoUrl, setPhotoUrl] = useState<string | undefined>(undefined);

  useEffect(() => {
    const fetchData = async () => {
      if (user?.utilisateurId) {
        try {
          const [userResponse, coachResponse] = await Promise.all([
            usersApi.getById(user.utilisateurId),
            coachsApi.getDetails(user.utilisateurId),
          ]);
          
          const userData = userResponse.data;
          const coachData = coachResponse.data;
          
          setFormData({
            nom: userData.nom || "",
            prenom: userData.prenom || "",
            telephone: userData.telephone || "",
            adresse: userData.adresse || "",
            specialites: coachData.specialites || "Musculation, CrossFit, Nutrition",
            bio: coachData.bio || "Coach passionné avec plusieurs années d'expérience...",
            photo: userData.photo || "",
          });
          setCoachStats(coachData);
          const ng = typeof coachData.noteGlobale === 'number' ? coachData.noteGlobale : null;
          const na = typeof coachData.nombreAvis === 'number' ? coachData.nombreAvis : 0;
          setNoteGlobale(ng);
          setNombreAvis(na);

          try {
            const res = await reservationsCoachingApi.getByCoach(user.utilisateurId.toString());
            const currentYear = new Date().getFullYear();
            const totalYear = (res.data || []).filter((r) => {
              const d = new Date(r.dateSeance);
              return d.getFullYear() === currentYear;
            }).length;
            setTotalSessionsThisYear(totalYear);
          } catch { void 0; }

          if (ng === null) {
            try {
              const notesRes = await notationsApi.getByCoach(user.utilisateurId.toString());
              const notes = Array.isArray(notesRes.data) ? notesRes.data : [];
              const count = notes.length;
              const avg = count > 0 ? (notes.reduce((acc: number, n) => acc + Number(n.note || 0), 0) / count) : 0;
              setNoteGlobale(Number(avg.toFixed(1)));
              setNombreAvis(count);
            } catch { void 0; }
          }
          // Charger la photo via API
          try {
            const photoRes = await usersApi.getPhoto(user.utilisateurId);
            const objectUrl = URL.createObjectURL(photoRes.data);
            setPhotoUrl(objectUrl);
          } catch {
            setPhotoUrl(undefined);
          }
        } catch (error) {
          toast.error("Erreur lors du chargement des données");
        }
      }
    };
    fetchData();
  }, [user]);

  const handlePhotoUpload = async (file: File) => {
    if (!user?.utilisateurId) return;
    await usersApi.uploadPhoto(user.utilisateurId, file);
    try {
      const photoRes = await usersApi.getPhoto(user.utilisateurId);
      const objectUrl = URL.createObjectURL(photoRes.data);
      setPhotoUrl(objectUrl);
    } catch { void 0; }
  };

  const handleSave = async () => {
    if (!user?.utilisateurId) return;
    setTouched({ prenom: true, nom: true, telephone: true, adresse: true, specialites: true, bio: true });
    setErrors({});
    const normalized = {
      nom: normalizeText(formData.nom),
      prenom: normalizeText(formData.prenom),
      telephone: normalizeTelephone(formData.telephone),
      adresse: normalizeText(formData.adresse),
      specialites: normalizeText(formData.specialites),
      bio: normalizeText(formData.bio),
    };
    const res = profileSchema.safeParse(normalized);
    if (!res.success) {
      const fieldErrors: Record<string, string> = {};
      res.error.errors.forEach((err) => {
        if (err.path[0]) fieldErrors[String(err.path[0])] = err.message;
      });
      setErrors(fieldErrors);
      return;
    }

    setLoading(true);
    try {
      await usersApi.update(user.utilisateurId, {
        nom: normalized.nom,
        prenom: normalized.prenom,
        telephone: normalized.telephone,
        adresse: normalized.adresse,
      });
      toast.success("Profil mis à jour avec succès !");
      setIsEditing(false);
    } catch (error) {
      toast.error(error.response?.data?.message || "Erreur lors de la mise à jour");
    } finally {
      setLoading(false);
    }
  };

  const normalizeText = (v: string) => v.replace(/\s+/g, " ").trim();
  const normalizeTelephone = (v: string) => v.replace(/\s+/g, "").trim();
  const profileSchema = z.object({
    prenom: z
      .string()
      .min(2, "Prénom trop court")
      .max(50, "Prénom trop long")
      .regex(/^[\p{L}' -]+$/u, "Prénom invalide (lettres, espaces, -, ')")
      ,
    nom: z
      .string()
      .min(2, "Nom trop court")
      .max(50, "Nom trop long")
      .regex(/^[\p{L}' -]+$/u, "Nom invalide (lettres, espaces, -, ')")
      ,
    telephone: z.string().regex(/^\+?[0-9]{8,15}$/i, "Téléphone invalide (8-15 chiffres, optionnel +)"),
    adresse: z.string().min(5, "Adresse trop courte").max(120, "Adresse trop longue"),
    specialites: z
      .string()
      .min(1, "Spécialités requises")
      .refine((v) => {
        const tokens = v.split(",").map((t) => t.trim()).filter(Boolean);
        if (tokens.length === 0) return false;
        if (tokens.length > 8) return false;
        return tokens.every((t) => /^[\p{L}][\p{L} ' -]{1,30}$/u.test(t));
      }, "Spécialités invalides (lettres uniquement, séparées par des virgules)"),
    bio: z
      .string()
      .min(30, "Bio trop courte (min 30 caractères)")
      .max(1000, "Bio trop longue")
      .refine((v) => /[\p{L}]/u.test(v), "La bio doit contenir des lettres"),
  });
  const validateField = (name: keyof typeof touched, value: string) => {
    const candidate = {
      prenom: name === "prenom" ? normalizeText(value) : normalizeText(formData.prenom),
      nom: name === "nom" ? normalizeText(value) : normalizeText(formData.nom),
      telephone: name === "telephone" ? normalizeTelephone(value) : normalizeTelephone(formData.telephone),
      adresse: name === "adresse" ? normalizeText(value) : normalizeText(formData.adresse),
      specialites: name === "specialites" ? normalizeText(value) : normalizeText(formData.specialites),
      bio: name === "bio" ? normalizeText(value) : normalizeText(formData.bio),
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
    specialites: normalizeText(formData.specialites),
    bio: normalizeText(formData.bio),
  }).success;

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <div>
        <h1 className="text-3xl font-bold mb-2">Mon Profil Coach</h1>
        <p className="text-muted-foreground">Gérez vos informations professionnelles</p>
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

      <div className="grid md:grid-cols-2 gap-4">
        <Card>
          <CardHeader>
            <CardTitle className="text-sm font-medium">Note Globale</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex items-center gap-2">
              <Star className="h-6 w-6 fill-accent text-accent" />
              <span className="text-3xl font-bold">{noteGlobale ?? "—"}</span>
              <span className="text-muted-foreground">/5</span>
            </div>
            <p className="text-xs text-muted-foreground mt-2">Sur {nombreAvis} avis</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle className="text-sm font-medium">Séances Totales</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{totalSessionsThisYear}</div>
            <p className="text-xs text-muted-foreground mt-2">Cette année</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle className="flex items-center gap-2">
            <UserCircle className="h-5 w-5" />
            Informations Professionnelles
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
            <Label htmlFor="specialites">Spécialités</Label>
              <Input
                id="specialites"
                value={formData.specialites}
                disabled={!isEditing}
                onChange={(e) => {
                  const v = normalizeText(e.target.value);
                  setFormData({ ...formData, specialites: v });
                  setErrors((p) => ({ ...p, specialites: validateField("specialites", v) }));
                }}
                onBlur={() => setTouched((p) => ({ ...p, specialites: true }))}
                className={touched.specialites && errors.specialites ? "border-destructive" : ""}
                placeholder="Musculation, CrossFit, Nutrition..."
              />
              {touched.specialites && errors.specialites && (
                <p className="text-sm text-destructive">{errors.specialites}</p>
              )}
            <div className="flex gap-2 mt-2">
              {formData.specialites
                .split(",")
                .map((spec) => spec.trim())
                .filter(Boolean)
                .filter((spec) => /^[\p{L}][\p{L} ' -]{1,30}$/u.test(spec))
                .slice(0, 8)
                .map((spec, idx) => (
                  <Badge key={`${spec}-${idx}`} variant="secondary">
                    {spec}
                  </Badge>
                ))}
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="bio">Bio / Description</Label>
              <Textarea
                id="bio"
                value={formData.bio}
                disabled={!isEditing}
                onChange={(e) => {
                  const v = normalizeText(e.target.value);
                  setFormData({ ...formData, bio: v });
                  setErrors((p) => ({ ...p, bio: validateField("bio", v) }));
                }}
                onBlur={() => setTouched((p) => ({ ...p, bio: true }))}
                className={touched.bio && errors.bio ? "border-destructive" : ""}
                rows={4}
              />
              {touched.bio && errors.bio && (
                <p className="text-sm text-destructive">{errors.bio}</p>
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
          <CardTitle>Sécurité</CardTitle>
        </CardHeader>
        <CardContent>
          <ChangePasswordDialog userId={user?.utilisateurId || ""} />
        </CardContent>
      </Card>
    </div>
  );
};

export default CoachProfile;
