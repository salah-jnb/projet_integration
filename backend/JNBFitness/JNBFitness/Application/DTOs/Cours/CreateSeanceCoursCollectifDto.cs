using System;
using System.ComponentModel.DataAnnotations;

namespace JNBFitness.Application.DTOs.Cours
{
    public class CreateSeanceCoursCollectifDto
    {
        [Required]
        public long CoursCollectifId { get; set; }

        [Required]
        public DateTime DateSeance { get; set; }

        [Range(0, int.MaxValue)]
        public int PlacesDisponibles { get; set; } = 0;
    }
}