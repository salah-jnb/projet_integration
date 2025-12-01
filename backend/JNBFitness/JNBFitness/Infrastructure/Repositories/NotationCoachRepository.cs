using JNBFitness.Domain.Entities;
using JNBFitness.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace JNBFitness.Infrastructure.Repositories
{
    public class NotationCoachRepository : Repository<NotationCoach>, INotationCoachRepository
    {
        public NotationCoachRepository(ApplicationDbContext context) : base(context)
        {
        }

        public async Task<IEnumerable<NotationCoach>> GetByCoachIdAsync(long coachId)
        {
            return await _dbSet
                .Include(n => n.Client)
                .ThenInclude(c => c.Utilisateur)
                .Where(n => n.CoachId == coachId)
                .OrderByDescending(n => n.DateNotation)
                .ToListAsync();
        }

        public async Task<NotationCoach?> GetByReservationIdAsync(long reservationId)
        {
            return await _dbSet.FirstOrDefaultAsync(n => n.ReservationCoachingId == reservationId);
        }
    }
}