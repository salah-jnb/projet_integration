using JNBFitness.Application.DTOs.Notation;
using JNBFitness.Application.Services.Notation;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace JNBFitness.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class NotationsController : ControllerBase
    {
        private readonly INotationCoachService _service;

        public NotationsController(INotationCoachService service)
        {
            _service = service;
        }

        [HttpPost]
        [ProducesResponseType(typeof(NotationCoachDto), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<NotationCoachDto>> Create([FromBody] CreateNotationCoachDto dto)
        {
            try
            {
                var created = await _service.CreateAsync(dto);
                return CreatedAtAction(nameof(GetByReservation), new { reservationId = created.ReservationCoachingId }, created);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        [HttpGet("coach/{coachId}")]
        [ProducesResponseType(typeof(IEnumerable<NotationCoachDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<NotationCoachDto>>> GetByCoach(long coachId)
        {
            var list = await _service.GetByCoachIdAsync(coachId);
            return Ok(list);
        }

        [HttpGet("reservation/{reservationId}")]
        [ProducesResponseType(typeof(NotationCoachDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<NotationCoachDto>> GetByReservation(long reservationId)
        {
            var item = await _service.GetByReservationIdAsync(reservationId);
            if (item == null) return NotFound();
            return Ok(item);
        }
    }
}