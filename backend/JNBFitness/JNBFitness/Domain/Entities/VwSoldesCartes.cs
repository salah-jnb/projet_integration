namespace JNBFitness.Domain.Entities
{
    public class VwSoldesCartes
    {
        public long CarteId { get; set; }
        public long UtilisateurId { get; set; }
        public required string Numero { get; set; }
        public long SoldeCent { get; set; }
        public long TotalCredits { get; set; }
        public long TotalDebits { get; set; }
        public long Ecart { get; set; }
    }
}
