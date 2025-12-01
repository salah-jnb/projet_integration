using JNBFitness.Application.DTOs.Article;

namespace JNBFitness.Application.Services.Article
{
    public interface IArticleService
    {
        Task<ArticleDto> CreateArticleAsync(CreateArticleDto createDto);
        Task<ArticleDto> UpdateArticleAsync(long id, UpdateArticleDto updateDto);
        Task<IEnumerable<ArticleDto>> GetArticlesPubliesAsync();
        Task<IEnumerable<ArticleDto>> GetArticlesEnAttenteAsync();
        Task<IEnumerable<ArticleDto>> GetArticlesByCoachIdAsync(long coachId);
        Task<ArticleDto> GetArticleByIdAsync(long id);
        Task<bool> DeleteArticleAsync(long id);
        Task<ArticleDto> ValidateArticleAsync(long id, ValidateArticleDto dto);
    }
}
