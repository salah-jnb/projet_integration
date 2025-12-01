using System;

namespace JNBFitness.Application.DTOs.Cours
{
    public class UpdateSeanceCoursCollectifDto
    {
        public DateTime? DateSeance { get; set; }
        public int? PlacesDisponibles { get; set; }
        public bool? Annulee { get; set; }
    }
}