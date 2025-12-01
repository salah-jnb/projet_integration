namespace JNBFitness.Application.DTOs.Abonnement
{
    public class UpdateTypeAbonnementConfigDto
    {
        public string Type { get; set; }
        public string Nom { get; set; }
        public string Description { get; set; }
        public int? DureeEnMois { get; set; }
        public int? NombreSeances { get; set; }
        public decimal? Prix { get; set; }
        public bool? Actif { get; set; }
    }
}