namespace JNBFitness.Domain.Entities
{
    public class Client
    {
        public long UtilisateurId { get; set; }
        public DateTime? DateActivation { get; set; }
        public string CodeParrainage { get; set; }
        public int NombreParrainagesValides { get; set; }
        public string? ParrainePar { get; set; }

        // Navigation properties
        public Utilisateur Utilisateur { get; set; }
        public ICollection<AbonnementClient> Abonnements { get; set; }
        public ICollection<ReservationCoaching> ReservationsCoaching { get; set; }
        public ICollection<ReservationCoursCollectif> ReservationsCoursCollectifs { get; set; }
        public ICollection<NotationCoach> Notations { get; set; }
        public ICollection<Paiement> Paiements { get; set; }
        public ICollection<Parrainage> ParrainagesEffectues { get; set; }
        public ICollection<Parrainage> ParrainagesRecus { get; set; }
    }
}
