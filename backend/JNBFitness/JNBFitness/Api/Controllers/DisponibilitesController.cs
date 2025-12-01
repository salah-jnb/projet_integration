using JNBFitness.Application.DTOs.Utilisateur;
using JNBFitness.Application.Services.Utilisateur;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace JNBFitness.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class DisponibilitesController : ControllerBase
    {
        private readonly IDisponibiliteService _service;

        public DisponibilitesController(IDisponibiliteService service)
        {
            _service = service;
        }

        /// <summary>
        /// Liste des disponibilités d'un coach
        /// </summary>
        [HttpGet("coach/{coachId}")]
        [ProducesResponseType(typeof(IEnumerable<DisponibiliteCoachDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<DisponibiliteCoachDto>>> GetByCoach(long coachId)
        {
            var list = await _service.GetByCoachIdAsync(coachId);
            return Ok(list);
        }

        /// <summary>
        /// Créer une disponibilité (Coach/Admin)
        /// </summary>
        [HttpPost("coach/{coachId}")]
        [Authorize]
        [ProducesResponseType(typeof(DisponibiliteCoachDto), StatusCodes.Status201Created)]
        public async Task<ActionResult<DisponibiliteCoachDto>> Create(long coachId, [FromBody] DisponibiliteCoachDto dto)
        {
            var created = await _service.CreateAsync(coachId, dto);
            return CreatedAtAction(nameof(GetByCoach), new { coachId }, created);
        }

        /// <summary>
        /// Mettre à jour une disponibilité
        /// </summary>
        [HttpPut("{id}")]
        [Authorize]
        [ProducesResponseType(typeof(DisponibiliteCoachDto), StatusCodes.Status200OK)]
        public async Task<ActionResult<DisponibiliteCoachDto>> Update(long id, [FromBody] DisponibiliteCoachDto dto)
        {
            var updated = await _service.UpdateAsync(id, dto);
            return Ok(updated);
        }

        /// <summary>
        /// Supprimer une disponibilité
        /// </summary>
        [HttpDelete("{id}")]
        [Authorize]
        [ProducesResponseType(StatusCodes.Status200OK)]
        public async Task<ActionResult> Delete(long id)
        {
            await _service.DeleteAsync(id);
            return Ok(new { message = "Disponibilité supprimée" });
        }
    }
}


