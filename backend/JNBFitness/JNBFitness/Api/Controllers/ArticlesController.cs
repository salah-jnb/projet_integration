using JNBFitness.Application.DTOs.Article;
using JNBFitness.Application.Services.Article;
using JNBFitness.Domain.Enums;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Http;
using System.IO;

namespace JNBFitness.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class ArticlesController : ControllerBase
    {
        private readonly IArticleService _articleService;

        public ArticlesController(IArticleService articleService)
        {
            _articleService = articleService;
        }

        /// <summary>
        /// Récupérer tous les articles publiés
        /// </summary>
        [HttpGet]
        [ProducesResponseType(typeof(IEnumerable<ArticleDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<ArticleDto>>> GetAll()
        {
            var articles = await _articleService.GetArticlesPubliesAsync();
            return Ok(articles);
        }

        [HttpGet("en-attente")]
        [Authorize]
        [ProducesResponseType(typeof(IEnumerable<ArticleDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<ArticleDto>>> GetEnAttente()
        {
            var articles = await _articleService.GetArticlesEnAttenteAsync();
            return Ok(articles);
        }

        /// <summary>
        /// Récupérer un article par ID
        /// </summary>
        [HttpGet("{id}")]
        [ProducesResponseType(typeof(ArticleDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<ArticleDto>> GetById(long id)
        {
            try
            {
                var article = await _articleService.GetArticleByIdAsync(id);
                return Ok(article);
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Récupérer les articles d'un coach
        /// </summary>
        [HttpGet("coach/{coachId}")]
        [ProducesResponseType(typeof(IEnumerable<ArticleDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<ArticleDto>>> GetByCoachId(long coachId)
        {
            var articles = await _articleService.GetArticlesByCoachIdAsync(coachId);
            return Ok(articles);
        }

        /// <summary>
        /// Créer un nouvel article
        /// </summary>
        [HttpPost]
        [Authorize]
        [ProducesResponseType(typeof(ArticleDto), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<ArticleDto>> Create([FromForm] long coachId, [FromForm] string titre, [FromForm] string contenu, [FromForm] string? statut, IFormFile? image)
        {
            try
            {
                string? savedImageUrl = null;
                if (image != null && image.Length > 0)
                {
                    var uploadsDir = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot", "uploads", "articles");
                    if (!Directory.Exists(uploadsDir)) Directory.CreateDirectory(uploadsDir);

                    var ext = Path.GetExtension(image.FileName);
                    var safeTitle = (titre ?? "article").Trim().Replace(" ", "_").Replace(".", "_");
                    var fileName = $"{coachId}_{safeTitle}_{DateTime.Now:yyyyMMddHHmmss}{ext}";
                    var filePath = Path.Combine(uploadsDir, fileName);
                    using (var stream = new FileStream(filePath, FileMode.Create))
                    {
                        await image.CopyToAsync(stream);
                    }
                    savedImageUrl = $"/uploads/articles/{fileName}";
                }

                var createDto = new CreateArticleDto
                {
                    CoachId = coachId,
                    Titre = titre,
                    Contenu = contenu,
                    ImageUrl = savedImageUrl,
                    Statut = statut
                };

                var article = await _articleService.CreateArticleAsync(createDto);
                return CreatedAtAction(nameof(GetById), new { id = article.Id }, article);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpPut("{id}/image")]
        [Authorize]
        [RequestSizeLimit(20_000_000)]
        [ProducesResponseType(typeof(ArticleDto), StatusCodes.Status200OK)]
        public async Task<ActionResult<ArticleDto>> UploadImage(long id, IFormFile image)
        {
            if (image == null || image.Length == 0)
                return BadRequest(new { message = "Fichier invalide" });

            try
            {
                var uploadsDir = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot", "uploads", "articles");
                if (!Directory.Exists(uploadsDir)) Directory.CreateDirectory(uploadsDir);

                var ext = Path.GetExtension(image.FileName);
                var fileName = $"article_{id}_{DateTime.Now:yyyyMMddHHmmss}{ext}";
                var filePath = Path.Combine(uploadsDir, fileName);
                using (var stream = new FileStream(filePath, FileMode.Create))
                {
                    await image.CopyToAsync(stream);
                }

                var updateDto = new UpdateArticleDto
                {
                    ImageUrl = $"/uploads/articles/{fileName}"
                };
                var article = await _articleService.UpdateArticleAsync(id, updateDto);
                return Ok(article);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }


        [HttpPut("{id}")]
        [Authorize]
        [Consumes("multipart/form-data")]
        [RequestSizeLimit(20_000_000)]
        [ProducesResponseType(typeof(ArticleDto), StatusCodes.Status200OK)]
        public async Task<ActionResult<ArticleDto>> UpdateMultipart(long id, [FromForm] string? titre, [FromForm] string? contenu, IFormFile? image)
        {
            try
            {
                string? savedImageUrl = null;
                if (image != null && image.Length > 0)
                {
                    var uploadsDir = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot", "uploads", "articles");
                    if (!Directory.Exists(uploadsDir)) Directory.CreateDirectory(uploadsDir);

                    var ext = Path.GetExtension(image.FileName);
                    var safeTitle = (titre ?? "article").Trim().Replace(" ", "_").Replace(".", "_");
                    var fileName = $"article_{id}_{DateTime.Now:yyyyMMddHHmmss}{ext}";
                    var filePath = Path.Combine(uploadsDir, fileName);
                    using (var stream = new FileStream(filePath, FileMode.Create))
                    {
                        await image.CopyToAsync(stream);
                    }
                    savedImageUrl = $"/uploads/articles/{fileName}";
                }

                var updateDto = new UpdateArticleDto
                {
                    Titre = titre,
                    Contenu = contenu,
                    ImageUrl = savedImageUrl
                };

                var article = await _articleService.UpdateArticleAsync(id, updateDto);
                return Ok(article);
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Soumettre un article en brouillon pour validation (COACH)
        /// </summary>
        [HttpPut("{id}/soumettre")]
        [Authorize]
        [ProducesResponseType(typeof(ArticleDto), StatusCodes.Status200OK)]
        public async Task<ActionResult<ArticleDto>> SubmitForValidation(long id)
        {
            try
            {
                var updateDto = new UpdateArticleDto
                {
                    Statut = StatutArticle.EN_ATTENTE_VALIDATION.ToString()
                };
                var article = await _articleService.UpdateArticleAsync(id, updateDto);
                return Ok(article);
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Valider ou rejeter un article (ADMIN)
        /// </summary>
        [HttpPut("{id}/valider")]
        [Authorize]
        [ProducesResponseType(typeof(ArticleDto), StatusCodes.Status200OK)]
        public async Task<ActionResult<ArticleDto>> Validate(long id, [FromBody] ValidateArticleDto dto)
        {
            var article = await _articleService.ValidateArticleAsync(id, dto);
            return Ok(article);
        }

        /// <summary>
        /// Supprimer un article
        /// </summary>
        [HttpDelete("{id}")]
        [Authorize]
        [ProducesResponseType(StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult> Delete(long id)
        {
            try
            {
                await _articleService.DeleteArticleAsync(id);
                return Ok(new { message = "Article supprimé avec succès" });
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }
    }
}
