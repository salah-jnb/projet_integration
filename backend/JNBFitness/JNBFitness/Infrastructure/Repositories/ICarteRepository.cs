using JNBFitness.Domain.Entities;

namespace JNBFitness.Infrastructure.Repositories
{
    public interface ICarteRepository : IRepository<Carte>
    {
        Task<Carte> GetByUtilisateurIdAsync(long utilisateurId);
        Task<Carte> GetByNumeroAsync(string numero);
    }
}
