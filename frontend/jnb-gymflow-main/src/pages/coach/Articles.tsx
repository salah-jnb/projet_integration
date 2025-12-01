import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { FileText, Eye, Edit, Trash2, Loader2 } from "lucide-react";
import { CreateArticleDialog } from "@/components/coach/CreateArticleDialog";
import { articlesApi, api, API_BASE_URL } from "@/lib/api";
import { toast } from "sonner";
import { useAuth } from "@/contexts/AuthContext";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";

interface Article {
  id: number;
  coachId: number;
  titre: string;
  contenu: string;
  imageUrl: string;
  statut: string;
  dateCreation: string;
  datePublication?: string;
}

const CoachArticles = () => {
  const { user } = useAuth();
  const [articles, setArticles] = useState<Article[]>([]);
  const [loading, setLoading] = useState(true);
  const [editOpen, setEditOpen] = useState(false);
  const [editingArticle, setEditingArticle] = useState<Article | null>(null);
  const [editFile, setEditFile] = useState<File | null>(null);
  const [editFileError, setEditFileError] = useState<string | null>(null);
  

  const editSchema = z.object({
    titre: z.string().min(5, "Titre trop court"),
    contenu: z.string().min(1, "Contenu requis"),
  });
  type EditFormData = z.infer<typeof editSchema>;
  const { register, handleSubmit, formState: { errors }, reset } = useForm<EditFormData>({
    resolver: zodResolver(editSchema),
  });

  const fetchArticles = async () => {
    if (!user?.utilisateurId) return;
    
    try {
      setLoading(true);
      const response = await articlesApi.getByCoach(user.utilisateurId.toString());
      setArticles(response.data);
      console.log(response.data);
    } catch (error) {
      console.error("Erreur lors du chargement des articles:", error);
      toast.error("Impossible de charger les articles");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user?.utilisateurId) {
      fetchArticles();
    }
  }, [user?.utilisateurId]);

  const handleDelete = async (id: number) => {
    if (!confirm("Voulez-vous vraiment supprimer cet article ?")) return;
    
    try {
      await api.delete(`/api/Articles/${id}`);
      toast.success("Article supprimé avec succès");
      fetchArticles();
    } catch (error) {
      console.error("Erreur lors de la suppression:", error);
      toast.error("Impossible de supprimer l'article");
    }
  };

  const handlePublish = async (id: number) => {
    try {
      await articlesApi.submitForValidation(id);
      toast.success("Article envoyé pour validation");
      fetchArticles();
    } catch (error) {
      console.error("Erreur lors de la publication:", error);
      toast.error("Impossible de publier l'article");
    }
  };

  const openEdit = (article: Article) => {
    setEditingArticle(article);
    setEditOpen(true);
    reset({ titre: article.titre, contenu: article.contenu });
    setEditFile(null);
    setEditFileError(null);
  };

  const submitEdit = async (data: EditFormData) => {
    if (!editingArticle) return;
    try {
      const formData = new FormData();
      formData.append("titre", data.titre);
      formData.append("contenu", data.contenu);
      if (editFile) {
        formData.append("image", editFile);
      }
      await articlesApi.update(editingArticle.id.toString(), formData);
      toast.success("Article mis à jour");
      setEditOpen(false);
      setEditingArticle(null);
      setEditFile(null);
      setEditFileError(null);
      fetchArticles();
    } catch (error) {
      toast.error("Impossible de mettre à jour l'article");
    }
  };

  const getStatusVariant = (statut: string) => {
    switch (statut) {
      case "PUBLIE":
        return "default";
      case "EN_ATTENTE":
      case "EN_ATTENTE_VALIDATION":
        return "secondary";
      case "REJETE":
        return "destructive";
      default:
        return "outline";
    }
  };

  const getStatusLabel = (statut: string) => {
    switch (statut) {
      case "PUBLIE":
        return "Publié";
      case "EN_ATTENTE":
      case "EN_ATTENTE_VALIDATION":
        return "En attente";
      case "REJETE":
        return "Rejeté";
      default:
        return statut;
    }
  };

  // Fonction pour obtenir la source d'image
  const getImageSrc = (imageUrl: string) => {
    if (!imageUrl) return null;
    if (imageUrl.startsWith('data:image')) return imageUrl;
    if (imageUrl.startsWith('http')) return imageUrl;
    if (imageUrl.startsWith('/')) return `${API_BASE_URL}${imageUrl}`;
    return `data:image/jpeg;base64,${imageUrl}`;
  };

  if (loading) {
    return (
      <div className="p-6 flex justify-center items-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <>
    <div className="p-6 space-y-6 animate-fade-in">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold mb-2">Mes Articles</h1>
          <p className="text-muted-foreground">Rédigez et partagez vos connaissances</p>
        </div>
        <CreateArticleDialog onCreated={fetchArticles} />
      </div>

      <div className="grid gap-4">
        {articles.length === 0 ? (
          <p className="text-center text-muted-foreground py-8">
            Aucun article trouvé
          </p>
        ) : (
          articles.map((article) => (
            <Card key={article.id}>
              <CardHeader>
                <CardTitle className="flex items-center justify-between">
                  <span className="flex items-center gap-2">
                    <FileText className="h-5 w-5" />
                    {article.titre}
                  </span>
                  <Badge variant={getStatusVariant(article.statut)}>
                    {getStatusLabel(article.statut)}
                  </Badge>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {article.imageUrl && (
                    <div className="w-full h-48 md:h-56 lg:h-64 rounded-lg bg-muted flex items-center justify-center overflow-hidden border border-muted-foreground/20">
                      <img
                        src={getImageSrc(article.imageUrl)}
                        alt={article.titre}
                        className="max-w-full max-h-full object-contain"
                        loading="lazy"
                      />
                    </div>
                  )}
                  <p className="text-sm text-muted-foreground line-clamp-2">
                    {article.contenu}
                  </p>
                  <div className="flex items-center justify-between">
                    <div className="text-sm text-muted-foreground space-y-1">
                      <p>Créé le {new Date(article.dateCreation).toLocaleDateString()}</p>
                      {article.datePublication && (
                        <p>Publié le {new Date(article.datePublication).toLocaleDateString()}</p>
                      )}
                    </div>
                    <div className="flex gap-2">
                      {article.statut === "BROUILLON" && (
                        <Button size="sm" onClick={() => handlePublish(article.id)}>
                          Publier
                        </Button>
                      )}
                      <Button size="sm" variant="outline" onClick={() => openEdit(article)}>
                        <Edit className="h-4 w-4" />
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => handleDelete(article.id)}
                      >
                        <Trash2 className="h-4 w-4 text-destructive" />
                      </Button>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))
        )}
      </div>
    </div>
    <Dialog open={editOpen} onOpenChange={setEditOpen}>
      <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Modifier l'article</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit(submitEdit)} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="edit-titre">Titre *</Label>
            <Input id="edit-titre" {...register("titre")} />
            {errors.titre && (
              <p className="text-sm text-destructive">{errors.titre.message}</p>
            )}
          </div>
          <div className="space-y-2">
            <Label htmlFor="edit-contenu">Contenu *</Label>
            <Textarea id="edit-contenu" rows={12} {...register("contenu")} />
            {errors.contenu && (
              <p className="text-sm text-destructive">{errors.contenu.message}</p>
            )}
          </div>
          <div className="space-y-2">
            <Label htmlFor="edit-image">Image (optionnel)</Label>
            <Input
              id="edit-image"
              type="file"
              accept="image/*"
              onChange={(e) => {
                const f = e.target.files?.[0] || null;
                if (!f) {
                  setEditFile(null);
                  setEditFileError(null);
                  return;
                }
                const allowed = ["image/jpeg", "image/png", "image/webp"];
                const maxSize = 5 * 1024 * 1024;
                if (!allowed.includes(f.type)) {
                  setEditFile(null);
                  setEditFileError("Format d'image invalide (JPEG, PNG, WEBP)");
                  return;
                }
                if (f.size > maxSize) {
                  setEditFile(null);
                  setEditFileError("Image trop volumineuse (max 5MB)");
                  return;
                }
                setEditFileError(null);
                setEditFile(f);
              }}
            />
            {editFileError && <p className="text-sm text-destructive">{editFileError}</p>}
          </div>
          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={() => setEditOpen(false)}>
              Annuler
            </Button>
            <Button type="submit">Enregistrer</Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
    </>
  );
};

export default CoachArticles;
