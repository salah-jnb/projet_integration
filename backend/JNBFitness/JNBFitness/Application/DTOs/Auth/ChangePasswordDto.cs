using System.ComponentModel.DataAnnotations;

namespace JNBFitness.Application.DTOs.Auth
{
    public class ChangePasswordDto
    {
        [Required(ErrorMessage = "L'ancien mot de passe est obligatoire")]
        public string AncienMotDePasse { get; set; }

        [Required(ErrorMessage = "Le nouveau mot de passe est obligatoire")]
        [MinLength(6, ErrorMessage = "Le mot de passe doit contenir au moins 6 caractères")]
        public string NouveauMotDePasse { get; set; }
    }
}
