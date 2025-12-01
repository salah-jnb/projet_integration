using JNBFitness.Domain.Entities;
using JNBFitness.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace JNBFitness.Infrastructure.Repositories
{
    public class DisponibiliteCoachRepository : Repository<DisponibiliteCoach>, IDisponibiliteCoachRepository
    {
        public DisponibiliteCoachRepository(ApplicationDbContext context) : base(context)
        {
        }

        public async Task<IEnumerable<DisponibiliteCoach>> GetByCoachIdAsync(long coachId)
        {
            return await _dbSet.Where(d => d.CoachId == coachId).ToListAsync();
        }
    }
}


