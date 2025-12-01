using JNBFitness.Application.DTOs.Utilisateur;
using JNBFitness.Application.Services.Utilisateur;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.ComponentModel.DataAnnotations;

namespace JNBFitness.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class UtilisateursController : ControllerBase
    {
        private readonly IUtilisateurService _utilisateurService;

        public UtilisateursController(IUtilisateurService utilisateurService)
        {
            _utilisateurService = utilisateurService;
        }

        /// <summary>
        /// Récupérer la liste de tous les utilisateurs
        /// </summary>
        [HttpGet]
        [ProducesResponseType(typeof(IEnumerable<UtilisateurDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<UtilisateurDto>>> GetAll([FromQuery] string? type, [FromQuery] string? statut, [FromQuery] bool? newsletter, [FromQuery] string? search)
        {
            if (string.IsNullOrEmpty(type) && string.IsNullOrEmpty(statut) && !newsletter.HasValue && string.IsNullOrEmpty(search))
            {
                var all = await _utilisateurService.GetAllAsync();
                return Ok(all);
            }

            var utilisateurs = await _utilisateurService.GetAllAsync(type, statut, newsletter, search);
            return Ok(utilisateurs);
        }

        /// <summary>
        /// Récupérer un utilisateur par ID
        /// </summary>
        [HttpGet("{id}")]
        [ProducesResponseType(typeof(UtilisateurDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<UtilisateurDto>> GetById(long id)
        {
            try
            {
                var utilisateur = await _utilisateurService.GetByIdAsync(id);
                return Ok(utilisateur);
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Récupérer un utilisateur par email
        /// </summary>
        [HttpGet("by-email/{email}")]
        [ProducesResponseType(typeof(UtilisateurDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<UtilisateurDto>> GetByEmail(string email)
        {
            try
            {
                var utilisateur = await _utilisateurService.GetByEmailAsync(email);
                return Ok(utilisateur);
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Mettre à jour le profil utilisateur
        /// </summary>
        [HttpPut("{id}")]
        [ProducesResponseType(typeof(UtilisateurDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<UtilisateurDto>> UpdateProfile(long id, [FromBody] UpdateProfileDto updateDto)
        {
            try
            {
                var utilisateur = await _utilisateurService.UpdateProfileAsync(id, updateDto);
                return Ok(utilisateur);
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Récupérer la photo de profil de l'utilisateur (fichier statique)
        /// </summary>
        [HttpGet("{id}/photo")]
        [ProducesResponseType(StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<IActionResult> GetPhoto(long id)
        {
            // Récupérer l'utilisateur et le nom du fichier photo
            var utilisateur = await _utilisateurService.GetByIdAsync(id);
            if (utilisateur == null || string.IsNullOrWhiteSpace(utilisateur.Photo))
            {
                return NotFound(new { message = "Photo introuvable pour cet utilisateur" });
            }

            var uploadsDir = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot", "uploads");
            var filePath = Path.Combine(uploadsDir, utilisateur.Photo);

            if (!System.IO.File.Exists(filePath))
            {
                return NotFound(new { message = "Fichier photo non trouvé" });
            }

            // Déterminer le type de contenu en fonction de l'extension
            var ext = Path.GetExtension(filePath).ToLowerInvariant();
            var contentType = ext switch
            {
                ".jpg" => "image/jpeg",
                ".jpeg" => "image/jpeg",
                ".png" => "image/png",
                ".gif" => "image/gif",
                _ => "application/octet-stream"
            };

            return PhysicalFile(filePath, contentType);
        }

        /// <summary>
        /// Mettre à jour la photo de profil de l'utilisateur
        /// </summary>
        [HttpPut("{id}/photo")]
        [RequestSizeLimit(20_000_000)]
        [ProducesResponseType(typeof(UtilisateurDto), StatusCodes.Status200OK)]
        public async Task<ActionResult<UtilisateurDto>> UploadPhoto(long id, IFormFile file)
        {
            if (file == null || file.Length == 0)
                return BadRequest(new { message = "Fichier invalide" });

            // Récupérer l'utilisateur pour obtenir son nom
            var utilisateur = await _utilisateurService.GetByIdAsync(id);
            if (utilisateur == null)
                return NotFound(new { message = "Utilisateur introuvable" });

            // Ensure upload directory exists
            var uploadsDir = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot", "uploads");
            if (!Directory.Exists(uploadsDir)) Directory.CreateDirectory(uploadsDir);

            // Créer le nom de fichier avec l'ID et le nom de l'utilisateur
            var fileExtension = Path.GetExtension(file.FileName);
            var safeFileName = $"{id}_{utilisateur.Nom}_{utilisateur.Prenom}".Replace(" ", "_").Replace(".", "_");
            var fileName = $"{safeFileName}{fileExtension}";
            var filePath = Path.Combine(uploadsDir, fileName);

            // Supprimer l'ancienne photo si elle existe
            if (!string.IsNullOrEmpty(utilisateur.Photo))
            {
                var oldFilePath = Path.Combine(uploadsDir, utilisateur.Photo);
                if (System.IO.File.Exists(oldFilePath))
                {
                    System.IO.File.Delete(oldFilePath);
                }
            }

            using (var stream = new FileStream(filePath, FileMode.Create))
            {
                await file.CopyToAsync(stream);
            }

            // Mettre à jour la photo dans le profil
            var updateDto = new UpdateProfileDto 
            { 
                Photo = fileName // Ajouter le champ Photo au UpdateProfileDto
            };
            var updatedUtilisateur = await _utilisateurService.UpdateProfileAsync(id, updateDto);
            return Ok(updatedUtilisateur);
        }

        /// <summary>
        /// Mettre à jour le statut d'un utilisateur (ADMIN)
        /// </summary>
        [HttpPut("{id}/statut/{statut}")]
        [Authorize]
        [ProducesResponseType(typeof(UtilisateurDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<UtilisateurDto>> UpdateStatut(long id, string statut)
        {
            try
            {
                var utilisateur = await _utilisateurService.ChangeStatutAsync(id, statut);
                return Ok(utilisateur);
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Mettre à jour l'abonnement newsletter de l'utilisateur
        /// </summary>
        [HttpPut("{id}/newsletter/{abonne}")]
        [ProducesResponseType(typeof(UtilisateurDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<UtilisateurDto>> UpdateNewsletter(long id, bool abonne)
        {
            try
            {
                var utilisateur = await _utilisateurService.SetNewsletterAsync(id, abonne);
                return Ok(utilisateur);
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Supprimer un utilisateur
        /// </summary>
        [HttpDelete("{id}")]
        [ProducesResponseType(StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult> Delete(long id)
        {
            try
            {
                await _utilisateurService.DeleteAsync(id);
                return Ok(new { message = "Utilisateur supprimé avec succès" });
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Créer un nouvel utilisateur (client ou coach)
        /// </summary>
        [HttpPost]
        [ProducesResponseType(typeof(UtilisateurDto), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<UtilisateurDto>> CreateUser([FromBody] CreateUserDto createDto)
        {
            try
            {
                var utilisateur = await _utilisateurService.CreateUserAsync(createDto);
                return CreatedAtAction(nameof(GetById), new { id = utilisateur.Id }, utilisateur);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}
