using JNBFitness.Domain.Entities;

namespace JNBFitness.Infrastructure.Repositories
{
    public interface ICoachRepository : IRepository<Coach>
    {
        Task<Coach> GetByUtilisateurIdAsync(long utilisateurId);
        Task<IEnumerable<Coach>> GetCoachsWithDisponibilitesAsync();
        Task<Coach> GetCoachWithDetailsAsync(long coachId);
    }
}
