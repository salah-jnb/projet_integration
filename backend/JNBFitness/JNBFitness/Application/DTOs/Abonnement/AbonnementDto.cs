namespace JNBFitness.Application.DTOs.Abonnement
{
    public class AbonnementDto
    {
        public long Id { get; set; }
        public long ClientId { get; set; }
        public long TypeAbonnementId { get; set; }
        public string TypeNom { get; set; }
        public DateTime DateDebut { get; set; }
        public DateTime? DateFin { get; set; }
        public string Statut { get; set; }
        public int? SeancesRestantes { get; set; }
        public bool OffertParParrainage { get; set; }
    }
}
