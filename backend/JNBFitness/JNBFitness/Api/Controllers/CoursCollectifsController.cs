using JNBFitness.Application.DTOs.Cours;
using JNBFitness.Application.Services.Cours;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace JNBFitness.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class CoursCollectifsController : ControllerBase
    {
        private readonly ICoursCollectifService _coursService;
        private readonly ISeanceCoursCollectifService _seanceService;

        public CoursCollectifsController(ICoursCollectifService coursService, ISeanceCoursCollectifService seanceService)
        {
            _coursService = coursService;
            _seanceService = seanceService;
        }

        /// <summary>
        /// Récupérer tous les cours actifs
        /// </summary>
        [HttpGet]
        [ProducesResponseType(typeof(IEnumerable<CoursCollectifDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<CoursCollectifDto>>> GetAll()
        {
            var cours = await _coursService.GetCoursActifsAsync();
            return Ok(cours);
        }

        [HttpGet("seances")]
        [ProducesResponseType(typeof(IEnumerable<SeanceCoursCollectifDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<SeanceCoursCollectifDto>>> GetToutesSeances()
        {
            var seances = await _seanceService.GetAllSeancesAsync();
            return Ok(seances);
        }

        /// <summary>
        /// Récupérer toutes les séances de cours collectif disponibles
        /// </summary>
        [HttpGet("seances/disponibles")]
        [ProducesResponseType(typeof(IEnumerable<SeanceCoursCollectifDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<SeanceCoursCollectifDto>>> GetSeancesDisponibles()
        {
            var seances = await _seanceService.GetSeancesDisponiblesAsync();
            return Ok(seances);
        }

        /// <summary>
        /// Récupérer les séances disponibles d'un coach spécifique
        /// </summary>
        [HttpGet("coach/{coachId}/seances/disponibles")]
        [ProducesResponseType(typeof(IEnumerable<SeanceCoursCollectifDto>), StatusCodes.Status200OK)]
        public async Task<ActionResult<IEnumerable<SeanceCoursCollectifDto>>> GetSeancesDisponiblesParCoach(long coachId)
        {
            var seances = await _seanceService.GetSeancesDisponiblesByCoachIdAsync(coachId);
            return Ok(seances);
        }

        /// <summary>
        /// Créer une nouvelle séance de cours collectif
        /// </summary>
        [HttpPost("seances")]
        [ProducesResponseType(typeof(SeanceCoursCollectifDto), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<SeanceCoursCollectifDto>> CreateSeance([FromBody] CreateSeanceCoursCollectifDto createDto)
        {
            try
            {
                var seance = await _seanceService.CreateSeanceAsync(createDto);
                return CreatedAtAction(nameof(CreateSeance), seance);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Récupérer un cours par ID
        /// </summary>
        [HttpGet("{id}")]
        [ProducesResponseType(typeof(CoursCollectifDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<CoursCollectifDto>> GetById(long id)
        {
            try
            {
                var cours = await _coursService.GetCoursByIdAsync(id);
                return Ok(cours);
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Créer un nouveau cours collectif
        /// </summary>
        [HttpPost]
        [Authorize]
        [ProducesResponseType(typeof(CoursCollectifDto), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<CoursCollectifDto>> Create([FromBody] CreateCoursCollectifDto createDto)
        {
            try
            {
                var cours = await _coursService.CreateCoursAsync(createDto);
                return CreatedAtAction(nameof(GetById), new { id = cours.Id }, cours);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Mettre à jour un cours collectif
        /// </summary>
        [HttpPut("{id}")]
        [Authorize]
        [ProducesResponseType(typeof(CoursCollectifDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<CoursCollectifDto>> Update(long id, [FromBody] UpdateCoursCollectifDto updateDto)
        {
            try
            {
                var cours = await _coursService.UpdateCoursAsync(id, updateDto);
                return Ok(cours);
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Supprimer un cours collectif
        /// </summary>
        [HttpDelete("{id}")]
        [Authorize]
        [ProducesResponseType(StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult> Delete(long id)
        {
            try
            {
                await _coursService.DeleteCoursAsync(id);
                return Ok(new { message = "Cours supprimé avec succès" });
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        [HttpPut("seances/{id}")]
        [Authorize]
        [ProducesResponseType(typeof(SeanceCoursCollectifDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult<SeanceCoursCollectifDto>> UpdateSeance(long id, [FromBody] UpdateSeanceCoursCollectifDto updateDto)
        {
            try
            {
                var seance = await _seanceService.UpdateSeanceAsync(id, updateDto);
                return Ok(seance);
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        [HttpPut("seances/{id}/annuler")]
        [Authorize]
        [ProducesResponseType(StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult> AnnulerSeance(long id)
        {
            try
            {
                await _seanceService.AnnulerSeanceAsync(id);
                return Ok(new { message = "Séance annulée" });
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }

        [HttpDelete("seances/{id}")]
        [Authorize]
        [ProducesResponseType(StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public async Task<ActionResult> SupprimerSeance(long id)
        {
            try
            {
                await _seanceService.DeleteSeanceAsync(id);
                return Ok(new { message = "Séance supprimée" });
            }
            catch (Exception ex)
            {
                return NotFound(new { message = ex.Message });
            }
        }
    }
}
