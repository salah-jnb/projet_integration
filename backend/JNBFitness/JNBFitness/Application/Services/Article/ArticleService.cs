using AutoMapper;
using JNBFitness.Application.DTOs.Article;
using JNBFitness.Domain.Enums;
using JNBFitness.Infrastructure.Repositories;
using JNBFitness.Application.DTOs.Notification;
using JNBFitness.Application.Services.Notification;

namespace JNBFitness.Application.Services.Article
{
    public class ArticleService : IArticleService
    {
        private readonly IArticleRepository _articleRepository;
        private readonly IMapper _mapper;
        private readonly INotificationService _notificationService;

        public ArticleService(IArticleRepository articleRepository, IMapper mapper, INotificationService notificationService)
        {
            _articleRepository = articleRepository;
            _mapper = mapper;
            _notificationService = notificationService;
        }

        public async Task<ArticleDto> CreateArticleAsync(CreateArticleDto createDto)
        {
            var initialStatut = StatutArticle.BROUILLON;
            if (!string.IsNullOrEmpty(createDto.Statut))
            {
                if (string.Equals(createDto.Statut, "EN_ATTENTE", StringComparison.OrdinalIgnoreCase))
                {
                    initialStatut = StatutArticle.EN_ATTENTE_VALIDATION;
                }
                else if (Enum.TryParse<StatutArticle>(createDto.Statut, true, out var parsed))
                {
                    if (parsed == StatutArticle.BROUILLON || parsed == StatutArticle.EN_ATTENTE_VALIDATION)
                        initialStatut = parsed;
                }
            }

            var article = new Domain.Entities.Article
            {
                CoachId = createDto.CoachId,
                Titre = createDto.Titre,
                Contenu = createDto.Contenu,
                ImageUrl = createDto.ImageUrl,
                Statut = initialStatut,
                DateCreation = DateTime.Now
            };

            article = await _articleRepository.AddAsync(article);
            return _mapper.Map<ArticleDto>(article);
        }

        public async Task<ArticleDto> UpdateArticleAsync(long id, UpdateArticleDto updateDto)
        {
            var article = await _articleRepository.GetByIdAsync(id);
            if (article == null)
                throw new Exception("Article introuvable");

            if (!string.IsNullOrEmpty(updateDto.Titre))
                article.Titre = updateDto.Titre;
            if (!string.IsNullOrEmpty(updateDto.Contenu))
                article.Contenu = updateDto.Contenu;
            if (!string.IsNullOrEmpty(updateDto.ImageUrl))
                article.ImageUrl = updateDto.ImageUrl;
            if (!string.IsNullOrEmpty(updateDto.Statut))
                article.Statut = Enum.Parse<StatutArticle>(updateDto.Statut);

            await _articleRepository.UpdateAsync(article);
            return _mapper.Map<ArticleDto>(article);
        }

        public async Task<ArticleDto> ValidateArticleAsync(long id, ValidateArticleDto dto)
        {
            var article = await _articleRepository.GetByIdAsync(id);
            if (article == null)
                throw new Exception("Article introuvable");

            article.CommentaireAdmin = dto.CommentaireAdmin;
            article.DateValidation = DateTime.Now;
            if (dto.Publier)
            {
                article.Statut = StatutArticle.PUBLIE;
                article.DatePublication = DateTime.Now;
            }
            else
            {
                article.Statut = StatutArticle.REJETE;
            }

            await _articleRepository.UpdateAsync(article);
            var typeNotif = dto.Publier ? "ARTICLE_PUBLIE" : "ARTICLE_REJETE";
            var titreNotif = dto.Publier ? "Article publié" : "Article refusé";
            var messageNotif = dto.Publier ? "Votre article a été publié" : "Votre article a été refusé";
            await _notificationService.CreateNotificationAsync(new CreateNotificationDto
            {
                DestinataireId = article.CoachId,
                Titre = titreNotif,
                Message = messageNotif,
                Type = typeNotif
            });
            return _mapper.Map<ArticleDto>(article);
        }

        public async Task<IEnumerable<ArticleDto>> GetArticlesPubliesAsync()
        {
            var articles = await _articleRepository.GetArticlesPubliesAsync();
            return _mapper.Map<IEnumerable<ArticleDto>>(articles);
        }

        public async Task<IEnumerable<ArticleDto>> GetArticlesEnAttenteAsync()
        {
            var articles = await _articleRepository.GetArticlesEnAttenteAsync();
            return _mapper.Map<IEnumerable<ArticleDto>>(articles);
        }

        public async Task<IEnumerable<ArticleDto>> GetArticlesByCoachIdAsync(long coachId)
        {
            var articles = await _articleRepository.GetArticlesByCoachIdAsync(coachId);
            return _mapper.Map<IEnumerable<ArticleDto>>(articles);
        }

        public async Task<ArticleDto> GetArticleByIdAsync(long id)
        {
            var article = await _articleRepository.GetByIdAsync(id);
            if (article == null)
                throw new Exception("Article introuvable");

            return _mapper.Map<ArticleDto>(article);
        }

        public async Task<bool> DeleteArticleAsync(long id)
        {
            var article = await _articleRepository.GetByIdAsync(id);
            if (article == null)
                throw new Exception("Article introuvable");

            await _articleRepository.DeleteAsync(article);
            return true;
        }
    }
}
