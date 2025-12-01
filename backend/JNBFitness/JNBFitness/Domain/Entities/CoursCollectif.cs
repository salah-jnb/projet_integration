namespace JNBFitness.Domain.Entities
{
    public class CoursCollectif
    {
        public long Id { get; set; }
        public string Nom { get; set; }
        public string Description { get; set; }
        public long CoachId { get; set; }
        public string JourSemaine { get; set; }
        public TimeSpan HeureDebut { get; set; }
        public TimeSpan HeureFin { get; set; }
        public int CapaciteMax { get; set; }
        public bool Actif { get; set; }

        // Navigation properties
        public Coach Coach { get; set; }
        public ICollection<SeanceCoursCollectif> Seances { get; set; }
    }
}
