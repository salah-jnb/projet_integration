using JNBFitness.Domain.Entities;
using JNBFitness.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace JNBFitness.Infrastructure.Repositories
{
    public class NotificationRepository : Repository<Notification>, INotificationRepository
    {
        public NotificationRepository(ApplicationDbContext context) : base(context)
        {
        }

        public async Task<IEnumerable<Notification>> GetNotificationsByUtilisateurIdAsync(long utilisateurId)
        {
            return await _dbSet
                .Where(n => n.DestinataireId == utilisateurId)
                .OrderByDescending(n => n.DateEnvoi)
                .ToListAsync();
        }

        public async Task<IEnumerable<Notification>> GetNotificationsNonLuesAsync(long utilisateurId)
        {
            return await _dbSet
                .Where(n => n.DestinataireId == utilisateurId && !n.Lue)
                .OrderByDescending(n => n.DateEnvoi)
                .ToListAsync();
        }
    }
}
