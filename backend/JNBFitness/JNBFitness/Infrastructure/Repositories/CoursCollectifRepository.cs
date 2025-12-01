using JNBFitness.Domain.Entities;
using JNBFitness.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace JNBFitness.Infrastructure.Repositories
{
    public class CoursCollectifRepository : Repository<CoursCollectif>, ICoursCollectifRepository
    {
        public CoursCollectifRepository(ApplicationDbContext context) : base(context)
        {
        }

        public async Task<IEnumerable<CoursCollectif>> GetCoursActifsAsync()
        {
            return await _dbSet
                .Include(c => c.Coach)
                .ThenInclude(c => c.Utilisateur)
                .Where(c => c.Actif)
                .ToListAsync();
        }

        public async Task<CoursCollectif> GetCoursWithSeancesAsync(long coursId)
        {
            return await _dbSet
                .Include(c => c.Coach)
                .ThenInclude(co => co.Utilisateur)
                .Include(c => c.Seances)
                .ThenInclude(s => s.Reservations)
                .FirstOrDefaultAsync(c => c.Id == coursId);
        }
    }
}
