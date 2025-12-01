namespace JNBFitness.Application.DTOs.Cours
{
    public class SeanceCoursCollectifDto
    {
        public long Id { get; set; }
        public long CoursCollectifId { get; set; }
        public string CoursNom { get; set; }
        public DateTime DateSeance { get; set; }
        public int PlacesDisponibles { get; set; }
        public bool Annulee { get; set; }
    }
}
