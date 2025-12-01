using JNBFitness.Domain.Enums;

namespace JNBFitness.Domain.Entities
{
    public class ReservationCoursCollectif
    {
        public long Id { get; set; }
        public long ClientId { get; set; }
        public long SeanceCoursCollectifId { get; set; }
        public StatutReservation Statut { get; set; }
        public DateTime DateReservation { get; set; }
        public int DelaiAnnulationHeures { get; set; }

        // Navigation properties
        public Client Client { get; set; }
        public SeanceCoursCollectif SeanceCoursCollectif { get; set; }
    }
}
