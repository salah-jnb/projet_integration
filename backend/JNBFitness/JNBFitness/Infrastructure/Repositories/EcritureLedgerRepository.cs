using JNBFitness.Domain.Entities;
using JNBFitness.Infrastructure.Data;

namespace JNBFitness.Infrastructure.Repositories
{
    public class EcritureLedgerRepository : Repository<EcritureLedger>, IEcritureLedgerRepository
    {
        public EcritureLedgerRepository(ApplicationDbContext context) : base(context)
        {
        }
    }
}