using System.ComponentModel.DataAnnotations;

namespace JNBFitness.Application.DTOs.Reservation
{
    public class CreateReservationCoachingDto
    {
        [Required]
        public long ClientId { get; set; }

        [Required]
        public long CoachId { get; set; }

        [Required]
        public DateTime DateSeance { get; set; }

        [Required]
        public string TypeSeance { get; set; }
    }
}
