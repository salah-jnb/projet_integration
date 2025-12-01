using JNBFitness.Application.DTOs.Notification;
using JNBFitness.Application.Services.Notification;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace JNBFitness.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class NotificationsController : ControllerBase
    {
        private readonly INotificationService _notificationService;

        public NotificationsController(INotificationService notificationService)
        {
            _notificationService = notificationService;
        }

        /// <summary>
        /// Récupérer les notifications d'un utilisateur
        /// </summary>
        [HttpGet("utilisateur/{utilisateurId}")]
        [ProducesResponseType(typeof(IEnumerable<NotificationDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<NotificationDto>>> GetByUtilisateurId(long utilisateurId)
        {
            var notifications = await _notificationService.GetNotificationsByUtilisateurIdAsync(utilisateurId);
            return Ok(notifications);
        }

        /// <summary>
        /// Récupérer uniquement les notifications non lues d'un utilisateur
        /// </summary>
        [HttpGet("utilisateur/{utilisateurId}/non-lues")]
        [ProducesResponseType(typeof(IEnumerable<NotificationDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<NotificationDto>>> GetNonLuesByUtilisateurId(long utilisateurId)
        {
            var notifications = await _notificationService.GetNotificationsNonLuesAsync(utilisateurId);
            return Ok(notifications);
        }

        /// <summary>
        /// Créer une nouvelle notification
        /// </summary>
        [HttpPost]
        [Authorize]
        [ProducesResponseType(typeof(NotificationDto), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<NotificationDto>> Create([FromBody] CreateNotificationDto createDto)
        {
            try
            {
                var notification = await _notificationService.CreateNotificationAsync(createDto);
                return CreatedAtAction(nameof(Create), notification);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Marquer une notification comme lue
        /// </summary>
        [HttpPut("{id}/lire")]
        [ProducesResponseType(StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult> MarquerCommeLue(long id)
        {
            try
            {
                await _notificationService.MarquerCommeLueAsync(id);
                return Ok(new { message = "Notification marquée comme lue" });
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }
    }
}
