using JNBFitness.Application.DTOs.Reservation;

namespace JNBFitness.Application.Services.Reservation
{
    public interface IReservationCoachingService
    {
        Task<ReservationCoachingDto> CreateReservationAsync(CreateReservationCoachingDto createDto);
        Task<IEnumerable<ReservationCoachingDto>> GetReservationsByClientIdAsync(long clientId);
        Task<IEnumerable<ReservationCoachingDto>> GetReservationsByCoachIdAsync(long coachId);
        Task<ReservationCoachingDto> GetReservationByIdAsync(long reservationId);
        Task<bool> CancelReservationAsync(long reservationId);
        Task<bool> CompleteReservationAsync(long reservationId);
        Task<bool> ConfirmReservationAsync(long reservationId);
        Task<bool> IsTimeSlotAvailableAsync(long coachId, DateTime dateSeance, int dureeMinutes);
    }
}
