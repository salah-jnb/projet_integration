namespace JNBFitness.Domain.Entities
{
    public class Notification
    {
        public long Id { get; set; }
        public long DestinataireId { get; set; }
        public string Titre { get; set; }
        public string Message { get; set; }
        public string Type { get; set; }
        public DateTime DateEnvoi { get; set; }
        public bool Lue { get; set; }
        public bool EmailEnvoye { get; set; }

        // Navigation property
        public Utilisateur Destinataire { get; set; }
    }
}
