using JNBFitness.Domain.Entities;
using JNBFitness.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace JNBFitness.Infrastructure.Repositories
{
    public class ParrainageRepository : Repository<Parrainage>, IParrainageRepository
    {
        public ParrainageRepository(ApplicationDbContext context) : base(context)
        {
        }

        public async Task<IEnumerable<Parrainage>> GetParrainagesByParrainIdAsync(long parrainId)
        {
            return await _dbSet
                .Include(p => p.Filleul)
                .ThenInclude(f => f.Utilisateur)
                .Where(p => p.ParrainId == parrainId)
                .ToListAsync();
        }

        public async Task<int> GetNombreParrainagesValidesAsync(long parrainId)
        {
            return await _dbSet
                .Where(p => p.ParrainId == parrainId && p.Valide)
                .CountAsync();
        }

        public async Task<IEnumerable<Parrainage>> GetParrainagesByFilleulIdAsync(long filleulId)
        {
            return await _dbSet
                .Include(p => p.Parrain)
                .ThenInclude(p => p.Utilisateur)
                .Where(p => p.FilleulId == filleulId)
                .ToListAsync();
        }

        public async Task<Parrainage> GetParrainageWithDetailsAsync(long id)
        {
            return await _dbSet
                .Include(p => p.Parrain)
                .ThenInclude(p => p.Utilisateur)
                .Include(p => p.Filleul)
                .ThenInclude(f => f.Utilisateur)
                .FirstOrDefaultAsync(p => p.Id == id);
        }

        public async Task<bool> ExistsParrainageAsync(long parrainId, long filleulId)
        {
            return await _dbSet
                .AnyAsync(p => p.ParrainId == parrainId && p.FilleulId == filleulId);
        }
    }
}
