using System.ComponentModel.DataAnnotations;
using JNBFitness.Domain.Enums;

namespace JNBFitness.Application.DTOs.Utilisateur
{
    public class CreateUserDto
    {
        [Required(ErrorMessage = "L'email est obligatoire")]
        [EmailAddress(ErrorMessage = "Format email invalide")]
        public string Email { get; set; }

        [Required(ErrorMessage = "Le mot de passe est obligatoire")]
        [MinLength(6, ErrorMessage = "Le mot de passe doit contenir au moins 6 caractères")]
        public string MotDePasse { get; set; }

        [Required(ErrorMessage = "Le nom est obligatoire")]
        public string Nom { get; set; }

        [Required(ErrorMessage = "Le prénom est obligatoire")]
        public string Prenom { get; set; }

        public string Telephone { get; set; }
        public string Adresse { get; set; }
        public string Photo { get; set; }

        [Required(ErrorMessage = "Le type d'utilisateur est obligatoire")]
        public TypeUtilisateur TypeUtilisateur { get; set; }

        public string CodeParrainage { get; set; }

        // Champs spécifiques au coach
        public string Specialites { get; set; }
        public string Description { get; set; }
    }
}