using JNBFitness.Domain.Entities;
using JNBFitness.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace JNBFitness.Infrastructure.Repositories
{
    public class CoachRepository : Repository<Coach>, ICoachRepository
    {
        public CoachRepository(ApplicationDbContext context) : base(context)
        {
        }

        public async Task<Coach> GetByUtilisateurIdAsync(long utilisateurId)
        {
            return await _dbSet
                .Include(c => c.Utilisateur)
                .FirstOrDefaultAsync(c => c.UtilisateurId == utilisateurId);
        }

        public async Task<IEnumerable<Coach>> GetCoachsWithDisponibilitesAsync()
        {
            return await _dbSet
                .Include(c => c.Utilisateur)
                .Include(c => c.Disponibilites)
                .ToListAsync();
        }

        public async Task<Coach> GetCoachWithDetailsAsync(long coachId)
        {
            return await _dbSet
                .Include(c => c.Utilisateur)
                .Include(c => c.Disponibilites)
                .Include(c => c.Notations)
                .FirstOrDefaultAsync(c => c.UtilisateurId == coachId);
        }
    }
}
