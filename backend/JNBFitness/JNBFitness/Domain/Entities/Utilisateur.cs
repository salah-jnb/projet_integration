using JNBFitness.Domain.Enums;
using static System.Runtime.InteropServices.JavaScript.JSType;

namespace JNBFitness.Domain.Entities
{
    public class Utilisateur
    {
        public long Id { get; set; }
        public required string Email { get; set; }
        public required string MotDePasse { get; set; }
        public required string Nom { get; set; }
        public required string Prenom { get; set; }
        public string? Telephone { get; set; }
        public string? Adresse { get; set; }
        public string? Photo { get; set; }
        public TypeUtilisateur TypeUtilisateur { get; set; }
        public StatutUtilisateur Statut { get; set; }
        public DateTime DateInscription { get; set; }
        public bool AbonneNewsletter { get; set; }

        // Navigation properties
        public Carte Carte { get; set; }
        public ICollection<Notification> Notifications { get; set; }
    }
}
