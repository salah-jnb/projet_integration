namespace JNBFitness.Application.DTOs.Abonnement
{
    public class AbonnementDetailsDto
    {
        public long Id { get; set; }
        public long ClientId { get; set; }
        public string ClientNom { get; set; }
        public string ClientPrenom { get; set; }
        public TypeAbonnementDto TypeAbonnement { get; set; }
        public DateTime DateDebut { get; set; }
        public DateTime? DateFin { get; set; }
        public string Statut { get; set; }
        public int? SeancesRestantes { get; set; }
        public bool OffertParParrainage { get; set; }
    }
}
