using System.ComponentModel.DataAnnotations;

namespace JNBFitness.Application.DTOs.Abonnement
{
    public class CreateAbonnementDto
    {
        [Required]
        public long ClientId { get; set; }

        [Required]
        public long TypeAbonnementId { get; set; }

        public DateTime DateDebut { get; set; } = DateTime.Now;
    }
}
