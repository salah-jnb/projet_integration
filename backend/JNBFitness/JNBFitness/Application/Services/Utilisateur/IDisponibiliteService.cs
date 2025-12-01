using JNBFitness.Application.DTOs.Utilisateur;

namespace JNBFitness.Application.Services.Utilisateur
{
    public interface IDisponibiliteService
    {
        Task<IEnumerable<DisponibiliteCoachDto>> GetByCoachIdAsync(long coachId);
        Task<DisponibiliteCoachDto> CreateAsync(long coachId, DisponibiliteCoachDto dto);
        Task<DisponibiliteCoachDto> UpdateAsync(long id, DisponibiliteCoachDto dto);
        Task DeleteAsync(long id);
    }
}


