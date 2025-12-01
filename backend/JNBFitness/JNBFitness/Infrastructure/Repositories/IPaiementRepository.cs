using JNBFitness.Domain.Entities;

namespace JNBFitness.Infrastructure.Repositories
{
    public interface IPaiementRepository : IRepository<Paiement>
    {
        Task<IEnumerable<Paiement>> GetPaiementsByClientIdAsync(long clientId);
        Task<Paiement> GetPaiementWithDetailsAsync(long paiementId);
        Task<IEnumerable<Paiement>> GetAllWithAbonnementAsync();
    }
}
