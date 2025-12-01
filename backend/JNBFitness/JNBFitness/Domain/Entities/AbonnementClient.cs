using JNBFitness.Domain.Enums;

namespace JNBFitness.Domain.Entities
{
    public class AbonnementClient
    {
        public long Id { get; set; }
        public long ClientId { get; set; }
        public long TypeAbonnementId { get; set; }
        public DateTime DateDebut { get; set; }
        public DateTime? DateFin { get; set; }
        public StatutAbonnement Statut { get; set; }
        public int? SeancesRestantes { get; set; }
        public bool OffertParParrainage { get; set; }

        // Navigation properties
        public Client Client { get; set; }
        public TypeAbonnementConfig TypeAbonnement { get; set; }
        public ICollection<Paiement> Paiements { get; set; }
    }
}
