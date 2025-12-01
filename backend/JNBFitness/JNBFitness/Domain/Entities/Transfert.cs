using JNBFitness.Domain.Enums;

namespace JNBFitness.Domain.Entities
{
    public class Transfert
    {
        public long Id { get; set; }
        public string Reference { get; set; }
        public long EmetteurCarteId { get; set; }
        public long RecepteurCarteId { get; set; }
        public long MontantCent { get; set; }
        public string Devise { get; set; }
        public string Motif { get; set; }
        public DateTime DateTransfert { get; set; }
        public StatutTransfert Statut { get; set; }

        // Navigation properties
        public Carte CarteEmetteur { get; set; }
        public Carte CarteRecepteur { get; set; }
        public ICollection<EcritureLedger> Ecritures { get; set; }
        public Paiement Paiement { get; set; }
    }
}
