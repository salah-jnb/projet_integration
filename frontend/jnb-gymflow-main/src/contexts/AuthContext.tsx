import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { authApi } from '@/lib/api';
import { toast } from 'sonner';

interface User {
  utilisateurId: string;
  email: string;
  nom: string;
  prenom: string;
  typeUtilisateur: 'CLIENT' | 'COACH' | 'ADMINISTRATEUR';
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  register: (data: RegisterData) => Promise<void>;
  logout: () => void;
  isLoading: boolean;
}

interface RegisterData {
  email: string;
  motDePasse: string;
  nom: string;
  prenom: string;
  telephone: string;
  adresse: string;
  codeParrainage?: string;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Check for existing session
    const storedToken = localStorage.getItem('jnb_token');
    const storedUser = localStorage.getItem('jnb_user');
    
    if (storedToken && storedUser) {
      setToken(storedToken);
      setUser(JSON.parse(storedUser));
    }
    setIsLoading(false);
  }, []);

  const login = async (email: string, motDePasse: string) => {
    try {
      const response = await authApi.login(email, motDePasse);
      const { token, utilisateurId, email: userEmail, nom, prenom, typeUtilisateur } = response.data;
      
      const userData: User = {
        utilisateurId,
        email: userEmail,
        nom,
        prenom,
        typeUtilisateur,
      };

      localStorage.setItem('jnb_token', token);
      localStorage.setItem('jnb_user', JSON.stringify(userData));
      
      setToken(token);
      setUser(userData);
      
      toast.success('Connexion réussie !');
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 'Erreur de connexion';
      toast.error(errorMessage);
      throw error;
    }
  };

  const register = async (data: RegisterData) => {
    try {
      const response = await authApi.register(data);
      const { token, utilisateurId, email, nom, prenom, typeUtilisateur } = response.data;
      
      const userData: User = {
        utilisateurId,
        email,
        nom,
        prenom,
        typeUtilisateur,
      };

      localStorage.setItem('jnb_token', token);
      localStorage.setItem('jnb_user', JSON.stringify(userData));
      
      setToken(token);
      setUser(userData);
      
      toast.success('Inscription réussie !');
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 'Erreur lors de l\'inscription';
      toast.error(errorMessage);
      throw error;
    }
  };

  const logout = () => {
    localStorage.removeItem('jnb_token');
    localStorage.removeItem('jnb_user');
    setToken(null);
    setUser(null);
    toast.info('Déconnexion réussie');
  };

  return (
    <AuthContext.Provider value={{ user, token, login, register, logout, isLoading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
