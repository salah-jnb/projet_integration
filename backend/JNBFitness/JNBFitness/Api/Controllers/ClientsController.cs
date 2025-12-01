using JNBFitness.Application.DTOs.Utilisateur;
using JNBFitness.Application.Services.Utilisateur;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace JNBFitness.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class ClientsController : ControllerBase
    {
        private readonly IClientService _clientService;

        public ClientsController(IClientService clientService)
        {
            _clientService = clientService;
        }

        /// <summary>
        /// Récupérer un client par ID utilisateur
        /// </summary>
        [HttpGet("{utilisateurId}")]
        [ProducesResponseType(typeof(ClientDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<ClientDto>> GetById(long utilisateurId)
        {
            try
            {
                var client = await _clientService.GetByIdAsync(utilisateurId);
                return Ok(client);
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Récupérer tous les clients actifs
        /// </summary>
        [HttpGet]
        [Authorize]
        [ProducesResponseType(typeof(IEnumerable<ClientDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<ClientDto>>> GetAll()
        {
            var clients = await _clientService.GetAllClientsAsync();
            return Ok(clients);
        }

        /// <summary>
        /// Récupérer un client par son code de parrainage
        /// </summary>
        [HttpGet("parrainage/{codeParrainage}")]
        [AllowAnonymous]
        [ProducesResponseType(typeof(ClientDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<ClientDto>> GetByCodeParrainage(string codeParrainage)
        {
            try
            {
                var client = await _clientService.GetByCodeParrainageAsync(codeParrainage);
                return Ok(client);
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }
    }
}
