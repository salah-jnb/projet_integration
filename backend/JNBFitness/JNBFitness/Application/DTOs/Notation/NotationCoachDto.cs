namespace JNBFitness.Application.DTOs.Notation
{
    public class NotationCoachDto
    {
        public long Id { get; set; }
        public long ClientId { get; set; }
        public long CoachId { get; set; }
        public long ReservationCoachingId { get; set; }
        public int Note { get; set; }
        public string Commentaire { get; set; }
        public DateTime DateNotation { get; set; }
        public string ClientNom { get; set; }
        public string ClientPrenom { get; set; }
    }
}