using JNBFitness.Domain.Entities;

namespace JNBFitness.Infrastructure.Repositories
{
    public interface INotificationRepository : IRepository<Notification>
    {
        Task<IEnumerable<Notification>> GetNotificationsByUtilisateurIdAsync(long utilisateurId);
        Task<IEnumerable<Notification>> GetNotificationsNonLuesAsync(long utilisateurId);
    }
}
