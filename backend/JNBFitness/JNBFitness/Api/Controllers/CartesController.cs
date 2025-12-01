using JNBFitness.Application.DTOs.Paiement;
using JNBFitness.Application.Services.Paiement;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace JNBFitness.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class CartesController : ControllerBase
    {
        private readonly ICarteService _carteService;

        public CartesController(ICarteService carteService)
        {
            _carteService = carteService;
        }

        /// <summary>
        /// Récupérer toutes les cartes
        /// </summary>
        [HttpGet]
        [ProducesResponseType(typeof(IEnumerable<CarteDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<CarteDto>>> GetAll()
        {
            var cartes = await _carteService.GetAllAsync();
            return Ok(cartes);
        }

        /// <summary>
        /// Récupérer la carte d'un utilisateur
        /// </summary>
        [HttpGet("utilisateur/{utilisateurId}")]
        [ProducesResponseType(typeof(CarteDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<CarteDto>> GetByUtilisateurId(long utilisateurId)
        {
            try
            {
                var carte = await _carteService.GetCarteByUtilisateurIdAsync(utilisateurId);
                return Ok(carte);
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Recharger une carte
        /// </summary>
        [HttpPost("{carteId}/recharger")]
        [ProducesResponseType(typeof(CarteDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<CarteDto>> Recharger(long carteId, [FromBody] decimal montantEuro)
        {
            try
            {
                var carte = await _carteService.RechargerCarteAsync(carteId, montantEuro);
                return Ok(carte);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Diminuer le montant d'une carte (simuler un paiement)
        /// </summary>
        [HttpPost("{carteId}/diminuer")]
        [ProducesResponseType(typeof(CarteDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<CarteDto>> DiminuerMontant(long carteId, [FromBody] decimal montantEuro)
        {
            try
            {
                var carte = await _carteService.DiminuerMontantCarteAsync(carteId, montantEuro);
                return Ok(carte);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Transférer un montant entre deux cartes (simulation d'abonnement)
        /// </summary>
        [HttpPost("transferer")]
        [ProducesResponseType(typeof(CarteDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<CarteDto>> TransfererMontant([FromBody] CreateTransfertDto transferRequest)
        {
            try
            {
                if (string.IsNullOrWhiteSpace(transferRequest.Devise)) transferRequest.Devise = "TND";
                var carte = await _carteService.TransfererMontantAsync(
                    transferRequest.EmetteurCarteId, 
                    transferRequest.RecepteurCarteId, 
                    transferRequest.MontantEuro,
                    transferRequest.Devise);
                return Ok(carte);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}
