using JNBFitness.Domain.Entities;

namespace JNBFitness.Infrastructure.Repositories
{
    public interface ITypeAbonnementConfigRepository : IRepository<TypeAbonnementConfig>
    {
        Task<IEnumerable<TypeAbonnementConfig>> GetAllActifsAsync();
    }
}