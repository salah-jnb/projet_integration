using JNBFitness.Application.DTOs.Paiement;

namespace JNBFitness.Application.Services.Paiement
{
    public interface ICarteService
    {
        Task<CarteDto> GetCarteByUtilisateurIdAsync(long utilisateurId);
        Task<CarteDto> RechargerCarteAsync(long carteId, decimal montantEuro);
        Task<IEnumerable<CarteDto>> GetAllAsync();
        Task<CarteDto> DiminuerMontantCarteAsync(long carteId, decimal montantEuro);
        Task<CarteDto> TransfererMontantAsync(long carteSourceId, long carteDestinationId, decimal montantEuro,string Devise);
    }
}
