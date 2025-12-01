namespace JNBFitness.Application.DTOs.Cours
{
    public class CoursCollectifDto
    {
        public long Id { get; set; }
        public string Nom { get; set; }
        public string Description { get; set; }
        public long CoachId { get; set; }
        public string CoachNom { get; set; }
        public string CoachPrenom { get; set; }
        public string JourSemaine { get; set; }
        public TimeSpan HeureDebut { get; set; }
        public TimeSpan HeureFin { get; set; }
        public int CapaciteMax { get; set; }
        public bool Actif { get; set; }
    }
}
