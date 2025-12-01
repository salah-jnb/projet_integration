using JNBFitness.Application.DTOs.Abonnement;

namespace JNBFitness.Application.Services.Abonnement
{
    public interface ITypeAbonnementConfigService
    {
        Task<IEnumerable<TypeAbonnementDto>> GetAllTypesAsync();
        Task<IEnumerable<TypeAbonnementDto>> GetAllTypesActifsAsync();
        Task<TypeAbonnementDto> GetTypeByIdAsync(long id);
        Task<TypeAbonnementDto> CreateTypeAsync(CreateTypeAbonnementConfigDto createDto);
        Task<TypeAbonnementDto> UpdateTypeAsync(long id, UpdateTypeAbonnementConfigDto updateDto);
        Task<bool> DeleteTypeAsync(long id);
    }
}