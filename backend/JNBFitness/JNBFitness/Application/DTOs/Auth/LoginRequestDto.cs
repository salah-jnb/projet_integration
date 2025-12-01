using System.ComponentModel.DataAnnotations;

namespace JNBFitness.Application.DTOs.Auth
{
    public class LoginRequestDto
    {
        [Required(ErrorMessage = "L'email est obligatoire")]
        [EmailAddress(ErrorMessage = "Format email invalide")]
        public string Email { get; set; }

        [Required(ErrorMessage = "Le mot de passe est obligatoire")]
        public string MotDePasse { get; set; }
    }

}
