using JNBFitness.Domain.Entities;

namespace JNBFitness.Infrastructure.Repositories
{
    public interface ITransfertRepository : IRepository<Transfert>
    {
        Task<IEnumerable<Transfert>> GetTransfertsByCarteIdAsync(long carteId);
        Task<Transfert> GetByReferenceAsync(string reference);
    }
}
