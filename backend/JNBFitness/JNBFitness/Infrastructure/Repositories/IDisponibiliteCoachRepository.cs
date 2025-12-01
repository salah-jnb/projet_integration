using JNBFitness.Domain.Entities;

namespace JNBFitness.Infrastructure.Repositories
{
    public interface IDisponibiliteCoachRepository : IRepository<DisponibiliteCoach>
    {
        Task<IEnumerable<DisponibiliteCoach>> GetByCoachIdAsync(long coachId);
    }
}


