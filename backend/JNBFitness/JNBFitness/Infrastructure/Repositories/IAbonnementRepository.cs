using JNBFitness.Domain.Entities;

namespace JNBFitness.Infrastructure.Repositories
{
    public interface IAbonnementRepository : IRepository<AbonnementClient>
    {
        Task<IEnumerable<AbonnementClient>> GetAbonnementsByClientIdAsync(long clientId);
        Task<IEnumerable<AbonnementClient>> GetAbonnementsActifsAsync(long clientId);
        Task<AbonnementClient> GetAbonnementWithDetailsAsync(long abonnementId);
        Task<IEnumerable<AbonnementClient>> GetAllWithTypeAsync();
        Task<IEnumerable<AbonnementClient>> GetAllActifsWithTypeAsync();
    }
}
