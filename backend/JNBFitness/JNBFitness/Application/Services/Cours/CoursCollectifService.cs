using AutoMapper;
using JNBFitness.Application.DTOs.Cours;
using JNBFitness.Domain.Entities;
using JNBFitness.Infrastructure.Repositories;

namespace JNBFitness.Application.Services.Cours
{
    public class CoursCollectifService : ICoursCollectifService
    {
        private readonly ICoursCollectifRepository _coursRepository;
        private readonly IMapper _mapper;

        public CoursCollectifService(ICoursCollectifRepository coursRepository, IMapper mapper)
        {
            _coursRepository = coursRepository;
            _mapper = mapper;
        }

        public async Task<CoursCollectifDto> CreateCoursAsync(CreateCoursCollectifDto createDto)
        {
            var cours = _mapper.Map<CoursCollectif>(createDto);
            cours.Actif = true;

            cours = await _coursRepository.AddAsync(cours);
            return _mapper.Map<CoursCollectifDto>(cours);
        }

        public async Task<CoursCollectifDto> UpdateCoursAsync(long id, UpdateCoursCollectifDto updateDto)
        {
            var cours = await _coursRepository.GetByIdAsync(id);
            if (cours == null)
                throw new Exception("Cours introuvable");

            if (!string.IsNullOrEmpty(updateDto.Nom))
                cours.Nom = updateDto.Nom;
            if (!string.IsNullOrEmpty(updateDto.Description))
                cours.Description = updateDto.Description;
            if (!string.IsNullOrEmpty(updateDto.JourSemaine))
                cours.JourSemaine = updateDto.JourSemaine;
            if (updateDto.HeureDebut.HasValue)
                cours.HeureDebut = updateDto.HeureDebut.Value;
            if (updateDto.HeureFin.HasValue)
                cours.HeureFin = updateDto.HeureFin.Value;
            if (updateDto.CapaciteMax.HasValue)
                cours.CapaciteMax = updateDto.CapaciteMax.Value;
            if (updateDto.Actif.HasValue)
                cours.Actif = updateDto.Actif.Value;

            await _coursRepository.UpdateAsync(cours);
            return _mapper.Map<CoursCollectifDto>(cours);
        }

        public async Task<IEnumerable<CoursCollectifDto>> GetCoursActifsAsync()
        {
            var cours = await _coursRepository.GetCoursActifsAsync();
            return _mapper.Map<IEnumerable<CoursCollectifDto>>(cours);
        }

        public async Task<CoursCollectifDto> GetCoursByIdAsync(long id)
        {
            var cours = await _coursRepository.GetByIdAsync(id);
            if (cours == null)
                throw new Exception("Cours introuvable");

            return _mapper.Map<CoursCollectifDto>(cours);
        }

        public async Task<bool> DeleteCoursAsync(long id)
        {
            var cours = await _coursRepository.GetByIdAsync(id);
            if (cours == null)
                throw new Exception("Cours introuvable");

            await _coursRepository.DeleteAsync(cours);
            return true;
        }
    }
}
