using AutoMapper;
using JNBFitness.Application.DTOs.Parrainage;
using JNBFitness.Domain.Entities;
using JNBFitness.Infrastructure.Repositories;
using JNBFitness.Application.DTOs.Notification;
using JNBFitness.Application.Services.Notification;

namespace JNBFitness.Application.Services.Parrainage
{
    public class ParrainageService : IParrainageService
    {
        private readonly IParrainageRepository _parrainageRepository;
        private readonly IClientRepository _clientRepository;
        private readonly IMapper _mapper;
        private readonly INotificationService _notificationService;

        public ParrainageService(
            IParrainageRepository parrainageRepository,
            IClientRepository clientRepository,
            IMapper mapper,
            INotificationService notificationService)
        {
            _parrainageRepository = parrainageRepository;
            _clientRepository = clientRepository;
            _mapper = mapper;
            _notificationService = notificationService;
        }

        public async Task<IEnumerable<ParrainageDto>> GetParrainagesByParrainIdAsync(long parrainId)
        {
            var parrainages = await _parrainageRepository.GetParrainagesByParrainIdAsync(parrainId);
            return _mapper.Map<IEnumerable<ParrainageDto>>(parrainages);
        }

        public async Task<int> GetNombreParrainagesValidesAsync(long parrainId)
        {
            return await _parrainageRepository.GetNombreParrainagesValidesAsync(parrainId);
        }

        public async Task<IEnumerable<ParrainageDto>> GetParrainagesByFilleulIdAsync(long filleulId)
        {
            var parrainages = await _parrainageRepository.GetParrainagesByFilleulIdAsync(filleulId);
            return _mapper.Map<IEnumerable<ParrainageDto>>(parrainages);
        }

        public async Task<ParrainageDto> GetParrainageByIdAsync(long id)
        {
            var parrainage = await _parrainageRepository.GetParrainageWithDetailsAsync(id);
            return _mapper.Map<ParrainageDto>(parrainage);
        }

        public async Task<bool> ValiderParrainageAsync(long parrainageId)
        {
            var parrainage = await _parrainageRepository.GetByIdAsync(parrainageId);
            if (parrainage == null)
                return false;

            // Check if already validated
            if (parrainage.Valide)
                throw new Exception("Ce parrainage est déjà validé");

            // Validate the sponsorship
            parrainage.Valide = true;
            parrainage.DateValidation = DateTime.Now;
            await _parrainageRepository.UpdateAsync(parrainage);

            await _notificationService.CreateNotificationAsync(new CreateNotificationDto
            {
                DestinataireId = parrainage.ParrainId,
                Titre = "Parrainage validé",
                Message = "Votre parrainage a été validé",
                Type = "PARRAINAGE_VALIDE"
            });
            await _notificationService.CreateNotificationAsync(new CreateNotificationDto
            {
                DestinataireId = parrainage.FilleulId,
                Titre = "Parrainage validé",
                Message = "Votre parrainage a été validé",
                Type = "PARRAINAGE_VALIDE"
            });

            return true;
        }

        public async Task<string?> GetCodeParrainageByClientIdAsync(long clientId)
        {
            var client = await _clientRepository.GetByIdAsync(clientId);
            return client?.CodeParrainage;
        }

        public async Task<bool> CreateParrainageAsync(CreateParrainageDto createDto)
        {
            // Check if sponsorship already exists
            var exists = await _parrainageRepository.ExistsParrainageAsync(createDto.ParrainId, createDto.FilleulId);
            if (exists)
                throw new Exception("Ce parrainage existe déjà");

            // Validate that both parrain and filleul exist
            var parrain = await _clientRepository.GetByIdAsync(createDto.ParrainId);
            if (parrain == null)
                throw new Exception("Le parrain n'existe pas");

            var filleul = await _clientRepository.GetByIdAsync(createDto.FilleulId);
            if (filleul == null)
                throw new Exception("Le filleul n'existe pas");

            // Create the sponsorship
            var parrainage = new Domain.Entities.Parrainage
            {
                ParrainId = createDto.ParrainId,
                FilleulId = createDto.FilleulId,
                DateInscriptionFilleul = DateTime.Now,
                Valide = false,
                MoisGratuitAttribue = false
            };

            await _parrainageRepository.AddAsync(parrainage);
            return true;
        }
    }
}
