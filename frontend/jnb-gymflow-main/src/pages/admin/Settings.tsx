import { useAuth } from "@/contexts/AuthContext";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Settings as SettingsIcon, Save } from "lucide-react";
import { toast } from "sonner";
import { ChangePasswordDialog } from "@/components/ChangePasswordDialog";

const Settings = () => {
  const { user } = useAuth();
  const handleSave = () => {
    toast.success("Paramètres enregistrés avec succès !");
  };

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <div>
        <h1 className="text-3xl font-bold mb-2">Paramètres</h1>
        <p className="text-muted-foreground">Configuration générale de la plateforme</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <SettingsIcon className="h-5 w-5" />
            Informations Générales
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="nomSalle">Nom de la salle</Label>
            <Input id="nomSalle" defaultValue="JNB FITNESS" />
          </div>

          <div className="space-y-2">
            <Label htmlFor="adresse">Adresse</Label>
            <Input id="adresse" defaultValue="123 Avenue Habib Bourguiba, Tunis" />
          </div>

          <div className="grid md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="telephone">Téléphone</Label>
              <Input id="telephone" defaultValue="+216 71 123 456" />
            </div>
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" defaultValue="contact@jnbfitness.tn" />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Horaires d'ouverture</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="ouverture">Ouverture</Label>
              <Input id="ouverture" type="time" defaultValue="06:00" />
            </div>
            <div className="space-y-2">
              <Label htmlFor="fermeture">Fermeture</Label>
              <Input id="fermeture" type="time" defaultValue="22:00" />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Paramètres Financiers</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="commission">Commission par séance coaching (%)</Label>
            <Input id="commission" type="number" defaultValue="15" />
          </div>

          <div className="space-y-2">
            <Label htmlFor="delaiAnnulation">Délai d'annulation (heures)</Label>
            <Input id="delaiAnnulation" type="number" defaultValue="24" />
          </div>
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

      <Button onClick={handleSave} className="w-full">
        <Save className="mr-2 h-4 w-4" />
        Enregistrer les modifications
      </Button>
    </div>
  );
};

export default Settings;
