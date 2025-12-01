using JNBFitness.Domain.Entities;

namespace JNBFitness.Infrastructure.Repositories
{
    public interface IArticleRepository : IRepository<Article>
    {
        Task<IEnumerable<Article>> GetArticlesPubliesAsync();
        Task<IEnumerable<Article>> GetArticlesEnAttenteAsync();
        Task<IEnumerable<Article>> GetArticlesByCoachIdAsync(long coachId);
    }
}
