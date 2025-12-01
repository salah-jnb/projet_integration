using JNBFitness.Application.DTOs.Utilisateur;
using JNBFitness.Application.Services.Utilisateur;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace JNBFitness.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class CoachsController : ControllerBase
    {
        private readonly ICoachService _coachService;

        public CoachsController(ICoachService coachService)
        {
            _coachService = coachService;
        }

        /// <summary>
        /// Récupérer tous les coachs
        /// </summary>
        [HttpGet]
        [ProducesResponseType(typeof(IEnumerable<CoachDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<CoachDto>>> GetAll()
        {
            var coachs = await _coachService.GetAllCoachsAsync();
            return Ok(coachs);
        }

        /// <summary>
        /// Récupérer un coach par ID
        /// </summary>
        [HttpGet("{utilisateurId}")]
        [ProducesResponseType(typeof(CoachDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<CoachDto>> GetById(long utilisateurId)
        {
            try
            {
                var coach = await _coachService.GetByIdAsync(utilisateurId);
                return Ok(coach);
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Récupérer les détails complets d'un coach (avec disponibilités)
        /// </summary>
        [HttpGet("{utilisateurId}/details")]
        [ProducesResponseType(typeof(CoachDetailsDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<CoachDetailsDto>> GetDetails(long utilisateurId)
        {
            try
            {
                var coach = await _coachService.GetCoachWithDetailsAsync(utilisateurId);
                return Ok(coach);
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Mettre à jour le profil professionnel du coach
        /// </summary>
        [HttpPut("{utilisateurId}")]
        [ProducesResponseType(typeof(CoachDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<CoachDto>> UpdateCoachProfile(long utilisateurId, [FromBody] UpdateCoachProfileRequest request)
        {
            try
            {
                var coach = await _coachService.UpdateCoachProfileAsync(utilisateurId, request.Specialites, request.Description);
                return Ok(coach);
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }
    }

    public class UpdateCoachProfileRequest
    {
        public string? Specialites { get; set; }
        public string? Description { get; set; }
    }
    
}
