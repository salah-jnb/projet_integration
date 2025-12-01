using JNBFitness.Domain.Entities;
using JNBFitness.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace JNBFitness.Infrastructure.Repositories
{
    public class CarteRepository : Repository<Carte>, ICarteRepository
    {
        public CarteRepository(ApplicationDbContext context) : base(context)
        {
        }

        public async Task<Carte> GetByUtilisateurIdAsync(long utilisateurId)
        {
            return await _dbSet
                .Include(c => c.Utilisateur)
                .FirstOrDefaultAsync(c => c.UtilisateurId == utilisateurId);
        }

        public async Task<Carte> GetByNumeroAsync(string numero)
        {
            return await _dbSet.FirstOrDefaultAsync(c => c.Numero == numero);
        }
    }
}
