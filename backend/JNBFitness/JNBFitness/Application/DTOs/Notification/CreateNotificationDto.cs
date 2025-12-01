using System.ComponentModel.DataAnnotations;

namespace JNBFitness.Application.DTOs.Notification
{
    public class CreateNotificationDto
    {
        [Required]
        public long DestinataireId { get; set; }

        [Required]
        public string Titre { get; set; }

        [Required]
        public string Message { get; set; }

        [Required]
        public string Type { get; set; }
    }
}
