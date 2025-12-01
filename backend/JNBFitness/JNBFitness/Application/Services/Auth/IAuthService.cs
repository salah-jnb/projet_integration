using JNBFitness.Application.DTOs.Auth;

namespace JNBFitness.Application.Services.Auth
{
    public interface IAuthService
    {
        Task<LoginResponseDto> RegisterAsync(RegisterRequestDto registerDto);
        Task<LoginResponseDto> LoginAsync(LoginRequestDto loginDto);
        Task<bool> ChangePasswordAsync(long utilisateurId, ChangePasswordDto changePasswordDto);
        Task<string> GenerateJwtToken(long utilisateurId, string email, string typeUtilisateur);
    }
}
