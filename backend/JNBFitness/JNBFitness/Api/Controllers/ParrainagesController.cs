using JNBFitness.Application.DTOs.Parrainage;
using JNBFitness.Application.Services.Parrainage;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace JNBFitness.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class ParrainagesController : ControllerBase
    {
        private readonly IParrainageService _parrainageService;

        public ParrainagesController(IParrainageService parrainageService)
        {
            _parrainageService = parrainageService;
        }

        /// <summary>
        /// Récupérer les parrainages d'un parrain
        /// </summary>
        [HttpGet("parrain/{parrainId}")]
        [ProducesResponseType(typeof(IEnumerable<ParrainageDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<ParrainageDto>>> GetByParrainId(long parrainId)
        {
            var parrainages = await _parrainageService.GetParrainagesByParrainIdAsync(parrainId);
            return Ok(parrainages);
        }

        /// <summary>
        /// Récupérer le nombre de parrainages valides d'un parrain
        /// </summary>
        [HttpGet("parrain/{parrainId}/count")]
        [ProducesResponseType(typeof(int), StatusCodes.Status200OK)]
        public async Task<ActionResult<int>> GetNombreParrainagesValides(long parrainId)
        {
            var count = await _parrainageService.GetNombreParrainagesValidesAsync(parrainId);
            return Ok(count);
        }

        /// <summary>
        /// Récupérer les parrainages d'un filleul
        /// </summary>
        [HttpGet("filleul/{filleulId}")]
        [ProducesResponseType(typeof(IEnumerable<ParrainageDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<ParrainageDto>>> GetByFilleulId(long filleulId)
        {
            var parrainages = await _parrainageService.GetParrainagesByFilleulIdAsync(filleulId);
            return Ok(parrainages);
        }

        /// <summary>
        /// Récupérer un parrainage par son ID
        /// </summary>
        [HttpGet("{id}")]
        [ProducesResponseType(typeof(ParrainageDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<ParrainageDto>> GetById(long id)
        {
            var parrainage = await _parrainageService.GetParrainageByIdAsync(id);
            if (parrainage == null)
                return NotFound();

            return Ok(parrainage);
        }

        /// <summary>
        /// Valider un parrainage
        /// </summary>
        [HttpPut("{id}/valider")]
        [ProducesResponseType(StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult> Valider(long id)
        {
            try
            {
                var result = await _parrainageService.ValiderParrainageAsync(id);
                if (!result)
                    return NotFound(new { message = "Parrainage introuvable" });

                return Ok(new { message = "Parrainage validé avec succès" });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Récupérer le code de parrainage d'un client
        /// </summary>
        [HttpGet("client/{clientId}/code")]
        [ProducesResponseType(typeof(string), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<string>> GetCodeParrainageByClientId(long clientId)
        {
            var code = await _parrainageService.GetCodeParrainageByClientIdAsync(clientId);
            if (string.IsNullOrEmpty(code))
                return NotFound(new { message = "Client introuvable" });

            return Ok(code);
        }

        /// <summary>
        /// Créer un nouveau parrainage
        /// </summary>
        [HttpPost]
        [ProducesResponseType(typeof(ParrainageDto), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<ParrainageDto>> Create([FromBody] CreateParrainageDto createDto)
        {
            try
            {
                var result = await _parrainageService.CreateParrainageAsync(createDto);
                if (!result)
                    return BadRequest(new { message = "Erreur lors de la création du parrainage" });

                return CreatedAtAction(nameof(GetById), new { id = createDto.ParrainId }, createDto);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}
