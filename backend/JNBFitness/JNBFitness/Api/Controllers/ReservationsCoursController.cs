using JNBFitness.Application.DTOs.Reservation;
using JNBFitness.Application.Services.Reservation;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace JNBFitness.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class ReservationsCoursController : ControllerBase
    {
        private readonly IReservationCoursService _reservationService;

        public ReservationsCoursController(IReservationCoursService reservationService)
        {
            _reservationService = reservationService;
        }

        /// <summary>
        /// Créer une nouvelle réservation de cours collectif
        /// </summary>
        [HttpPost]
        [ProducesResponseType(typeof(ReservationCoursCollectifDto), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status409Conflict)]
        public async Task<ActionResult<ReservationCoursCollectifDto>> Create([FromBody] CreateReservationCoursDto createDto)
        {
            try
            {
                var reservation = await _reservationService.CreateReservationAsync(createDto);
                return CreatedAtAction(nameof(Create), reservation);
            }
            catch (Exception ex)
            {
                if (ex.Message.Contains("déjà réservé") || ex.Message.Contains("déjà réserver"))
                {
                    return Conflict(new { message = "Vous avez déjà réservé cette séance" });
                }
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Récupérer les réservations d'un client
        /// </summary>
        [HttpGet("client/{clientId}")]
        [ProducesResponseType(typeof(IEnumerable<ReservationCoursCollectifDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<ReservationCoursCollectifDto>>> GetByClientId(long clientId)
        {
            var reservations = await _reservationService.GetReservationsByClientIdAsync(clientId);
            return Ok(reservations);
        }

        /// <summary>
        /// Récupérer les participants (réservations) d'une séance de cours collectif
        /// </summary>
        [HttpGet("seance/{seanceId}")]
        [ProducesResponseType(typeof(IEnumerable<ReservationCoursCollectifDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<ReservationCoursCollectifDto>>> GetBySeanceId(long seanceId)
        {
            var reservations = await _reservationService.GetReservationsBySeanceIdAsync(seanceId);
            return Ok(reservations);
        }

        /// <summary>
        /// Annuler une réservation de cours
        /// </summary>
        [HttpDelete("{id}")]
        [ProducesResponseType(StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult> Cancel(long id)
        {
            try
            {
                await _reservationService.CancelReservationAsync(id);
                return Ok(new { message = "Réservation annulée avec succès" });
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }
    }
}
