using JNBFitness.Domain.Entities;
using JNBFitness.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace JNBFitness.Infrastructure.Repositories
{
    public class PaiementRepository : Repository<Paiement>, IPaiementRepository
    {
        public PaiementRepository(ApplicationDbContext context) : base(context)
        {
        }

        public async Task<IEnumerable<Paiement>> GetPaiementsByClientIdAsync(long clientId)
        {
            return await _dbSet
                .Include(p => p.Abonnement)
                .ThenInclude(a => a.TypeAbonnement)
                .Where(p => p.ClientId == clientId)
                .OrderByDescending(p => p.DatePaiement)
                .ToListAsync();
        }

        public async Task<Paiement> GetPaiementWithDetailsAsync(long paiementId)
        {
            return await _dbSet
                .Include(p => p.Client)
                .ThenInclude(c => c.Utilisateur)
                .Include(p => p.Abonnement)
                .Include(p => p.Transfert)
                .FirstOrDefaultAsync(p => p.Id == paiementId);
        }

        public async Task<IEnumerable<Paiement>> GetAllWithAbonnementAsync()
        {
            return await _dbSet
                .Include(p => p.Abonnement)
                .ThenInclude(a => a.TypeAbonnement)
                .OrderByDescending(p => p.DatePaiement)
                .ToListAsync();
        }
    }
}
