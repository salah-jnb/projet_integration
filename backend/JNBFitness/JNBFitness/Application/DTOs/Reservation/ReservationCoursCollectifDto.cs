namespace JNBFitness.Application.DTOs.Reservation
{
    public class ReservationCoursCollectifDto
    {
        public long Id { get; set; }
        public long ClientId { get; set; }
        public long SeanceCoursCollectifId { get; set; }
        public string CoursNom { get; set; }
        public DateTime DateSeance { get; set; }
        public string Statut { get; set; }
        public DateTime DateReservation { get; set; }
        public int DelaiAnnulationHeures { get; set; }
    }
}
