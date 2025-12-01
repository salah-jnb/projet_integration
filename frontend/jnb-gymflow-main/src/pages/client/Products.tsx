import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { produitsApi, API_BASE_URL } from "@/lib/api";
import { Loader2, Search, Package } from "lucide-react";

type Produit = {
  id: number;
  nom: string;
  description?: string;
  prix: number;
  categorie?: string;
  imageUrl?: string;
  actif: boolean;
};

const getImageSrc = (imageUrl?: string) => {
  if (!imageUrl) return undefined;
  if (imageUrl.startsWith("http")) return imageUrl;
  if (imageUrl.startsWith("/")) return `${API_BASE_URL}${imageUrl}`;
  return undefined;
};

const ClientProducts = () => {
  const [produits, setProduits] = useState<Produit[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");

  const fetchProduits = async () => {
    try {
      setLoading(true);
      const res = await produitsApi.getAll();
      setProduits(res.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProduits();
  }, []);

  const filtered = produits.filter((p) => {
    const t = searchTerm.toLowerCase();
    return (
      p.nom?.toLowerCase().includes(t) ||
      (p.categorie || "").toLowerCase().includes(t) ||
      (p.description || "").toLowerCase().includes(t)
    );
  });

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
        <h1 className="text-3xl font-bold mb-2">Produits</h1>
        <p className="text-muted-foreground">Consultez les produits disponibles</p>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center gap-2">
            <Search className="h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Rechercher par nom, catégorie..."
              className="max-w-sm"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {filtered.map((p) => (
              <Card key={p.id}>
                <CardHeader>
                  <CardTitle className="flex items-center justify-between">
                    <span className="flex items-center gap-2">
                      <Package className="h-5 w-5" />
                      {p.nom}
                    </span>
                    <Badge>{p.prix.toFixed(2)} DT</Badge>
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    {getImageSrc(p.imageUrl) && (
                      <img src={getImageSrc(p.imageUrl)} alt={p.nom} className="w-full max-h-[50vh] object-contain rounded" />
                    )}
                    <div className="text-sm text-muted-foreground">{p.categorie}</div>
                    <div className="text-sm">{p.description}</div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default ClientProducts;