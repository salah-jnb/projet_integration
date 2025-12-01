using JNBFitness.Application.DTOs.Utilisateur;

namespace JNBFitness.Application.Services.Utilisateur
{
    public interface IUtilisateurService
    {
        Task<UtilisateurDto> GetByIdAsync(long id);
        Task<UtilisateurDto> GetByEmailAsync(string email);
        Task<UtilisateurDto> UpdateProfileAsync(long id, UpdateProfileDto updateDto);
        Task<IEnumerable<UtilisateurDto>> GetAllAsync();
        Task<IEnumerable<UtilisateurDto>> GetAllAsync(string? type, string? statut, bool? abonneNewsletter, string? search);
        Task DeleteAsync(long id);
        Task<UtilisateurDto> CreateUserAsync(CreateUserDto createDto);
        Task<UtilisateurDto> ChangeStatutAsync(long id, string statut);
        Task<UtilisateurDto> SetNewsletterAsync(long id, bool abonne);
    }
}
