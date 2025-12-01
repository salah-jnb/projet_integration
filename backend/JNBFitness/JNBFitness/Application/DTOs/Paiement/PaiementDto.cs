namespace JNBFitness.Application.DTOs.Paiement
{
    public class PaiementDto
    {
        public long Id { get; set; }
        public long ClientId { get; set; }
        public long AbonnementId { get; set; }
        public string AbonnementNom { get; set; }
        public decimal Montant { get; set; }
        public DateTime DatePaiement { get; set; }
        public string MethodePaiement { get; set; }
        public string Statut { get; set; }
    }
}
