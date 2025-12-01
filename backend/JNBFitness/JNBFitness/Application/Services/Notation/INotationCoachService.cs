using JNBFitness.Application.DTOs.Notation;

namespace JNBFitness.Application.Services.Notation
{
    public interface INotationCoachService
    {
        Task<NotationCoachDto> CreateAsync(CreateNotationCoachDto dto);
        Task<IEnumerable<NotationCoachDto>> GetByCoachIdAsync(long coachId);
        Task<NotationCoachDto?> GetByReservationIdAsync(long reservationId);
    }
}