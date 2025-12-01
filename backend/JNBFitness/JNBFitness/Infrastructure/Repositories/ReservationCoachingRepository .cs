using JNBFitness.Domain.Entities;
using JNBFitness.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace JNBFitness.Infrastructure.Repositories
{
    public class ReservationCoachingRepository : Repository<ReservationCoaching>, IReservationCoachingRepository
    {
        public ReservationCoachingRepository(ApplicationDbContext context) : base(context)
        {
        }

        public async Task<IEnumerable<ReservationCoaching>> GetReservationsByClientIdAsync(long clientId)
        {
            return await _dbSet
                .Include(r => r.Coach)
                .ThenInclude(c => c.Utilisateur)
                .Where(r => r.ClientId == clientId)
                .OrderByDescending(r => r.DateSeance)
                .ToListAsync();
        }

        public async Task<IEnumerable<ReservationCoaching>> GetReservationsByCoachIdAsync(long coachId)
        {
            return await _dbSet
                .Include(r => r.Client)
                .ThenInclude(c => c.Utilisateur)
                .Where(r => r.CoachId == coachId)
                .OrderByDescending(r => r.DateSeance)
                .ToListAsync();
        }

        public async Task<ReservationCoaching> GetReservationWithDetailsAsync(long reservationId)
        {
            return await _dbSet
                .Include(r => r.Client)
                .ThenInclude(c => c.Utilisateur)
                .Include(r => r.Coach)
                .ThenInclude(c => c.Utilisateur)
                .Include(r => r.Notation)
                .FirstOrDefaultAsync(r => r.Id == reservationId);
        }
    }
}
