namespace JNBFitness.Domain.Entities
{
    public class NotationCoach
    {
        public long Id { get; set; }
        public long ClientId { get; set; }
        public long CoachId { get; set; }
        public long ReservationCoachingId { get; set; }
        public int Note { get; set; }
        public string Commentaire { get; set; }
        public DateTime DateNotation { get; set; }

        // Navigation properties
        public Client Client { get; set; }
        public Coach Coach { get; set; }
        public ReservationCoaching ReservationCoaching { get; set; }
    }
}
