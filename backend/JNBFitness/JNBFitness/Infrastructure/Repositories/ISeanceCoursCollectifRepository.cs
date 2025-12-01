using JNBFitness.Domain.Entities;
using System.Threading.Tasks;
using System.Collections.Generic;

namespace JNBFitness.Infrastructure.Repositories
{
    public interface ISeanceCoursCollectifRepository : IRepository<SeanceCoursCollectif>
    {
        Task<IEnumerable<SeanceCoursCollectif>> GetSeancesDisponiblesAsync();
        Task<IEnumerable<SeanceCoursCollectif>> GetSeancesDisponiblesByCoachIdAsync(long coachId);
        Task<IEnumerable<SeanceCoursCollectif>> GetAllWithCoursAsync();
    }
}