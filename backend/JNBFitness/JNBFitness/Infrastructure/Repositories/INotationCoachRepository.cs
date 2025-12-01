using JNBFitness.Domain.Entities;

namespace JNBFitness.Infrastructure.Repositories
{
    public interface INotationCoachRepository : IRepository<NotationCoach>
    {
        Task<IEnumerable<NotationCoach>> GetByCoachIdAsync(long coachId);
        Task<NotationCoach?> GetByReservationIdAsync(long reservationId);
    }
}