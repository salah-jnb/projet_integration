using JNBFitness.Domain.Entities;

namespace JNBFitness.Infrastructure.Repositories
{
    public interface IReservationCoursCollectifRepository : IRepository<ReservationCoursCollectif>
    {
        Task<IEnumerable<ReservationCoursCollectif>> GetReservationsByClientIdAsync(long clientId);
        Task<IEnumerable<ReservationCoursCollectif>> GetReservationsBySeanceIdAsync(long seanceId);
    }
}
