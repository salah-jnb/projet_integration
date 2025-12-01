using System.ComponentModel.DataAnnotations;

namespace JNBFitness.Application.DTOs.Reservation
{
    public class CreateReservationCoursDto
    {
        [Required]
        public long ClientId { get; set; }

        [Required]
        public long SeanceCoursCollectifId { get; set; }

        public int DelaiAnnulationHeures { get; set; } = 24;
    }
}
