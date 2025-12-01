namespace JNBFitness.Application.DTOs.Reservation
{
    public class ReservationCoachingDto
    {
        public long Id { get; set; }
        public long ClientId { get; set; }
        public long CoachId { get; set; }
        public string CoachNom { get; set; }
        public string CoachPrenom { get; set; }
        public DateTime DateSeance { get; set; }
        public int? DureeMinutes { get; set; }
        public string TypeSeance { get; set; }
        public string Statut { get; set; }
        public DateTime DateReservation { get; set; }
        public decimal Montant { get; set; }
    }
}
