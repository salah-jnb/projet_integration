using JNBFitness.Domain.Entities;
using JNBFitness.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace JNBFitness.Infrastructure.Repositories
{
    public class TransfertRepository : Repository<Transfert>, ITransfertRepository
    {
        public TransfertRepository(ApplicationDbContext context) : base(context)
        {
        }

        public async Task<IEnumerable<Transfert>> GetTransfertsByCarteIdAsync(long carteId)
        {
            return await _dbSet
                .Include(t => t.CarteEmetteur)
                .Include(t => t.CarteRecepteur)
                .Where(t => t.EmetteurCarteId == carteId || t.RecepteurCarteId == carteId)
                .OrderByDescending(t => t.DateTransfert)
                .ToListAsync();
        }

        public async Task<Transfert> GetByReferenceAsync(string reference)
        {
            return await _dbSet
                .Include(t => t.CarteEmetteur)
                .Include(t => t.CarteRecepteur)
                .FirstOrDefaultAsync(t => t.Reference == reference);
        }
    }
}
