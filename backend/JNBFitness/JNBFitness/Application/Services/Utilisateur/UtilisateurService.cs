using AutoMapper;
using JNBFitness.Application.DTOs.Utilisateur;
using JNBFitness.Domain.Entities;
using JNBFitness.Domain.Enums;
using JNBFitness.Infrastructure.Repositories;
using System.Security.Cryptography;
using System.Text;

namespace JNBFitness.Application.Services.Utilisateur
{
    public class UtilisateurService : IUtilisateurService
    {
        private readonly IUtilisateurRepository _utilisateurRepository;
        private readonly IClientRepository _clientRepository;
        private readonly ICoachRepository _coachRepository;
        private readonly ICarteRepository _carteRepository;
        private readonly IMapper _mapper;

        public UtilisateurService(
            IUtilisateurRepository utilisateurRepository,
            IClientRepository clientRepository,
            ICoachRepository coachRepository,
            ICarteRepository carteRepository,
            IMapper mapper)
        {
            _utilisateurRepository = utilisateurRepository;
            _clientRepository = clientRepository;
            _coachRepository = coachRepository;
            _carteRepository = carteRepository;
            _mapper = mapper;
        }

        public async Task<UtilisateurDto> GetByIdAsync(long id)
        {
            var utilisateur = await _utilisateurRepository.GetByIdAsync(id);
            if (utilisateur == null)
                throw new Exception("Utilisateur introuvable");

            return _mapper.Map<UtilisateurDto>(utilisateur);
        }

        public async Task<UtilisateurDto> GetByEmailAsync(string email)
        {
            var utilisateur = await _utilisateurRepository.GetByEmailAsync(email);
            if (utilisateur == null)
                throw new Exception("Utilisateur introuvable");

            return _mapper.Map<UtilisateurDto>(utilisateur);
        }

        public async Task<UtilisateurDto> UpdateProfileAsync(long id, UpdateProfileDto updateDto)
        {
            var utilisateur = await _utilisateurRepository.GetByIdAsync(id);
            if (utilisateur == null)
                throw new Exception("Utilisateur introuvable");

            utilisateur.Nom = updateDto.Nom ?? utilisateur.Nom;
            utilisateur.Prenom = updateDto.Prenom ?? utilisateur.Prenom;
            utilisateur.Telephone = updateDto.Telephone ?? utilisateur.Telephone;
            utilisateur.Adresse = updateDto.Adresse ?? utilisateur.Adresse;
            utilisateur.Photo = updateDto.Photo ?? utilisateur.Photo;
            

            await _utilisateurRepository.UpdateAsync(utilisateur);

            return _mapper.Map<UtilisateurDto>(utilisateur);
        }

        public async Task<IEnumerable<UtilisateurDto>> GetAllAsync()
        {
            var utilisateurs = await _utilisateurRepository.GetAllAsync();
            return _mapper.Map<IEnumerable<UtilisateurDto>>(utilisateurs);
        }

        public async Task<IEnumerable<UtilisateurDto>> GetAllAsync(string? type, string? statut, bool? abonneNewsletter, string? search)
        {
            var results = await _utilisateurRepository.FindAsync(u =>
                (string.IsNullOrEmpty(type) || u.TypeUtilisateur.ToString() == type) &&
                (string.IsNullOrEmpty(statut) || u.Statut.ToString() == statut) &&
                (!abonneNewsletter.HasValue || u.AbonneNewsletter == abonneNewsletter.Value) &&
                (string.IsNullOrEmpty(search) ||
                    (u.Nom != null && u.Nom.ToLower().Contains(search.ToLower())) ||
                    (u.Prenom != null && u.Prenom.ToLower().Contains(search.ToLower())) ||
                    (u.Email != null && u.Email.ToLower().Contains(search.ToLower())) ||
                    (u.Telephone != null && u.Telephone.ToLower().Contains(search.ToLower()))));

            return _mapper.Map<IEnumerable<UtilisateurDto>>(results);
        }

        public async Task DeleteAsync(long id)
        {
            var utilisateur = await _utilisateurRepository.GetByIdAsync(id);
            if (utilisateur == null)
                throw new Exception("Utilisateur introuvable");

            await _utilisateurRepository.DeleteAsync(utilisateur);
        }

        public async Task<UtilisateurDto> CreateUserAsync(CreateUserDto createDto)
        {
            // Vérifier si l'email existe déjà
            if (await _utilisateurRepository.EmailExistsAsync(createDto.Email))
            {
                throw new Exception("Cet email est déjà utilisé");
            }

            // Créer l'utilisateur
            var utilisateur = new JNBFitness.Domain.Entities.Utilisateur
            {
                Email = createDto.Email,
                MotDePasse = HashPassword(createDto.MotDePasse),
                Nom = createDto.Nom,
                Prenom = createDto.Prenom,
                Telephone = createDto.Telephone,
                Adresse = createDto.Adresse,
                Photo = createDto.Photo,
                TypeUtilisateur = createDto.TypeUtilisateur,
                Statut = StatutUtilisateur.EN_ATTENTE,
                DateInscription = DateTime.Now,
                AbonneNewsletter = false
            };

            utilisateur = await _utilisateurRepository.AddAsync(utilisateur);

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

            // Créer le client ou le coach selon le type
            if (createDto.TypeUtilisateur == TypeUtilisateur.CLIENT)
            {
                var client = new Client
                {
                    UtilisateurId = utilisateur.Id,
                    CodeParrainage = GenerateCodeParrainage(utilisateur.Id),
                    NombreParrainagesValides = 0,
                    ParrainePar = createDto.CodeParrainage
                };

                await _clientRepository.AddAsync(client);
            }
            else if (createDto.TypeUtilisateur == TypeUtilisateur.COACH)
            {
                var coach = new Coach
                {
                    UtilisateurId = utilisateur.Id,
                    Specialites = createDto.Specialites ?? string.Empty,
                    Description = createDto.Description ?? string.Empty,
                    NoteGlobale = 0,
                    NombreAvis = 0
                };

                await _coachRepository.AddAsync(coach);
            }

            return _mapper.Map<UtilisateurDto>(utilisateur);
        }

        public async Task<UtilisateurDto> ChangeStatutAsync(long id, string statut)
        {
            var utilisateur = await _utilisateurRepository.GetByIdAsync(id);
            if (utilisateur == null)
                throw new Exception("Utilisateur introuvable");

            if (!Enum.TryParse<StatutUtilisateur>(statut, true, out var newStatut))
                throw new Exception("Statut invalide");

            utilisateur.Statut = newStatut;
            await _utilisateurRepository.UpdateAsync(utilisateur);
            return _mapper.Map<UtilisateurDto>(utilisateur);
        }

        public async Task<UtilisateurDto> SetNewsletterAsync(long id, bool abonne)
        {
            var utilisateur = await _utilisateurRepository.GetByIdAsync(id);
            if (utilisateur == null)
                throw new Exception("Utilisateur introuvable");

            utilisateur.AbonneNewsletter = abonne;
            await _utilisateurRepository.UpdateAsync(utilisateur);
            return _mapper.Map<UtilisateurDto>(utilisateur);
        }

        private string HashPassword(string password)
        {
            using (var sha256 = SHA256.Create())
            {
                var hashedBytes = sha256.ComputeHash(Encoding.UTF8.GetBytes(password));
                return Convert.ToBase64String(hashedBytes);
            }
        }

        private string GenerateCodeParrainage(long utilisateurId)
        {
            return $"PARRAIN-{utilisateurId:D6}-{new Random().Next(1000, 9999)}";
        }
    }
}
