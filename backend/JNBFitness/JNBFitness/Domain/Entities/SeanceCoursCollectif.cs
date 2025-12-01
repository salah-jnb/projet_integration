namespace JNBFitness.Domain.Entities
{
    public class SeanceCoursCollectif
    {
        public long Id { get; set; }
        public long CoursCollectifId { get; set; }
        public DateTime DateSeance { get; set; }
        public int PlacesDisponibles { get; set; }
        public bool Annulee { get; set; }

        // Navigation properties
        public CoursCollectif CoursCollectif { get; set; }
        public ICollection<ReservationCoursCollectif> Reservations { get; set; }
    }
}
