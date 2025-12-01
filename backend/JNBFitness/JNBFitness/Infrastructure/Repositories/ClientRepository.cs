using JNBFitness.Domain.Entities;
using JNBFitness.Domain.Enums;
using JNBFitness.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace JNBFitness.Infrastructure.Repositories
{
    public class ClientRepository : Repository<Client>, IClientRepository
    {
        public ClientRepository(ApplicationDbContext context) : base(context)
        {
        }

        public async Task<Client> GetByUtilisateurIdAsync(long utilisateurId)
        {
            return await _dbSet
                .Include(c => c.Utilisateur)
                .FirstOrDefaultAsync(c => c.UtilisateurId == utilisateurId);
        }

        public async Task<Client> GetByCodeParrainageAsync(string codeParrainage)
        {
            return await _dbSet
                .Include(c => c.Utilisateur)
                .FirstOrDefaultAsync(c => c.CodeParrainage == codeParrainage);
        }

        public async Task<IEnumerable<Client>> GetClientsActifsAsync()
        {
            return await _dbSet
                .Include(c => c.Utilisateur)
                .Where(c => c.Utilisateur.Statut == StatutUtilisateur.ACTIF)
                .ToListAsync();
        }
    }
}
