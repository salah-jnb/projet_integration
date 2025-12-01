using JNBFitness.Application.DTOs.Abonnement;
using JNBFitness.Application.Services.Abonnement;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace JNBFitness.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class AbonnementsController : ControllerBase
    {
        private readonly IAbonnementService _abonnementService;
        private readonly ITypeAbonnementConfigService _typeAbonnementConfigService;

        public AbonnementsController(IAbonnementService abonnementService, ITypeAbonnementConfigService typeAbonnementConfigService)
        {
            _abonnementService = abonnementService;
            _typeAbonnementConfigService = typeAbonnementConfigService;
        }

        /// <summary>
        /// Récupérer tous les abonnements
        /// </summary>
        [HttpGet]
        [ProducesResponseType(typeof(IEnumerable<AbonnementDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<AbonnementDto>>> GetAll()
        {
            var abonnements = await _abonnementService.GetAllAsync();
            return Ok(abonnements);
        }

        /// <summary>
        /// Récupérer tous les abonnements actifs (admin)
        /// </summary>
        [HttpGet("actifs")]
        [ProducesResponseType(typeof(IEnumerable<AbonnementDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<AbonnementDto>>> GetAllActifs()
        {
            var abonnements = await _abonnementService.GetAllActifsAsync();
            return Ok(abonnements);
        }

        /// <summary>
        /// Créer un nouvel abonnement
        /// </summary>
        [HttpPost]
        [ProducesResponseType(typeof(AbonnementDto), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<AbonnementDto>> Create([FromBody] CreateAbonnementDto createDto)
        {
            // Diagnostic: Ajoute ces lignes ici
            Console.WriteLine("User authenticated ? " + User.Identity.IsAuthenticated);
            Console.WriteLine("User claims:");
            foreach (var c in User.Claims)
                Console.WriteLine($"[{c.Type}] = {c.Value}");
            try
            {
                var abonnement = await _abonnementService.CreateAbonnementAsync(createDto);
                return CreatedAtAction(nameof(GetDetails), new { id = abonnement.Id }, abonnement);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Récupérer les abonnements d'un client
        /// </summary>
        [HttpGet("client/{clientId}")]
        [ProducesResponseType(typeof(IEnumerable<AbonnementDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<AbonnementDto>>> GetByClientId(long clientId)
        {
            var abonnements = await _abonnementService.GetAbonnementsByClientIdAsync(clientId);
            return Ok(abonnements);
        }

        /// <summary>
        /// Récupérer les abonnements actifs d'un client
        /// </summary>
        [HttpGet("client/{clientId}/actifs")]
        [ProducesResponseType(typeof(IEnumerable<AbonnementDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<AbonnementDto>>> GetActifsByClientId(long clientId)
        {
            var abonnements = await _abonnementService.GetAbonnementsActifsAsync(clientId);
            return Ok(abonnements);
        }

        /// <summary>
        /// Récupérer les détails d'un abonnement
        /// </summary>
        [HttpGet("{id}")]
        [ProducesResponseType(typeof(AbonnementDetailsDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<AbonnementDetailsDto>> GetDetails(long id)
        {
            try
            {
                var abonnement = await _abonnementService.GetAbonnementDetailsAsync(id);
                return Ok(abonnement);
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Annuler un abonnement
        /// </summary>
        [HttpDelete("{id}")]
        [ProducesResponseType(StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult> Cancel(long id)
        {
            try
            {
                await _abonnementService.CancelAbonnementAsync(id);
                return Ok(new { message = "Abonnement annulé avec succès" });
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Récupérer tous les types d'abonnement disponibles
        /// </summary>
        [HttpGet("types")]
        [ProducesResponseType(typeof(IEnumerable<TypeAbonnementDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<TypeAbonnementDto>>> GetTypes()
        {
            var types = await _typeAbonnementConfigService.GetAllTypesActifsAsync();
            return Ok(types);
        }

        /// <summary>
        /// Créer un type d'abonnement
        /// </summary>
        [HttpPost("types")]
        [Authorize]
        [ProducesResponseType(typeof(TypeAbonnementDto), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<TypeAbonnementDto>> CreateType([FromBody] CreateTypeAbonnementConfigDto createDto)
        {
            try
            {
                var type = await _typeAbonnementConfigService.CreateTypeAsync(createDto);
                return CreatedAtAction(nameof(GetTypes), type);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Mettre à jour un type d'abonnement
        /// </summary>
        [HttpPut("types/{id}")]
        [Authorize]
        [ProducesResponseType(typeof(TypeAbonnementDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<TypeAbonnementDto>> UpdateType(long id, [FromBody] UpdateTypeAbonnementConfigDto updateDto)
        {
            try
            {
                var type = await _typeAbonnementConfigService.UpdateTypeAsync(id, updateDto);
                return Ok(type);
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Supprimer un type d'abonnement
        /// </summary>
        [HttpDelete("types/{id}")]
        [Authorize]
        [ProducesResponseType(StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult> DeleteType(long id)
        {
            try
            {
                await _typeAbonnementConfigService.DeleteTypeAsync(id);
                return Ok(new { message = "Type d'abonnement supprimé avec succès" });
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }
    }
}
