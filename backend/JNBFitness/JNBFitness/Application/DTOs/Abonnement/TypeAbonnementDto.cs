namespace JNBFitness.Application.DTOs.Abonnement
{
    public class TypeAbonnementDto
    {
        public long Id { get; set; }
        public required string Type { get; set; }
        public required string Nom { get; set; }
        public required string Description { get; set; }
        public int? DureeEnMois { get; set; }
        public int? NombreSeances { get; set; }
        public decimal Prix { get; set; }
        public bool Actif { get; set; }
    }
}
