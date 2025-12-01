using JNBFitness.Domain.Entities;

namespace JNBFitness.Infrastructure.Repositories
{
    public interface IParrainageRepository : IRepository<Parrainage>
    {
        Task<IEnumerable<Parrainage>> GetParrainagesByParrainIdAsync(long parrainId);
        Task<int> GetNombreParrainagesValidesAsync(long parrainId);
        Task<IEnumerable<Parrainage>> GetParrainagesByFilleulIdAsync(long filleulId);
        Task<Parrainage> GetParrainageWithDetailsAsync(long id);
        Task<bool> ExistsParrainageAsync(long parrainId, long filleulId);
    }
}
