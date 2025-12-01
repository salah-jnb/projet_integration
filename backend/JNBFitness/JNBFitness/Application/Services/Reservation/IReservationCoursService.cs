using JNBFitness.Application.DTOs.Reservation;

namespace JNBFitness.Application.Services.Reservation
{
    public interface IReservationCoursService
    {
        Task<ReservationCoursCollectifDto> CreateReservationAsync(CreateReservationCoursDto createDto);
        Task<IEnumerable<ReservationCoursCollectifDto>> GetReservationsByClientIdAsync(long clientId);
        Task<IEnumerable<ReservationCoursCollectifDto>> GetReservationsBySeanceIdAsync(long seanceId);
        Task<bool> CancelReservationAsync(long reservationId);
    }
}
