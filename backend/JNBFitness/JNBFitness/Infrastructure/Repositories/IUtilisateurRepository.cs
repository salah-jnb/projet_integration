using JNBFitness.Domain.Entities;

namespace JNBFitness.Infrastructure.Repositories
{
    public interface IUtilisateurRepository : IRepository<Utilisateur>
    {
        Task<Utilisateur> GetByEmailAsync(string email);
        Task<bool> EmailExistsAsync(string email);
    }
}
