using JNBFitness.Application.DTOs.Parrainage;

namespace JNBFitness.Application.Services.Parrainage
{
    public interface IParrainageService
    {
        Task<IEnumerable<ParrainageDto>> GetParrainagesByParrainIdAsync(long parrainId);
        Task<int> GetNombreParrainagesValidesAsync(long parrainId);
        Task<IEnumerable<ParrainageDto>> GetParrainagesByFilleulIdAsync(long filleulId);
        Task<ParrainageDto> GetParrainageByIdAsync(long id);
        Task<bool> ValiderParrainageAsync(long parrainageId);
        Task<string?> GetCodeParrainageByClientIdAsync(long clientId);
        Task<bool> CreateParrainageAsync(CreateParrainageDto createDto);
    }
}
