using JNBFitness.Domain.Enums;

namespace JNBFitness.Domain.Entities
{
    public class ReservationCoaching
    {
        public long Id { get; set; }
        public long ClientId { get; set; }
        public long CoachId { get; set; }
        public DateTime DateSeance { get; set; }
        public int? DureeMinutes { get; set; }
        public TypeSeance TypeSeance { get; set; }
        public StatutReservation Statut { get; set; }
        public DateTime DateReservation { get; set; }
        public decimal Montant { get; set; }

        // Navigation properties
        public Client Client { get; set; }
        public Coach Coach { get; set; }
        public NotationCoach Notation { get; set; }
    }
}
