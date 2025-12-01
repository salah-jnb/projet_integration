using JNBFitness.Domain.Entities;
using JNBFitness.Domain.Enums;
using JNBFitness.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace JNBFitness.Infrastructure.Repositories
{
    public class ArticleRepository : Repository<Article>, IArticleRepository
    {
        public ArticleRepository(ApplicationDbContext context) : base(context)
        {
        }

        public async Task<IEnumerable<Article>> GetArticlesPubliesAsync()
        {
            return await _dbSet
                .Include(a => a.Coach)
                .ThenInclude(c => c.Utilisateur)
                .Where(a => a.Statut == StatutArticle.PUBLIE)
                .OrderByDescending(a => a.DatePublication)
                .ToListAsync();
        }

        public async Task<IEnumerable<Article>> GetArticlesEnAttenteAsync()
        {
            return await _dbSet
                .Include(a => a.Coach)
                .ThenInclude(c => c.Utilisateur)
                .Where(a => a.Statut == StatutArticle.EN_ATTENTE_VALIDATION)
                .OrderByDescending(a => a.DateCreation)
                .ToListAsync();
        }

        public async Task<IEnumerable<Article>> GetArticlesByCoachIdAsync(long coachId)
        {
            return await _dbSet
                .Where(a => a.CoachId == coachId)
                .OrderByDescending(a => a.DateCreation)
                .ToListAsync();
        }
    }
}
