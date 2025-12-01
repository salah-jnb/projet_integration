using JNBFitness.Domain.Enums;

namespace JNBFitness.Domain.Entities
{
    public class Article
    {
        public long Id { get; set; }
        public long CoachId { get; set; }
        public string Titre { get; set; }
        public string Contenu { get; set; }
        public string? ImageUrl { get; set; }
        public StatutArticle Statut { get; set; }
        public DateTime DateCreation { get; set; }
        public DateTime? DatePublication { get; set; }
        public DateTime? DateValidation { get; set; }
        public string? CommentaireAdmin { get; set; }

        // Navigation property
        public Coach Coach { get; set; }
    }
}
