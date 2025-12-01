using JNBFitness.Application.DTOs.Utilisateur;

namespace JNBFitness.Application.Services.Utilisateur
{
    public interface ICoachService
    {
        Task<CoachDto> GetByIdAsync(long utilisateurId);
        Task<CoachDetailsDto> GetCoachWithDetailsAsync(long utilisateurId);
        Task<IEnumerable<CoachDto>> GetAllCoachsAsync();
        Task<CoachDto> UpdateCoachProfileAsync(long utilisateurId, string? specialites, string? description);
    }
}
