namespace JNBFitness.Domain.Entities
{
    public class Coach
    {
        public long UtilisateurId { get; set; }
        public string Specialites { get; set; }
        public string Description { get; set; }
        public decimal NoteGlobale { get; set; }
        public int NombreAvis { get; set; }

        // Navigation properties
        public Utilisateur Utilisateur { get; set; }
        public ICollection<DisponibiliteCoach> Disponibilites { get; set; }
        public ICollection<ReservationCoaching> ReservationsCoaching { get; set; }
        public ICollection<NotationCoach> Notations { get; set; }
        public ICollection<CoursCollectif> CoursCollectifs { get; set; }
        public ICollection<Article> Articles { get; set; }
    }

}
