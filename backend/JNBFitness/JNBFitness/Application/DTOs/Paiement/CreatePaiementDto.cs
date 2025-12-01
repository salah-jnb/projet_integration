using System.ComponentModel.DataAnnotations;

namespace JNBFitness.Application.DTOs.Paiement
{
    public class CreatePaiementDto
    {
        [Required]
        public long ClientId { get; set; }

        [Required]
        public long AbonnementId { get; set; }

        [Required]
        public decimal Montant { get; set; }

        public string MethodePaiement { get; set; } = "CARTE_VIRTUELLE";
    }
}
