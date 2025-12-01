using JNBFitness.Application.DTOs.Paiement;

namespace JNBFitness.Application.Services.Paiement
{
    public interface IPaiementService
    {
        Task<PaiementDto> CreatePaiementAsync(CreatePaiementDto createDto);
        Task<IEnumerable<PaiementDto>> GetPaiementsByClientIdAsync(long clientId);
        Task<IEnumerable<PaiementDto>> GetAllAsync();
    }

}
