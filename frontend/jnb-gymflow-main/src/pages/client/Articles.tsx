import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Loader2, FileText } from "lucide-react";
import { articlesApi, API_BASE_URL } from "@/lib/api";

interface Article {
  id: number;
  titre: string;
  contenu: string;
  imageUrl?: string;
  statut: string;
  dateCreation: string;
  coachNom?: string;
  coachPrenom?: string;
}

const ClientArticles = () => {
  const [articles, setArticles] = useState<Article[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchArticles = async () => {
      try {
        setLoading(true);
        const response = await articlesApi.getAll();
        setArticles(response.data);
      } catch (error) {
        // ignore
      } finally {
        setLoading(false);
      }
    };
    fetchArticles();
  }, []);

  const getStatusVariant = (statut: string) => {
    switch (statut) {
      case "PUBLIE":
        return "default";
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
        <h1 className="text-3xl font-bold mb-2">Articles publiés</h1>
        <p className="text-muted-foreground">Les articles validés par l'administration</p>
      </div>

      <div className="grid gap-4">
        {articles.length === 0 ? (
          <p className="text-center text-muted-foreground py-8">
            Aucun article publié
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
                <div className="space-y-3">
                  {article.imageUrl && (
                    <div className="w-full h-48 overflow-hidden rounded-lg">
                      <img
                        src={getImageSrc(article.imageUrl) || undefined}
                        alt={article.titre}
                        className="w-full h-full object-cover"
                      />
                    </div>
                  )}
                  <p className="text-sm text-muted-foreground">
                    {article.contenu}
                  </p>
                </div>
              </CardContent>
            </Card>
          ))
        )}
      </div>
    </div>
  );
};

export default ClientArticles;
  const getImageSrc = (imageUrl?: string) => {
    if (!imageUrl) return null;
    if (imageUrl.startsWith('data:image')) return imageUrl;
    if (imageUrl.startsWith('http')) return imageUrl;
    if (imageUrl.startsWith('/')) return `${API_BASE_URL}${imageUrl}`;
    return `data:image/jpeg;base64,${imageUrl}`;
  };