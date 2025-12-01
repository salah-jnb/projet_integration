using JNBFitness.Application.DTOs.Utilisateur;

namespace JNBFitness.Application.Services.Utilisateur
{
    public interface IClientService
    {
        Task<ClientDto> GetByIdAsync(long utilisateurId);
        Task<IEnumerable<ClientDto>> GetAllClientsAsync();
        Task<ClientDto> GetByCodeParrainageAsync(string codeParrainage);
    }
}
