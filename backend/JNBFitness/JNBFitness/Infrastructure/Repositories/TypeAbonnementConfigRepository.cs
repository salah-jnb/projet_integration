using JNBFitness.Domain.Entities;
using JNBFitness.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace JNBFitness.Infrastructure.Repositories
{
    public class TypeAbonnementConfigRepository : Repository<TypeAbonnementConfig>, ITypeAbonnementConfigRepository
    {
        public TypeAbonnementConfigRepository(ApplicationDbContext context) : base(context)
        {
        }

        public async Task<IEnumerable<TypeAbonnementConfig>> GetAllActifsAsync()
        {
            return await _dbSet.Where(t => t.Actif).ToListAsync();
        }
    }
}