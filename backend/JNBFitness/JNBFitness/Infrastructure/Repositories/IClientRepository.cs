using JNBFitness.Domain.Entities;

namespace JNBFitness.Infrastructure.Repositories
{
    public interface IClientRepository : IRepository<Client>
    {
        Task<Client> GetByUtilisateurIdAsync(long utilisateurId);
        Task<Client> GetByCodeParrainageAsync(string codeParrainage);
        Task<IEnumerable<Client>> GetClientsActifsAsync();
    }
}
