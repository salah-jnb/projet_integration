using System.ComponentModel.DataAnnotations;

namespace JNBFitness.Application.DTOs.Cours
{
    public class CreateCoursCollectifDto
    {
        [Required]
        public string Nom { get; set; }

        public string Description { get; set; }

        [Required]
        public long CoachId { get; set; }

        [Required]
        public string JourSemaine { get; set; }

        [Required]
        public TimeSpan HeureDebut { get; set; }

        [Required]
        public TimeSpan HeureFin { get; set; }

        [Required]
        public int CapaciteMax { get; set; }
    }
}
