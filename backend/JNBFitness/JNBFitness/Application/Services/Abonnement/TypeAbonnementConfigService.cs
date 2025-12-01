using AutoMapper;
using JNBFitness.Application.DTOs.Abonnement;
using JNBFitness.Infrastructure.Repositories;
using JNBFitness.Domain.Enums;

namespace JNBFitness.Application.Services.Abonnement
{
    public class TypeAbonnementConfigService : ITypeAbonnementConfigService
    {
        private readonly ITypeAbonnementConfigRepository _typeAbonnementConfigRepository;
        private readonly IMapper _mapper;

        public TypeAbonnementConfigService(ITypeAbonnementConfigRepository typeAbonnementConfigRepository, IMapper mapper)
        {
            _typeAbonnementConfigRepository = typeAbonnementConfigRepository;
            _mapper = mapper;
        }

        public async Task<IEnumerable<TypeAbonnementDto>> GetAllTypesAsync()
        {
            var types = await _typeAbonnementConfigRepository.GetAllAsync();
            return _mapper.Map<IEnumerable<TypeAbonnementDto>>(types);
        }

        public async Task<IEnumerable<TypeAbonnementDto>> GetAllTypesActifsAsync()
        {
            var types = await _typeAbonnementConfigRepository.GetAllActifsAsync();
            return _mapper.Map<IEnumerable<TypeAbonnementDto>>(types);
        }

        public async Task<TypeAbonnementDto> GetTypeByIdAsync(long id)
        {
            var type = await _typeAbonnementConfigRepository.GetByIdAsync(id);
            if (type == null)
                throw new Exception("Type d'abonnement introuvable");

            return _mapper.Map<TypeAbonnementDto>(type);
        }

        public async Task<TypeAbonnementDto> CreateTypeAsync(CreateTypeAbonnementConfigDto createDto)
        {
            var entity = new Domain.Entities.TypeAbonnementConfig
            {
                Type = ParseType(createDto.Type),
                Nom = createDto.Nom,
                Description = createDto.Description,
                DureeEnMois = createDto.DureeEnMois,
                NombreSeances = createDto.NombreSeances,
                Prix = createDto.Prix,
                Actif = true
            };

            entity = await _typeAbonnementConfigRepository.AddAsync(entity);
            return _mapper.Map<TypeAbonnementDto>(entity);
        }

        public async Task<TypeAbonnementDto> UpdateTypeAsync(long id, UpdateTypeAbonnementConfigDto updateDto)
        {
            var entity = await _typeAbonnementConfigRepository.GetByIdAsync(id);
            if (entity == null)
                throw new Exception("Type d'abonnement introuvable");

            if (!string.IsNullOrEmpty(updateDto.Type))
                entity.Type = ParseType(updateDto.Type);
            if (!string.IsNullOrEmpty(updateDto.Nom))
                entity.Nom = updateDto.Nom;
            if (!string.IsNullOrEmpty(updateDto.Description))
                entity.Description = updateDto.Description;
            if (updateDto.DureeEnMois.HasValue)
                entity.DureeEnMois = updateDto.DureeEnMois.Value;
            if (updateDto.NombreSeances.HasValue)
                entity.NombreSeances = updateDto.NombreSeances.Value;
            if (updateDto.Prix.HasValue)
                entity.Prix = updateDto.Prix.Value;
            if (updateDto.Actif.HasValue)
                entity.Actif = updateDto.Actif.Value;

            await _typeAbonnementConfigRepository.UpdateAsync(entity);
            return _mapper.Map<TypeAbonnementDto>(entity);
        }

        public async Task<bool> DeleteTypeAsync(long id)
        {
            var entity = await _typeAbonnementConfigRepository.GetByIdAsync(id);
            if (entity == null)
                throw new Exception("Type d'abonnement introuvable");

            await _typeAbonnementConfigRepository.DeleteAsync(entity);
            return true;
        }

        private static TypeAbonnement ParseType(string type)
        {
            if (!Enum.TryParse<TypeAbonnement>(type, ignoreCase: true, out var parsed))
                throw new Exception("Type d'abonnement invalide");
            return parsed;
        }
    }
}