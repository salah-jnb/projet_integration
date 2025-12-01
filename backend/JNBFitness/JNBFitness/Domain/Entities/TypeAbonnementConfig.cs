using JNBFitness.Domain.Enums;

namespace JNBFitness.Domain.Entities
{
    public class TypeAbonnementConfig
    {
        public long Id { get; set; }
        public TypeAbonnement Type { get; set; }
        public string Nom { get; set; }
        public string Description { get; set; }
        public int? DureeEnMois { get; set; }
        public int? NombreSeances { get; set; }
        public decimal Prix { get; set; }
        public bool Actif { get; set; }

        // Navigation properties
        public ICollection<AbonnementClient> Abonnements { get; set; }
    }
}
