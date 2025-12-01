namespace JNBFitness.Domain.Entities
{
    public class VwAbonnementsActifs
    {
        public long Id { get; set; }
        public long ClientId { get; set; }
        public long TypeAbonnementId { get; set; }
        public string TypeNom { get; set; }
        public DateTime DateDebut { get; set; }
        public DateTime? DateFin { get; set; }
    }
}
