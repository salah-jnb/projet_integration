using JNBFitness.Application.DTOs.Paiement;
using JNBFitness.Application.Services.Paiement;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace JNBFitness.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class TransfertsController : ControllerBase
    {
        private readonly ITransfertService _transfertService;

        public TransfertsController(ITransfertService transfertService)
        {
            _transfertService = transfertService;
        }

        /// <summary>
        /// Créer un nouveau transfert
        /// </summary>
        [HttpPost]
        [ProducesResponseType(typeof(TransfertDto), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<TransfertDto>> Create([FromBody] CreateTransfertDto createDto)
        {
            try
            {
                var transfert = await _transfertService.CreateTransfertAsync(createDto);
                return CreatedAtAction(nameof(Create), transfert);
            }
            catch (Exception ex)
            {
                var details = ex.InnerException?.Message ?? ex.Message;
                return BadRequest(new { message = details });
            }
        }

        /// <summary>
        /// Récupérer l'historique des transferts d'une carte
        /// </summary>
        [HttpGet("carte/{carteId}")]
        [ProducesResponseType(typeof(IEnumerable<TransfertDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<TransfertDto>>> GetByCarteId(long carteId)
        {
            var transferts = await _transfertService.GetTransfertsByCarteIdAsync(carteId);
            return Ok(transferts);
        }
    }
}
