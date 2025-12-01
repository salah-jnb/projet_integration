using System.Security.Cryptography.Xml;

namespace JNBFitness.Domain.Entities
{
    public class Carte
    {
        public long Id { get; set; }
        public long UtilisateurId { get; set; }
        public required string Numero { get; set; }
        public required string Libelle { get; set; }
        public required string Devise { get; set; }
        public long SoldeCent { get; set; }
        public bool Active { get; set; }
        public DateTime DateCreation { get; set; }
        public DateTime DateMiseAJour { get; set; }

        // Navigation properties
        public Utilisateur Utilisateur { get; set; }
        public ICollection<Transfert> TransfertsEmis { get; set; }
        public ICollection<Transfert> TransfertsRecus { get; set; }
        public ICollection<EcritureLedger> Ecritures { get; set; }
    }
}
