using JNBFitness.Domain.Entities;

namespace JNBFitness.Infrastructure.Repositories
{
    public interface IReservationCoachingRepository : IRepository<ReservationCoaching>
    {
        Task<IEnumerable<ReservationCoaching>> GetReservationsByClientIdAsync(long clientId);
        Task<IEnumerable<ReservationCoaching>> GetReservationsByCoachIdAsync(long coachId);
        Task<ReservationCoaching> GetReservationWithDetailsAsync(long reservationId);
    }
}
