using JNBFitness.Application.DTOs.Auth;
using JNBFitness.Application.Services.Auth;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace JNBFitness.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class AuthController : ControllerBase
    {
        private readonly IAuthService _authService;

        public AuthController(IAuthService authService)
        {
            _authService = authService;
        }

        /// <summary>
        /// Inscription d'un nouveau client
        /// </summary>
        [AllowAnonymous]
        [HttpPost("register")]
        [ProducesResponseType(typeof(LoginResponseDto), StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult<LoginResponseDto>> Register([FromBody] RegisterRequestDto registerDto)
        {
            try
            {
                var result = await _authService.RegisterAsync(registerDto);
                return CreatedAtAction(nameof(Register), result);
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Connexion utilisateur
        /// </summary>
        [AllowAnonymous]
        [HttpPost("login")]
        [ProducesResponseType(typeof(LoginResponseDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status401Unauthorized)]
        public async Task<ActionResult<LoginResponseDto>> Login([FromBody] LoginRequestDto loginDto)
        {
            try
            {
                var result = await _authService.LoginAsync(loginDto);
                return Ok(result);
            }
            catch (Exception ex)
            {
                return Unauthorized(new { message = ex.Message });
            }
        }

        /// <summary>
        /// Changer le mot de passe
        /// </summary>
        [HttpPost("change-password/{utilisateurId}")]
        [Authorize]
        [ProducesResponseType(StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public async Task<ActionResult> ChangePassword(long utilisateurId, [FromBody] ChangePasswordDto changePasswordDto)
        {
            try
            {
                await _authService.ChangePasswordAsync(utilisateurId, changePasswordDto);
                return Ok(new { message = "Mot de passe modifié avec succès" });
            }
            catch (Exception ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }
    }
}
