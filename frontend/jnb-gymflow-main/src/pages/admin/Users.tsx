import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Select, SelectTrigger, SelectValue, SelectContent, SelectItem } from "@/components/ui/select";
import { Switch } from "@/components/ui/switch";
import { Search, Edit, Trash2, Loader2 } from "lucide-react";
import { CreateUserDialog } from "@/components/admin/CreateUserDialog";
import { api, usersApi } from "@/lib/api";
import { toast } from "sonner";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";

interface User {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  typeUtilisateur: string;
  telephone?: string;
  adresse?: string;
  statut?: string;
  abonneNewsletter?: boolean;
}

const Users = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [userToDelete, setUserToDelete] = useState<number | null>(null);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const response = await usersApi.getAll();
      setUsers(response.data);
    } catch (error) {
      console.error("Erreur lors du chargement des utilisateurs:", error);
      toast.error("Impossible de charger les utilisateurs");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleDelete = async (id: number) => {
    try {
      await usersApi.delete(id.toString());
      toast.success("Utilisateur supprimé avec succès");
      setUserToDelete(null);
      fetchUsers();
    } catch (error) {
      console.error("Erreur lors de la suppression:", error);
      toast.error("Impossible de supprimer l'utilisateur");
    }
  };

  const handleEdit = (user: User) => {
    setEditingUser({ ...user });
    setIsEditDialogOpen(true);
  };

  const handleUpdateUser = async () => {
    if (!editingUser || !editingUser.id) {
      toast.error("Utilisateur invalide");
      return;
    }

    try {
      const updateData = {
        nom: editingUser.nom,
        prenom: editingUser.prenom,
        telephone: editingUser.telephone || "",
        adresse: editingUser.adresse || "",
      };

      await api.put(`/api/Utilisateurs/${editingUser.id}`, updateData);
      toast.success("Utilisateur modifié avec succès");
      setIsEditDialogOpen(false);
      setEditingUser(null);
      fetchUsers();
    } catch (error) {
      console.error("Erreur lors de la modification:", error);
      toast.error("Impossible de modifier l'utilisateur");
    }
  };

  const filteredUsers = users.filter(
    (user) =>
      user.nom?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.prenom?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email?.toLowerCase().includes(searchTerm.toLowerCase())
  );

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
          <h1 className="text-3xl font-bold mb-2">Gestion Utilisateurs</h1>
          <p className="text-muted-foreground">Liste et gestion des membres</p>
        </div>
        <CreateUserDialog onUserCreated={fetchUsers} />
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center gap-2">
            <Search className="h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Rechercher par nom ou email..."
              className="max-w-sm"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {filteredUsers.length === 0 ? (
              <p className="text-center text-muted-foreground py-8">
                Aucun utilisateur trouvé
              </p>
            ) : (
              filteredUsers.map((user) => (
                <div
                  key={user.id}
                  className="flex items-center justify-between p-4 border border-border rounded-lg hover:shadow-md transition-shadow"
                >
                  <div className="flex-1">
                    <p className="font-semibold">
                      {user.prenom} {user.nom}
                    </p>
                    <p className="text-sm text-muted-foreground">{user.email}</p>
                    {user.telephone && (
                      <p className="text-sm text-muted-foreground">{user.telephone}</p>
                    )}
                  </div>
                  <div className="flex items-center gap-3">
                    <div className="flex items-center gap-2">
                      <Badge variant="outline">{user.typeUtilisateur}</Badge>
                      {user.statut && (
                        <Badge variant={user.statut === "ACTIF" ? "default" : "secondary"}>{user.statut}</Badge>
                      )}
                      <Select
                        defaultValue={user.statut}
                        onValueChange={async (value) => {
                          try {
                            await usersApi.updateStatus(user.id.toString(), value);
                            toast.success("Statut mis à jour");
                            fetchUsers();
                          } catch {
                            toast.error("Échec mise à jour statut");
                          }
                        }}
                      >
                        <SelectTrigger className="w-[160px]">
                          <SelectValue placeholder="Changer statut" />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="ACTIF">Activer</SelectItem>
                          <SelectItem value="SUSPENDU">Suspendre</SelectItem>
                          <SelectItem value="INACTIF">Désactiver</SelectItem>
                        </SelectContent>
                      </Select>
                      <div className="flex items-center gap-2">
                        <span className="text-sm text-muted-foreground">Newsletter</span>
                        <Switch
                          checked={!!user.abonneNewsletter}
                          onCheckedChange={async (checked) => {
                            try {
                              await usersApi.updateNewsletter(user.id.toString(), checked);
                              toast.success("Préférence newsletter mise à jour");
                              fetchUsers();
                            } catch {
                              toast.error("Échec mise à jour newsletter");
                            }
                          }}
                        />
                      </div>
                    </div>
                    <div className="flex gap-2">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => handleEdit(user)}
                      >
                        <Edit className="h-4 w-4" />
                      </Button>

                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => setUserToDelete(user.id)}
                      >
                        <Trash2 className="h-4 w-4 text-destructive" />
                      </Button>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </CardContent>
      </Card>

      {/* AlertDialog de suppression */}
      <AlertDialog 
        open={userToDelete !== null} 
        onOpenChange={(open) => !open && setUserToDelete(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Êtes-vous sûr?</AlertDialogTitle>
            <AlertDialogDescription>
              Cette action ne peut pas être annulée. Cela supprimera définitivement l'utilisateur
              {userToDelete && users.find(u => u.id === userToDelete) && (
                <> <strong>{users.find(u => u.id === userToDelete)?.prenom} {users.find(u => u.id === userToDelete)?.nom}</strong></>
              )}.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={() => setUserToDelete(null)}>
              Annuler
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={() => {
                if (userToDelete) {
                  handleDelete(userToDelete);
                }
              }}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              Supprimer
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Dialog de modification */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Modifier l'utilisateur</DialogTitle>
            <DialogDescription>
              Modifiez les informations de l'utilisateur
            </DialogDescription>
          </DialogHeader>
          {editingUser && (
            <div className="grid gap-4 py-4">
              <div className="grid gap-2">
                <Label htmlFor="edit-prenom">Prénom</Label>
                <Input
                  id="edit-prenom"
                  value={editingUser.prenom || ""}
                  onChange={(e) =>
                    setEditingUser({ ...editingUser, prenom: e.target.value })
                  }
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="edit-nom">Nom</Label>
                <Input
                  id="edit-nom"
                  value={editingUser.nom || ""}
                  onChange={(e) =>
                    setEditingUser({ ...editingUser, nom: e.target.value })
                  }
                />
              </div>
             
              <div className="grid gap-2">
                <Label htmlFor="edit-telephone">Téléphone</Label>
                <Input
                  id="edit-telephone"
                  value={editingUser.telephone || ""}
                  onChange={(e) =>
                    setEditingUser({ ...editingUser, telephone: e.target.value })
                  }
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="edit-adresse">Adresse</Label>
                <Input
                  id="edit-adresse"
                  value={editingUser.adresse || ""}
                  onChange={(e) =>
                    setEditingUser({ ...editingUser, adresse: e.target.value })
                  }
                />
              </div>
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsEditDialogOpen(false)}>
              Annuler
            </Button>
            <Button onClick={handleUpdateUser}>Enregistrer</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default Users;
