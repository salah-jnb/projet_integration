using JNBFitness.Application.DTOs.Paiement;
using JNBFitness.Application.Services.Paiement;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace JNBFitness.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class PaiementsController : ControllerBase
    {
        private readonly IPaiementService _paiementService;

        public PaiementsController(IPaiementService paiementService)
        {
            _paiementService = paiementService;
        }

        /// <summary>
        /// Récupérer tous les paiements
        /// </summary>
        [HttpGet]
        [ProducesResponseType(typeof(IEnumerable<PaiementDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<PaiementDto>>> GetAll()
        {
            var paiements = await _paiementService.GetAllAsync();
            return Ok(paiements);
        }

        /// <summary>
        /// Créer un nouveau paiement
        /// </summary>
        [HttpPost]
        [ProducesResponseType(typeof(PaiementDto), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<PaiementDto>> Create([FromBody] CreatePaiementDto createDto)
        {
            try
            {
                var paiement = await _paiementService.CreatePaiementAsync(createDto);
                return CreatedAtAction(nameof(Create), paiement);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Récupérer l'historique des paiements d'un client
        /// </summary>
        [HttpGet("client/{clientId}")]
        [ProducesResponseType(typeof(IEnumerable<PaiementDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<PaiementDto>>> GetByClientId(long clientId)
        {
            var paiements = await _paiementService.GetPaiementsByClientIdAsync(clientId);
            return Ok(paiements);
        }
    }
}
