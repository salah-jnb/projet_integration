import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Switch } from "@/components/ui/switch";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { Badge } from "@/components/ui/badge";
import { toast } from "sonner";
import { produitsApi, API_BASE_URL } from "@/lib/api";
import { Loader2, Plus, Pencil, Trash2, Search, Package } from "lucide-react";

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

const AdminProducts = () => {
  const [produits, setProduits] = useState<Produit[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [editOpen, setEditOpen] = useState(false);
  const [selected, setSelected] = useState<Produit | null>(null);
  const [nom, setNom] = useState("");
  const [description, setDescription] = useState("");
  const [prix, setPrix] = useState<string>("");
  const [categorie, setCategorie] = useState("");
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [actif, setActif] = useState<boolean>(true);

  const fetchProduits = async () => {
    try {
      setLoading(true);
      const res = await produitsApi.getAllAdmin();
      setProduits(res.data);
    } catch {
      toast.error("Impossible de charger les produits");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProduits();
  }, []);

  const resetForm = () => {
    setNom("");
    setDescription("");
    setPrix("");
    setCategorie("");
    setImageFile(null);
    setActif(true);
  };

  const openCreate = () => {
    resetForm();
    setCreateOpen(true);
  };

  const submitCreate = async () => {
    if (!nom || !prix) {
      toast.error("Nom et prix requis");
      return;
    }
    const normalized = prix.replace(',', '.').trim();
    const prixNum = Number(normalized);
    if (!isFinite(prixNum)) {
      toast.error("Prix invalide (utilisez un nombre décimal)");
      return;
    }
    try {
      const form = new FormData();
      form.append("Nom", nom);
      if (description) form.append("Description", description);
      form.append("Prix", prixNum.toString());
      if (categorie) form.append("Categorie", categorie);
      if (imageFile) form.append("image", imageFile);
      await produitsApi.create(form);
      toast.success("Produit créé");
      setCreateOpen(false);
      fetchProduits();
    } catch {
      toast.error("Échec de création");
    }
  };

  const openEdit = (p: Produit) => {
    setSelected(p);
    setNom(p.nom);
    setDescription(p.description || "");
    setPrix(String(p.prix));
    setCategorie(p.categorie || "");
    setImageFile(null);
    setActif(!!p.actif);
    setEditOpen(true);
  };

  const submitEdit = async () => {
    if (!selected) return;
    const normalized = prix.replace(',', '.').trim();
    const prixNum = Number(normalized);
    if (!isFinite(prixNum)) {
      toast.error("Prix invalide (utilisez un nombre décimal)");
      return;
    }
    try {
      const form = new FormData();
      if (nom) form.append("Nom", nom);
      if (description) form.append("Description", description);
      form.append("Prix", prixNum.toString());
      if (categorie) form.append("Categorie", categorie);
      form.append("Actif", String(actif));
      if (imageFile) form.append("image", imageFile);
      await produitsApi.update(selected.id, form);
      toast.success("Produit mis à jour");
      setEditOpen(false);
      fetchProduits();
    } catch {
      toast.error("Échec de mise à jour");
    }
  };

  const remove = async (id: number) => {
    try {
      await produitsApi.delete(id);
      toast.success("Produit supprimé");
      fetchProduits();
    } catch {
      toast.error("Échec de suppression");
    }
  };

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
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold mb-2">Gestion Produits</h1>
          <p className="text-muted-foreground">Administration des articles boutique</p>
        </div>
        <Button onClick={openCreate}>
          <Plus className="h-4 w-4 mr-2" />
          Ajouter
        </Button>
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
          <div className="space-y-3">
            {filtered.length === 0 ? (
              <p className="text-center text-muted-foreground py-8">Aucun produit</p>
            ) : (
              filtered.map((p) => (
                <div key={p.id} className="flex items-center justify-between p-4 border border-border rounded-lg">
                  <div className="flex items-center gap-4">
                    <div className="h-16 w-16 bg-muted rounded overflow-hidden flex items-center justify-center">
                      {getImageSrc(p.imageUrl) ? (
                        <img src={getImageSrc(p.imageUrl)} alt={p.nom} className="h-16 w-16 object-contain" />
                      ) : (
                        <Package className="h-6 w-6 text-muted-foreground" />
                      )}
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-semibold">{p.nom}</span>
                        <Badge variant={p.actif ? "default" : "secondary"}>{p.actif ? "Actif" : "Inactif"}</Badge>
                      </div>
                      <div className="text-sm text-muted-foreground">{p.categorie} • {p.prix.toFixed(2)} DT</div>
                      <div className="text-sm">{p.description}</div>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <Button variant="outline" onClick={() => openEdit(p)}>
                      <Pencil className="h-4 w-4 mr-2" />
                      Modifier
                    </Button>
                    <Button variant="destructive" onClick={() => remove(p.id)}>
                      <Trash2 className="h-4 w-4 mr-2" />
                      Supprimer
                    </Button>
                  </div>
                </div>
              ))
            )}
          </div>
        </CardContent>
      </Card>

      <Dialog open={createOpen} onOpenChange={setCreateOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Nouveau produit</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>Nom</Label>
              <Input value={nom} onChange={(e) => setNom(e.target.value)} />
            </div>
            <div className="space-y-2">
              <Label>Description</Label>
              <Input value={description} onChange={(e) => setDescription(e.target.value)} />
            </div>
            <div className="space-y-2">
              <Label>Prix</Label>
              <Input type="number" step="0.01" inputMode="decimal" placeholder="Ex: 12.50" value={prix} onChange={(e) => setPrix(e.target.value)} />
            </div>
            <div className="space-y-2">
              <Label>Catégorie</Label>
              <Input value={categorie} onChange={(e) => setCategorie(e.target.value)} />
            </div>
            <div className="space-y-2">
              <Label>Image</Label>
              <Input type="file" accept="image/*" onChange={(e) => setImageFile(e.target.files?.[0] || null)} />
            </div>
          </div>
          <DialogFooter>
            <Button onClick={submitCreate}>Créer</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={editOpen} onOpenChange={setEditOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Modifier produit</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>Nom</Label>
              <Input value={nom} onChange={(e) => setNom(e.target.value)} />
            </div>
            <div className="space-y-2">
              <Label>Description</Label>
              <Input value={description} onChange={(e) => setDescription(e.target.value)} />
            </div>
            <div className="space-y-2">
              <Label>Prix</Label>
              <Input type="number" step="0.01" inputMode="decimal" placeholder="Ex: 12.50" value={prix} onChange={(e) => setPrix(e.target.value)} />
            </div>
            <div className="space-y-2">
              <Label>Catégorie</Label>
              <Input value={categorie} onChange={(e) => setCategorie(e.target.value)} />
            </div>
            <div className="flex items-center justify-between py-2">
              <Label>Actif</Label>
              <Switch checked={actif} onCheckedChange={(v) => setActif(!!v)} />
            </div>
            <div className="space-y-2">
              <Label>Image</Label>
              <Input type="file" accept="image/*" onChange={(e) => setImageFile(e.target.files?.[0] || null)} />
            </div>
          </div>
          <DialogFooter>
            <Button onClick={submitEdit}>Enregistrer</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default AdminProducts;
