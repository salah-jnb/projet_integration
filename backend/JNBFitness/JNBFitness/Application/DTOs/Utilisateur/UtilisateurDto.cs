namespace JNBFitness.Application.DTOs.Utilisateur
{
    public class UtilisateurDto
    {
        public long Id { get; set; }
        public required string Email { get; set; }
        public required string Nom { get; set; }
        public required string Prenom { get; set; }
        public required string Telephone { get; set; }
        public required string Adresse { get; set; }
        public required string Photo { get; set; }
        public required string TypeUtilisateur { get; set; }
        public required string Statut { get; set; }
        public DateTime DateInscription { get; set; }
        public bool AbonneNewsletter { get; set; }
    }
}
