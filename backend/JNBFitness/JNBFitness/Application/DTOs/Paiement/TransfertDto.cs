namespace JNBFitness.Application.DTOs.Paiement
{
    public class TransfertDto
    {
        public long Id { get; set; }
        public string Reference { get; set; }
        public long EmetteurCarteId { get; set; }
        public long RecepteurCarteId { get; set; }
        public long MontantCent { get; set; }
        public decimal MontantEuro => MontantCent / 100.0m;
        public string Devise { get; set; }
        public string Motif { get; set; }
        public DateTime DateTransfert { get; set; }
        public string Statut { get; set; }
    }
}
