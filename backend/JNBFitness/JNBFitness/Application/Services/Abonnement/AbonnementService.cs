using AutoMapper;
using JNBFitness.Application.DTOs.Abonnement;
using JNBFitness.Domain.Entities;
using JNBFitness.Domain.Enums;
using JNBFitness.Infrastructure.Repositories;

namespace JNBFitness.Application.Services.Abonnement
{
    public class AbonnementService : IAbonnementService
    {
        private readonly IAbonnementRepository _abonnementRepository;
        private readonly ITypeAbonnementConfigRepository _typeAbonnementRepository;
        private readonly IMapper _mapper;

        public AbonnementService(IAbonnementRepository abonnementRepository, ITypeAbonnementConfigRepository typeAbonnementRepository, IMapper mapper)
        {
            _abonnementRepository = abonnementRepository;
            _typeAbonnementRepository = typeAbonnementRepository;
            _mapper = mapper;
        }

        public async Task<AbonnementDto> CreateAbonnementAsync(CreateAbonnementDto createDto)
        {
            var type = await _typeAbonnementRepository.GetByIdAsync(createDto.TypeAbonnementId);
            if (type == null)
                throw new Exception("Type d'abonnement introuvable");

            var abonnement = new AbonnementClient
            {
                ClientId = createDto.ClientId,
                TypeAbonnementId = createDto.TypeAbonnementId,
                DateDebut = createDto.DateDebut,
                Statut = StatutAbonnement.ACTIF,
                OffertParParrainage = false
            };

            if (type.Type == TypeAbonnement.SALLE || type.Type == TypeAbonnement.COURS_COLLECTIFS)
            {
                var duree = type.DureeEnMois.HasValue ? type.DureeEnMois.Value : 1;
                abonnement.DateFin = abonnement.DateDebut.AddMonths(duree);
            }
            else
            {
                int? seances = type.NombreSeances;
                if (!seances.HasValue)
                {
                    seances = type.Type switch
                    {
                        TypeAbonnement.PACK_COACHING_5 => 5,
                        TypeAbonnement.PACK_COACHING_10 => 10,
                        TypeAbonnement.PACK_COACHING_20 => 20,
                        _ => null
                    };
                }
                abonnement.SeancesRestantes = seances;
            }

            abonnement = await _abonnementRepository.AddAsync(abonnement);
            return _mapper.Map<AbonnementDto>(abonnement);
        }

        public async Task<IEnumerable<AbonnementDto>> GetAbonnementsByClientIdAsync(long clientId)
        {
            var abonnements = await _abonnementRepository.GetAbonnementsByClientIdAsync(clientId);
            return _mapper.Map<IEnumerable<AbonnementDto>>(abonnements);
        }

        public async Task<IEnumerable<AbonnementDto>> GetAbonnementsActifsAsync(long clientId)
        {
            var abonnements = await _abonnementRepository.GetAbonnementsActifsAsync(clientId);
            return _mapper.Map<IEnumerable<AbonnementDto>>(abonnements);
        }

        public async Task<AbonnementDetailsDto> GetAbonnementDetailsAsync(long abonnementId)
        {
            var abonnement = await _abonnementRepository.GetAbonnementWithDetailsAsync(abonnementId);
            if (abonnement == null)
                throw new Exception("Abonnement introuvable");

            return _mapper.Map<AbonnementDetailsDto>(abonnement);
        }

        public async Task<bool> CancelAbonnementAsync(long abonnementId)
        {
            var abonnement = await _abonnementRepository.GetByIdAsync(abonnementId);
            if (abonnement == null)
                throw new Exception("Abonnement introuvable");

            abonnement.Statut = StatutAbonnement.ANNULE;
            await _abonnementRepository.UpdateAsync(abonnement);

            return true;
        }

        public async Task<IEnumerable<AbonnementDto>> GetAllAsync()
        {
            var abonnements = await _abonnementRepository.GetAllWithTypeAsync();
            return _mapper.Map<IEnumerable<AbonnementDto>>(abonnements);
        }

        public async Task<IEnumerable<AbonnementDto>> GetAllActifsAsync()
        {
            var abonnements = await _abonnementRepository.GetAllActifsWithTypeAsync();
            return _mapper.Map<IEnumerable<AbonnementDto>>(abonnements);
        }
    }
}
