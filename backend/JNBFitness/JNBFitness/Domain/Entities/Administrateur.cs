namespace JNBFitness.Domain.Entities
{
    public class Administrateur
    {
        public long UtilisateurId { get; set; }

        // Navigation property
        public Utilisateur Utilisateur { get; set; }
    }
}
