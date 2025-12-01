using System.ComponentModel.DataAnnotations;

namespace JNBFitness.Application.DTOs.Notation
{
    public class CreateNotationCoachDto
    {
        [Required]
        public long ClientId { get; set; }
        [Required]
        public long CoachId { get; set; }
        [Required]
        public long ReservationCoachingId { get; set; }
        [Required]
        [Range(1,5)]
        public int Note { get; set; }
        public string? Commentaire { get; set; }
    }
}