import { useState, useEffect } from 'react';
import { clientsApi } from '@/lib/api';

export const useClient = (utilisateurId: string) => {
  const [client, setClient] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchClient = async () => {
      try {
        const response = await clientsApi.getByUserId(utilisateurId);
        setClient(response.data);
      } catch (err: any) {
        setError(err.response?.data?.message || 'Erreur lors du chargement');
      } finally {
        setLoading(false);
      }
    };

    if (utilisateurId) {
      fetchClient();
    }
  }, [utilisateurId]);

  return { client, loading, error };
};
