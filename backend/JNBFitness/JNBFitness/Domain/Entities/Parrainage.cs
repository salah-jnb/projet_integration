namespace JNBFitness.Domain.Entities
{
    public class Parrainage
    {
        public long Id { get; set; }
        public long ParrainId { get; set; }
        public long FilleulId { get; set; }
        public DateTime DateInscriptionFilleul { get; set; }
        public bool Valide { get; set; }
        public DateTime? DateValidation { get; set; }
        public bool MoisGratuitAttribue { get; set; }

        // Navigation properties
        public Client Parrain { get; set; }
        public Client Filleul { get; set; }
    }
}
