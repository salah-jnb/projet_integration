using AutoMapper;
using JNBFitness.Application.DTOs.Utilisateur;
using JNBFitness.Domain.Entities;
using JNBFitness.Infrastructure.Repositories;

namespace JNBFitness.Application.Services.Utilisateur
{
    public class DisponibiliteService : IDisponibiliteService
    {
        private readonly IDisponibiliteCoachRepository _repo;
        private readonly IMapper _mapper;

        public DisponibiliteService(IDisponibiliteCoachRepository repo, IMapper mapper)
        {
            _repo = repo;
            _mapper = mapper;
        }

        public async Task<IEnumerable<DisponibiliteCoachDto>> GetByCoachIdAsync(long coachId)
        {
            var list = await _repo.GetByCoachIdAsync(coachId);
            return _mapper.Map<IEnumerable<DisponibiliteCoachDto>>(list);
        }

        public async Task<DisponibiliteCoachDto> CreateAsync(long coachId, DisponibiliteCoachDto dto)
        {
            var entity = new DisponibiliteCoach
            {
                CoachId = coachId,
                JourSemaine = dto.JourSemaine,
                HeureDebut = dto.HeureDebut,
                HeureFin = dto.HeureFin,
                Actif = dto.Actif
            };
            entity = await _repo.AddAsync(entity);
            return _mapper.Map<DisponibiliteCoachDto>(entity);
        }

        public async Task<DisponibiliteCoachDto> UpdateAsync(long id, DisponibiliteCoachDto dto)
        {
            var entity = await _repo.GetByIdAsync(id);
            if (entity == null) throw new Exception("Disponibilité introuvable");
            entity.JourSemaine = dto.JourSemaine;
            entity.HeureDebut = dto.HeureDebut;
            entity.HeureFin = dto.HeureFin;
            entity.Actif = dto.Actif;
            await _repo.UpdateAsync(entity);
            return _mapper.Map<DisponibiliteCoachDto>(entity);
        }

        public async Task DeleteAsync(long id)
        {
            var entity = await _repo.GetByIdAsync(id);
            if (entity == null) throw new Exception("Disponibilité introuvable");
            await _repo.DeleteAsync(entity);
        }
    }
}


