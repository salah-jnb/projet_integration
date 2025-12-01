namespace JNBFitness.Application.DTOs.Notification
{
    public class NotificationDto
    {
        public long Id { get; set; }
        public long DestinataireId { get; set; }
        public string Titre { get; set; }
        public string Message { get; set; }
        public string Type { get; set; }
        public DateTime DateEnvoi { get; set; }
        public bool Lue { get; set; }
        public bool EmailEnvoye { get; set; }
    }
}
