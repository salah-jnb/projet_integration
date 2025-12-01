namespace JNBFitness.Application.DTOs.Parrainage
{
    public class ParrainageDto
    {
        public long Id { get; set; }
        public long ParrainId { get; set; }
        public long FilleulId { get; set; }
        public required string FilleulNom { get; set; }
        public required string FilleulPrenom { get; set; }
        public required string FilleulEmail { get; set; }
        public DateTime DateInscriptionFilleul { get; set; }
        public bool Valide { get; set; }
        public DateTime? DateValidation { get; set; }
        public bool MoisGratuitAttribue { get; set; }
    }
}
