using JNBFitness.Domain.Entities;
using JNBFitness.Domain.Enums;
using JNBFitness.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace JNBFitness.Infrastructure.Repositories
{
    public class AbonnementRepository : Repository<AbonnementClient>, IAbonnementRepository
    {
        public AbonnementRepository(ApplicationDbContext context) : base(context)
        {
        }

        public async Task<IEnumerable<AbonnementClient>> GetAbonnementsByClientIdAsync(long clientId)
        {
            return await _dbSet
                .Include(a => a.TypeAbonnement)
                .Where(a => a.ClientId == clientId)
                .ToListAsync();
        }

        public async Task<IEnumerable<AbonnementClient>> GetAbonnementsActifsAsync(long clientId)
        {
            return await _dbSet
                .Include(a => a.TypeAbonnement)
                .Where(a => a.ClientId == clientId && a.Statut == StatutAbonnement.ACTIF)
                .ToListAsync();
        }

        public async Task<AbonnementClient> GetAbonnementWithDetailsAsync(long abonnementId)
        {
            return await _dbSet
                .Include(a => a.Client)
                .Include(a => a.TypeAbonnement)
                .Include(a => a.Paiements)
                .FirstOrDefaultAsync(a => a.Id == abonnementId);
        }

        public async Task<IEnumerable<AbonnementClient>> GetAllWithTypeAsync()
        {
            return await _dbSet
                .Include(a => a.TypeAbonnement)
                .ToListAsync();
        }

        public async Task<IEnumerable<AbonnementClient>> GetAllActifsWithTypeAsync()
        {
            return await _dbSet
                .Include(a => a.TypeAbonnement)
                .Where(a => a.Statut == StatutAbonnement.ACTIF)
                .ToListAsync();
        }
    }
}
