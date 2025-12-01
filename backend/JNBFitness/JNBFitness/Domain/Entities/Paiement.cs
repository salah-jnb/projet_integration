using JNBFitness.Domain.Enums;

namespace JNBFitness.Domain.Entities
{
    public class Paiement
    {
        public long Id { get; set; }
        public long ClientId { get; set; }
        public long AbonnementId { get; set; }
        public long? TransfertId { get; set; }
        public decimal Montant { get; set; }
        public DateTime DatePaiement { get; set; }
        public string MethodePaiement { get; set; }
        public StatutPaiement Statut { get; set; }

        // Navigation properties
        public Client Client { get; set; }
        public AbonnementClient Abonnement { get; set; }
        public Transfert Transfert { get; set; }
    }
}
