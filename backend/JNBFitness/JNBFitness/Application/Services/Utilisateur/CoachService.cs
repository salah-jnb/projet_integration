using AutoMapper;
using JNBFitness.Application.DTOs.Reservation;
using JNBFitness.Application.DTOs.Utilisateur;
using JNBFitness.Application.Services.Utilisateur;
using JNBFitness.Infrastructure.Repositories;

namespace JNBFitness.Application.Services.Utilisateur
{
    public class CoachService : ICoachService
    {
        private readonly ICoachRepository _coachRepository;
        private readonly IMapper _mapper;

        public CoachService(ICoachRepository coachRepository, IMapper mapper)
        {
            _coachRepository = coachRepository;
            _mapper = mapper;
        }

        public async Task<CoachDto> GetByIdAsync(long utilisateurId)
        {
            var coach = await _coachRepository.GetByUtilisateurIdAsync(utilisateurId);
            if (coach == null)
                throw new Exception("Coach introuvable");

            return _mapper.Map<CoachDto>(coach);
        }

        public async Task<CoachDetailsDto> GetCoachWithDetailsAsync(long utilisateurId)
        {
            var coach = await _coachRepository.GetCoachWithDetailsAsync(utilisateurId);
            if (coach == null)
                throw new Exception("Coach introuvable");

            return _mapper.Map<CoachDetailsDto>(coach);
        }

        public async Task<IEnumerable<CoachDto>> GetAllCoachsAsync()
        {
            var coachs = await _coachRepository.GetCoachsWithDisponibilitesAsync();
            return _mapper.Map<IEnumerable<CoachDto>>(coachs);
        }

        public async Task<CoachDto> UpdateCoachProfileAsync(long utilisateurId, string? specialites, string? description)
        {
            var coach = await _coachRepository.GetByUtilisateurIdAsync(utilisateurId);
            if (coach == null)
                throw new Exception("Coach introuvable");

            if (!string.IsNullOrEmpty(specialites))
                coach.Specialites = specialites;
            if (!string.IsNullOrEmpty(description))
                coach.Description = description;

            await _coachRepository.UpdateAsync(coach);
            return _mapper.Map<CoachDto>(coach);
        }
    }
}
