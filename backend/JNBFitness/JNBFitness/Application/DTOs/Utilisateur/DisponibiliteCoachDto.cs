namespace JNBFitness.Application.DTOs.Utilisateur
{
    public class DisponibiliteCoachDto
    {
        public long Id { get; set; }
        public string JourSemaine { get; set; }
        public TimeSpan HeureDebut { get; set; }
        public TimeSpan HeureFin { get; set; }
        public bool Actif { get; set; }
    }
}
