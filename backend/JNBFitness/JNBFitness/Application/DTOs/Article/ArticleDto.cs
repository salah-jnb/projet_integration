namespace JNBFitness.Application.DTOs.Article
{
    public class ArticleDto
    {
        public long Id { get; set; }
        public long CoachId { get; set; }
        public string CoachNom { get; set; }
        public string CoachPrenom { get; set; }
        public string Titre { get; set; }
        public string Contenu { get; set; }
        public string ImageUrl { get; set; }
        public string Statut { get; set; }
        public DateTime DateCreation { get; set; }
        public DateTime? DatePublication { get; set; }
    }
}
