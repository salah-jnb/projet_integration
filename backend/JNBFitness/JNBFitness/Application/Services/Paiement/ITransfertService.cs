using JNBFitness.Application.DTOs.Paiement;

namespace JNBFitness.Application.Services.Paiement
{
    public interface ITransfertService
    {
        Task<TransfertDto> CreateTransfertAsync(CreateTransfertDto createDto);
        Task<IEnumerable<TransfertDto>> GetTransfertsByCarteIdAsync(long carteId);
    }
}
