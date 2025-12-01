using JNBFitness.Application.DTOs.Reservation;
using JNBFitness.Application.Services.Reservation;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace JNBFitness.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class ReservationsCoachingController : ControllerBase
    {
        private readonly IReservationCoachingService _reservationService;

        public ReservationsCoachingController(IReservationCoachingService reservationService)
        {
            _reservationService = reservationService;
        }

        /// <summary>
        /// Créer une nouvelle réservation de coaching
        /// </summary>
        [HttpPost]
        [ProducesResponseType(typeof(ReservationCoachingDto), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<ReservationCoachingDto>> Create([FromBody] CreateReservationCoachingDto createDto)
        {
            try
            {
                var reservation = await _reservationService.CreateReservationAsync(createDto);
                return CreatedAtAction(nameof(Create), reservation);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Récupérer les réservations d'un client
        /// </summary>
        [HttpGet("client/{clientId}")]
        [ProducesResponseType(typeof(IEnumerable<ReservationCoachingDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<ReservationCoachingDto>>> GetByClientId(long clientId)
        {
            var reservations = await _reservationService.GetReservationsByClientIdAsync(clientId);
            return Ok(reservations);
        }

        /// <summary>
        /// Récupérer les réservations d'un coach
        /// </summary>
        [HttpGet("coach/{coachId}")]
        [ProducesResponseType(typeof(IEnumerable<ReservationCoachingDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<ReservationCoachingDto>>> GetByCoachId(long coachId)
        {
            var reservations = await _reservationService.GetReservationsByCoachIdAsync(coachId);
            return Ok(reservations);
        }

        /// <summary>
        /// Récupérer une réservation par son ID
        /// </summary>
        [HttpGet("{id}")]
        [ProducesResponseType(typeof(ReservationCoachingDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<ReservationCoachingDto>> GetById(long id)
        {
            try
            {
                var reservation = await _reservationService.GetReservationByIdAsync(id);
                if (reservation == null)
                    return NotFound(new { message = "Réservation introuvable" });

                return Ok(reservation);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Annuler une réservation
        /// </summary>
        [HttpDelete("{id}")]
        [ProducesResponseType(StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult> Cancel(long id)
        {
            try
            {
                var result = await _reservationService.CancelReservationAsync(id);
                if (!result)
                    return NotFound(new { message = "Réservation introuvable" });

                return Ok(new { message = "Réservation annulée avec succès" });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Marquer une séance comme terminée
        /// </summary>
        [HttpPut("{id}/complete")]
        [ProducesResponseType(StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult> Complete(long id)
        {
            try
            {
                var result = await _reservationService.CompleteReservationAsync(id);
                if (!result)
                    return NotFound(new { message = "Réservation introuvable" });

                return Ok(new { message = "Séance marquée comme terminée avec succès" });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Confirmer une réservation (EN_ATTENTE -> CONFIRMEE)
        /// </summary>
        [HttpPut("{id}/confirmer")]
        [ProducesResponseType(StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult> Confirm(long id)
        {
            try
            {
                var result = await _reservationService.ConfirmReservationAsync(id);
                if (!result)
                    return NotFound(new { message = "Réservation introuvable" });

                return Ok(new { message = "Réservation confirmée avec succès" });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Vérifier la disponibilité d'un créneau horaire
        /// </summary>
        [HttpGet("check-availability")]
        [ProducesResponseType(typeof(bool), StatusCodes.Status200OK)]
        public async Task<ActionResult<bool>> CheckAvailability([FromQuery] long coachId, [FromQuery] DateTime dateSeance, [FromQuery] int dureeMinutes)
        {
            try
            {
                var isAvailable = await _reservationService.IsTimeSlotAvailableAsync(coachId, dateSeance, dureeMinutes);
                return Ok(isAvailable);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}
