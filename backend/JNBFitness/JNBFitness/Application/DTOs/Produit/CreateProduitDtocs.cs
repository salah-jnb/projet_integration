using System.ComponentModel.DataAnnotations;

namespace JNBFitness.Application.DTOs.Produit
{
    public class CreateProduitDto
    {
        [Required]
        public string Nom { get; set; }

        public string Description { get; set; }

        [Required]
        [Range(typeof(decimal), "0", "79228162514264337593543950335", ErrorMessage = "Le prix doit être un nombre positif")]
        public decimal Prix { get; set; }

        public string Categorie { get; set; }
        public string? ImageUrl { get; set; }
       
    }
}
