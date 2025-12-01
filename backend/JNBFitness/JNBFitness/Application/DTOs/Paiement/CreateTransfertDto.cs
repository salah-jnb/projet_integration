using System.ComponentModel.DataAnnotations;

namespace JNBFitness.Application.DTOs.Paiement
{

    public class CreateTransfertDto
    {
        [Required]
        public long EmetteurCarteId { get; set; }

        [Required]
        public long RecepteurCarteId { get; set; }

        [Required]
        [Range(0, double.MaxValue, ErrorMessage = "Le montant doit être supérieur à 0")]
        public decimal MontantEuro { get; set; }

        [Required]
        public string Motif { get; set; }
        public string? Devise { get; set; }
    }
}
