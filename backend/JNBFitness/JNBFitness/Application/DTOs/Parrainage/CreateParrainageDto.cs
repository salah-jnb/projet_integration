using System.ComponentModel.DataAnnotations;

namespace JNBFitness.Application.DTOs.Parrainage
{
    public class CreateParrainageDto
    {
        [Required]
        public long ParrainId { get; set; }

        [Required]
        public long FilleulId { get; set; }
    }
}
