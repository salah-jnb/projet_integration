using JNBFitness.Application.DTOs.Abonnement;

namespace JNBFitness.Application.Services.Abonnement
{
    public interface IAbonnementService
    {
        Task<AbonnementDto> CreateAbonnementAsync(CreateAbonnementDto createDto);
        Task<IEnumerable<AbonnementDto>> GetAbonnementsByClientIdAsync(long clientId);
        Task<IEnumerable<AbonnementDto>> GetAbonnementsActifsAsync(long clientId);
        Task<AbonnementDetailsDto> GetAbonnementDetailsAsync(long abonnementId);
        Task<bool> CancelAbonnementAsync(long abonnementId);
        Task<IEnumerable<AbonnementDto>> GetAllAsync();
        Task<IEnumerable<AbonnementDto>> GetAllActifsAsync();
    }
}
