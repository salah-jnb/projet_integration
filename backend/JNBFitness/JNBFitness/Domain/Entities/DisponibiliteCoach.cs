namespace JNBFitness.Domain.Entities
{
    public class DisponibiliteCoach
    {
        public long Id { get; set; }
        public long CoachId { get; set; }
        public string JourSemaine { get; set; }
        public TimeSpan HeureDebut { get; set; }
        public TimeSpan HeureFin { get; set; }
        public bool Actif { get; set; }

        // Navigation property
        public Coach Coach { get; set; }

    }
}