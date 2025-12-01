using System.ComponentModel.DataAnnotations;

namespace JNBFitness.Application.DTOs.Article
{
    public class CreateArticleDto
    {
        [Required]
        public long CoachId { get; set; }

        [Required]
        [MaxLength(200)]
        public string Titre { get; set; }

        [Required]
        public string Contenu { get; set; }

        public string ImageUrl { get; set; }
        public string? Statut { get; set; }
    }
}
