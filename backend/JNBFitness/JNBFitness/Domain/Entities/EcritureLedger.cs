using JNBFitness.Domain.Enums;

namespace JNBFitness.Domain.Entities
{
    public class EcritureLedger
    {
        public long Id { get; set; }
        public long TransfertId { get; set; }
        public long CarteId { get; set; }
        public SensEcriture Sens { get; set; }
        public long MontantCent { get; set; }
        public DateTime DateEcriture { get; set; }

        // Navigation properties
        public Transfert Transfert { get; set; }
        public Carte Carte { get; set; }
    }
}
