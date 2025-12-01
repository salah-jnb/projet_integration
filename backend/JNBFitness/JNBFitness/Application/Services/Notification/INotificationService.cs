using JNBFitness.Application.DTOs.Notification;

namespace JNBFitness.Application.Services.Notification
{
    public interface INotificationService
    {
        Task<NotificationDto> CreateNotificationAsync(CreateNotificationDto createDto);
        Task<IEnumerable<NotificationDto>> GetNotificationsByUtilisateurIdAsync(long utilisateurId);
        Task<IEnumerable<NotificationDto>> GetNotificationsNonLuesAsync(long utilisateurId);
        Task<bool> MarquerCommeLueAsync(long notificationId);
    }
}
