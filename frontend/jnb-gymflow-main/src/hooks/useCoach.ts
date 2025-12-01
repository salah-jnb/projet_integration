import { useState, useEffect } from 'react';
import { coachsApi } from '@/lib/api';

export const useCoach = (utilisateurId: string) => {
  const [coach, setCoach] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchCoach = async () => {
      try {
        const response = await coachsApi.getDetails(utilisateurId);
        setCoach(response.data);
      } catch (err: any) {
        setError(err.response?.data?.message || 'Erreur lors du chargement');
      } finally {
        setLoading(false);
      }
    };

    if (utilisateurId) {
      fetchCoach();
    }
  }, [utilisateurId]);

  return { coach, loading, error };
};
