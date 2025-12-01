import { useState, useRef, useEffect } from 'react';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import { Camera, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

interface ProfilePhotoUploadProps {
  currentPhoto?: string;
  userName: string;
  onPhotoUpload: (file: File) => Promise<void>;
}

export const ProfilePhotoUpload = ({ currentPhoto, userName, onPhotoUpload }: ProfilePhotoUploadProps) => {
  const [uploading, setUploading] = useState(false);
  const [preview, setPreview] = useState(currentPhoto);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Mettre à jour l'aperçu si la photo actuelle change (après GET API)
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    setPreview(currentPhoto);
  }, [currentPhoto]);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Validate file type
    if (!file.type.startsWith('image/')) {
      toast.error('Veuillez sélectionner une image valide');
      return;
    }

    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      toast.error('L\'image doit faire moins de 5MB');
      return;
    }

    setUploading(true);
    try {
      // Créer un aperçu local pour feedback immédiat
      const objectUrl = URL.createObjectURL(file);
      setPreview(objectUrl);

      // Upload via API (multipart/form-data)
      await onPhotoUpload(file);
      toast.success('Photo de profil mise à jour avec succès');
    } catch (error) {
      toast.error('Erreur lors de la mise à jour de la photo');
      setPreview(currentPhoto);
    } finally {
      setUploading(false);
    }
  };

  const getInitials = (name: string) => {
    const parts = name.split(' ');
    return parts.length >= 2 
      ? `${parts[0][0]}${parts[1][0]}`.toUpperCase()
      : name.substring(0, 2).toUpperCase();
  };

  return (
    <div className="flex flex-col items-center gap-4">
      <div className="relative">
        <Avatar className="h-32 w-32">
          <AvatarImage src={preview} alt={userName} />
          <AvatarFallback className="text-2xl">
            {getInitials(userName)}
          </AvatarFallback>
        </Avatar>
        <Button
          size="icon"
          variant="secondary"
          className="absolute bottom-0 right-0 rounded-full"
          onClick={() => fileInputRef.current?.click()}
          disabled={uploading}
        >
          {uploading ? (
            <Loader2 className="h-4 w-4 animate-spin" />
          ) : (
            <Camera className="h-4 w-4" />
          )}
        </Button>
      </div>
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        className="hidden"
        onChange={handleFileChange}
      />
      <p className="text-sm text-muted-foreground text-center">
        Cliquez sur l'icône pour changer votre photo
      </p>
    </div>
  );
};
