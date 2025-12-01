using JNBFitness.Application.DTOs.Auth;
using JNBFitness.Domain.Entities;
using JNBFitness.Domain.Enums;
using JNBFitness.Infrastructure.Repositories;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Security.Cryptography;
using System.Text;

namespace JNBFitness.Application.Services.Auth
{
    public class AuthService : IAuthService
    {
        private readonly IUtilisateurRepository _utilisateurRepository;
        private readonly IClientRepository _clientRepository;
        private readonly ICarteRepository _carteRepository;
        private readonly IConfiguration _configuration;

        public AuthService(
            IUtilisateurRepository utilisateurRepository,
            IClientRepository clientRepository,
            ICarteRepository carteRepository,
            IConfiguration configuration)
        {
            _utilisateurRepository = utilisateurRepository;
            _clientRepository = clientRepository;
            _carteRepository = carteRepository;
            _configuration = configuration;
        }

        public async Task<LoginResponseDto> RegisterAsync(RegisterRequestDto registerDto)
        {
            // Vérifier si l'email existe déjà
            if (await _utilisateurRepository.EmailExistsAsync(registerDto.Email))
            {
                throw new Exception("Cet email est déjà utilisé");
            }

            // Créer l'utilisateur
            var utilisateur = new Domain.Entities.Utilisateur
            {
                Email = registerDto.Email,
                MotDePasse = HashPassword(registerDto.MotDePasse),
                Nom = registerDto.Nom,
                Prenom = registerDto.Prenom,
                Telephone = registerDto.Telephone,
                Adresse = registerDto.Adresse,
                TypeUtilisateur = TypeUtilisateur.CLIENT,
                Statut = StatutUtilisateur.EN_ATTENTE,
                DateInscription = DateTime.Now,
                AbonneNewsletter = false
            };

            utilisateur = await _utilisateurRepository.AddAsync(utilisateur);

            // Créer le client
            var client = new Client
            {
                UtilisateurId = utilisateur.Id,
                CodeParrainage = GenerateCodeParrainage(utilisateur.Id),
                NombreParrainagesValides = 0,
                ParrainePar = registerDto.CodeParrainage
            };

            await _clientRepository.AddAsync(client);

            // Créer la carte virtuelle
            var carte = new Carte
            {
                UtilisateurId = utilisateur.Id,
                Numero = $"JNB-{utilisateur.Id:D10}-{DateTimeOffset.UtcNow.ToUnixTimeSeconds()}",
                Libelle = $"Carte JNB - {utilisateur.Prenom} {utilisateur.Nom}",
                Devise = "TND",
                SoldeCent = 0,
                Active = true,
                DateCreation = DateTime.Now,
                DateMiseAJour = DateTime.Now
            };

            await _carteRepository.AddAsync(carte);

            // Générer le token JWT
            var token = await GenerateJwtToken(utilisateur.Id, utilisateur.Email, utilisateur.TypeUtilisateur.ToString());

            return new LoginResponseDto
            {
                Token = token,
                Expiration = DateTime.Now.AddMinutes(int.Parse(_configuration["Jwt:ExpirationInMinutes"] ?? "60")),
                UtilisateurId = utilisateur.Id,
                Email = utilisateur.Email,
                Nom = utilisateur.Nom,
                Prenom = utilisateur.Prenom,
                TypeUtilisateur = utilisateur.TypeUtilisateur.ToString()
            };
        }

        public async Task<LoginResponseDto> LoginAsync(LoginRequestDto loginDto)
        {
            var utilisateur = await _utilisateurRepository.GetByEmailAsync(loginDto.Email);

            if (utilisateur == null || !VerifyPassword(loginDto.MotDePasse, utilisateur.MotDePasse))
            {
                throw new Exception("Email ou mot de passe incorrect");
            }

            if (utilisateur.Statut != StatutUtilisateur.ACTIF && utilisateur.Statut != StatutUtilisateur.EN_ATTENTE)
            {
                throw new Exception("Votre compte est inactif ou suspendu");
            }

            var token = await GenerateJwtToken(utilisateur.Id, utilisateur.Email, utilisateur.TypeUtilisateur.ToString());

            return new LoginResponseDto
            {
                Token = token,
                Expiration = DateTime.Now.AddMinutes(int.Parse(_configuration["Jwt:ExpirationInMinutes"] ?? "60")),
                UtilisateurId = utilisateur.Id,
                Email = utilisateur.Email,
                Nom = utilisateur.Nom,
                Prenom = utilisateur.Prenom,
                TypeUtilisateur = utilisateur.TypeUtilisateur.ToString()
            };
        }

        public async Task<bool> ChangePasswordAsync(long utilisateurId, ChangePasswordDto changePasswordDto)
        {
            var utilisateur = await _utilisateurRepository.GetByIdAsync(utilisateurId);

            if (utilisateur == null)
            {
                throw new Exception("Utilisateur introuvable");
            }

            if (!VerifyPassword(changePasswordDto.AncienMotDePasse, utilisateur.MotDePasse))
            {
                throw new Exception("Ancien mot de passe incorrect");
            }

            utilisateur.MotDePasse = HashPassword(changePasswordDto.NouveauMotDePasse);
            await _utilisateurRepository.UpdateAsync(utilisateur);

            return true;
        }

        public Task<string> GenerateJwtToken(long utilisateurId, string email, string typeUtilisateur)
        {
            var securityKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_configuration["Jwt:Key"] ?? "default_secret_key_12345678901234567890"));
            var credentials = new SigningCredentials(securityKey, SecurityAlgorithms.HmacSha256);

            var claims = new[]
            {
            new Claim(JwtRegisteredClaimNames.Sub, utilisateurId.ToString()),
            new Claim(JwtRegisteredClaimNames.Email, email),
            new Claim("role", typeUtilisateur),
            new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString())
        };

            var token = new JwtSecurityToken(
                issuer: _configuration["Jwt:Issuer"],
                audience: _configuration["Jwt:Audience"],
                claims: claims,
                expires: DateTime.Now.AddMinutes(int.Parse(_configuration["Jwt:ExpirationInMinutes"] ?? "60")),
                signingCredentials: credentials
            );

            return Task.FromResult(new JwtSecurityTokenHandler().WriteToken(token));
        }

        private string HashPassword(string password)
        {
            using var sha256 = SHA256.Create();
            var hashedBytes = sha256.ComputeHash(Encoding.UTF8.GetBytes(password));
            return Convert.ToBase64String(hashedBytes);
        }


        private bool VerifyPassword(string password, string hashedPassword)
        {
            var hashOfInput = HashPassword(password);
            return hashOfInput == hashedPassword;
        }

        private string GenerateCodeParrainage(long utilisateurId)
        {
            return $"JNB{utilisateurId:D6}{DateTime.Now.Ticks % 10000}";
        }
    }
}
