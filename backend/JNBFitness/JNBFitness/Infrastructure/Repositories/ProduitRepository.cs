using JNBFitness.Domain.Entities;
using JNBFitness.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace JNBFitness.Infrastructure.Repositories
{
    public class ProduitRepository : Repository<Produit>, IProduitRepository
    {
        public ProduitRepository(ApplicationDbContext context) : base(context)
        {
        }

        public async Task<IEnumerable<Produit>> GetProduitsActifsAsync()
        {
            return await _dbSet
                .Where(p => p.Actif)
                .ToListAsync();
        }

        public async Task<IEnumerable<Produit>> GetProduitsByCategorieAsync(string categorie)
        {
            return await _dbSet
                .Where(p => p.Categorie == categorie && p.Actif)
                .ToListAsync();
        }
    }
}
