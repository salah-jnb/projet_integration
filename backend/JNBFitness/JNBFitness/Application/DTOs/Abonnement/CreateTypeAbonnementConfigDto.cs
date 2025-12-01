using System.ComponentModel.DataAnnotations;

namespace JNBFitness.Application.DTOs.Abonnement
{
    public class CreateTypeAbonnementConfigDto
    {
        [Required]
        public string Type { get; set; }

        [Required]
        public string Nom { get; set; }

        [Required]
        public string Description { get; set; }

        public int? DureeEnMois { get; set; }
        public int? NombreSeances { get; set; }

        [Range(0, double.MaxValue)]
        public decimal Prix { get; set; }
    }
}