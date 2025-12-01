import { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { articlesApi, API_BASE_URL } from '@/lib/api';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Loader2, Heart, MessageCircle, Share2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { format } from 'date-fns';
import { fr } from 'date-fns/locale';

interface Article {
  id: number;
  titre: string;
  contenu: string;
  imageUrl?: string;
  statut: string;
  dateCreation: string;
  coachNom: string;
  coachPrenom: string;
  coachPhoto?: string;
}

const Home = () => {
  const { user } = useAuth();
  const [articles, setArticles] = useState<Article[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchArticles();
  }, []);

  const fetchArticles = async () => {
    try {
      const response = await articlesApi.getAll();
      setArticles(response.data);
    } catch (error) {
      console.error('Erreur lors du chargement des articles:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Loader2 className="h-8 w-8 animate-spin" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background">
      <div className="max-w-2xl mx-auto p-6 space-y-6 animate-fade-in">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold mb-2">
            Bienvenue, {user?.prenom} 👋
          </h1>
          <p className="text-muted-foreground">
            Découvrez les derniers articles de nos coachs
          </p>
        </div>

        {articles.length === 0 ? (
          <Card>
            <CardContent className="py-8">
              <p className="text-center text-muted-foreground">
                Aucun article publié pour le moment
              </p>
            </CardContent>
          </Card>
        ) : (
          articles.map((article) => (
            <Card key={article.id} className="overflow-hidden hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="flex items-center gap-3 mb-4">
                  <Avatar>
                    <AvatarImage src={article.coachPhoto} />
                    <AvatarFallback>
                      {article.coachPrenom[0]}{article.coachNom[0]}
                    </AvatarFallback>
                  </Avatar>
                  <div className="flex-1">
                    <p className="font-semibold">
                      {article.coachPrenom} {article.coachNom}
                    </p>
                    <p className="text-sm text-muted-foreground">
                      {format(new Date(article.dateCreation), 'dd MMMM yyyy', { locale: fr })}
                    </p>
                  </div>
                  <Badge variant={article.statut === 'PUBLIE' ? 'default' : 'secondary'}>
                    {article.statut}
                  </Badge>
                </div>
                <h2 className="text-2xl font-bold">{article.titre}</h2>
              </CardHeader>
              <CardContent>
                {article.imageUrl && (
                  <img
                    src={article.imageUrl.startsWith('/') ? `${API_BASE_URL}${article.imageUrl}` : article.imageUrl}
                    alt={article.titre}
                    className="w-full max-h-[60vh] object-contain rounded-lg mb-4"
                  />
                )}
                <p className="text-muted-foreground whitespace-pre-wrap mb-4">
                  {article.contenu}
                </p>
                <div className="flex items-center gap-4 pt-4 border-t">
                  <Button variant="ghost" size="sm">
                    <Heart className="h-4 w-4 mr-2" />
                    J'aime
                  </Button>
                  <Button variant="ghost" size="sm">
                    <MessageCircle className="h-4 w-4 mr-2" />
                    Commenter
                  </Button>
                  <Button variant="ghost" size="sm">
                    <Share2 className="h-4 w-4 mr-2" />
                    Partager
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))
        )}
      </div>
    </div>
  );
};

export default Home;
