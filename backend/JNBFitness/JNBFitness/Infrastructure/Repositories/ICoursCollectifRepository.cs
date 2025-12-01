using JNBFitness.Domain.Entities;

namespace JNBFitness.Infrastructure.Repositories
{
    public interface ICoursCollectifRepository : IRepository<CoursCollectif>
    {
        Task<IEnumerable<CoursCollectif>> GetCoursActifsAsync();
        Task<CoursCollectif> GetCoursWithSeancesAsync(long coursId);
    }
}
