import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { FileText, CheckCircle2, XCircle, Eye, Loader2 } from "lucide-react";
import { api, articlesApi, API_BASE_URL } from "@/lib/api";
import { toast } from "sonner";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";

interface Article {
  id: number;
  titre: string;
  contenu: string;
  imageUrl?: string;
  coachNom: string;
  coachPrenom: string;
  statut: string;
  dateCreation: string;
}

const AdminArticles = () => {
  const [articles, setArticles] = useState<Article[]>([]);
  const [loading, setLoading] = useState(true);
  const [viewOpen, setViewOpen] = useState(false);
  const [viewArticle, setViewArticle] = useState<Article | null>(null);

  const fetchArticles = async () => {
    try {
      setLoading(true);
      const response = await articlesApi.getPending();
      setArticles(response.data);
    } catch (error) {
      console.error("Erreur lors du chargement des articles:", error);
      toast.error("Impossible de charger les articles");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchArticles();
  }, []);

  const handleValidate = async (id: number) => {
    try {
      await articlesApi.validate(id, { publier: true, commentaireAdmin: "" });
      toast.success("Article validé avec succès");
      fetchArticles();
    } catch (error) {
      console.error("Erreur lors de la validation:", error);
      toast.error("Impossible de valider l'article");
    }
  };

  const getImageSrc = (imageUrl?: string) => {
    if (!imageUrl) return null;
    if (imageUrl.startsWith("data:image")) return imageUrl;
    if (imageUrl.startsWith("http")) return imageUrl;
    if (imageUrl.startsWith("/")) return `${API_BASE_URL}${imageUrl}`;
    return `data:image/jpeg;base64,${imageUrl}`;
  };

  const handleView = async (id: number) => {
    try {
      const res = await articlesApi.getById(id.toString());
      setViewArticle(res.data);
      setViewOpen(true);
    } catch (error) {
      toast.error("Impossible de charger l'article");
    }
  };

  const handleReject = async (id: number) => {
    const commentaire = prompt("Raison du rejet (optionnel):");
    try {
      await articlesApi.validate(id, { publier: false, commentaireAdmin: commentaire || "" });
      toast.success("Article rejeté");
      fetchArticles();
    } catch (error) {
      console.error("Erreur lors du rejet:", error);
      toast.error("Impossible de rejeter l'article");
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

  if (loading) {
    return (
      <div className="p-6 flex justify-center items-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <div>
        <h1 className="text-3xl font-bold mb-2">Modération Articles</h1>
        <p className="text-muted-foreground">Validez ou rejetez les articles des coachs</p>
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
                    {article.statut.replace("_", " ")}
                  </Badge>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between">
                  <div className="text-sm text-muted-foreground space-y-1">
                    <p>
                      Par {article.coachPrenom} {article.coachNom}
                    </p>
                    <p>Soumis le {new Date(article.dateCreation).toLocaleDateString()}</p>
                  </div>
                  <div className="flex gap-2">
                    <Button size="sm" variant="outline" onClick={() => handleView(article.id)}>
                      <Eye className="mr-2 h-4 w-4" />
                      Voir
                    </Button>
                    {(article.statut === "EN_ATTENTE" || article.statut === "EN_ATTENTE_VALIDATION") && (
                      <>
                        <Button
                          size="sm"
                          onClick={() => handleValidate(article.id)}
                        >
                          <CheckCircle2 className="mr-2 h-4 w-4" />
                          Valider
                        </Button>
                        <Button
                          size="sm"
                          variant="destructive"
                          onClick={() => handleReject(article.id)}
                        >
                          <XCircle className="mr-2 h-4 w-4" />
                          Rejeter
                        </Button>
                      </>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>
          ))
        )}
      </div>
      <Dialog open={viewOpen} onOpenChange={setViewOpen}>
        <DialogContent className="max-w-3xl">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <FileText className="h-5 w-5" />
              {viewArticle?.titre}
            </DialogTitle>
          </DialogHeader>
          {viewArticle && (
            <div className="space-y-4">
              {getImageSrc(viewArticle.imageUrl) && (
                <img
                  src={getImageSrc(viewArticle.imageUrl) as string}
                  alt={viewArticle.titre}
                  className="w-full h-auto rounded-md border"
                />
              )}
              <div className="text-sm text-muted-foreground">
                <p>
                  Par {viewArticle.coachPrenom} {viewArticle.coachNom}
                </p>
                <p>
                  Soumis le {new Date(viewArticle.dateCreation).toLocaleDateString()}
                </p>
              </div>
              <div className="prose max-w-none">
                {viewArticle.contenu}
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default AdminArticles;
