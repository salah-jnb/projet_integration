using AutoMapper;
using JNBFitness.Application.DTOs.Paiement;
using JNBFitness.Domain.Enums;
using JNBFitness.Infrastructure.Repositories;

namespace JNBFitness.Application.Services.Paiement
{
    public class PaiementService : IPaiementService
    {
        private readonly IPaiementRepository _paiementRepository;
        private readonly IMapper _mapper;

        public PaiementService(IPaiementRepository paiementRepository, IMapper mapper)
        {
            _paiementRepository = paiementRepository;
            _mapper = mapper;
        }

        public async Task<PaiementDto> CreatePaiementAsync(CreatePaiementDto createDto)
        {
            var paiement = new Domain.Entities.Paiement
            {
                ClientId = createDto.ClientId,
                AbonnementId = createDto.AbonnementId,
                Montant = createDto.Montant,
                DatePaiement = DateTime.Now,
                MethodePaiement = createDto.MethodePaiement,
                Statut = StatutPaiement.VALIDE
            };

            paiement = await _paiementRepository.AddAsync(paiement);
            return _mapper.Map<PaiementDto>(paiement);
        }

        public async Task<IEnumerable<PaiementDto>> GetPaiementsByClientIdAsync(long clientId)
        {
            var paiements = await _paiementRepository.GetPaiementsByClientIdAsync(clientId);
            return _mapper.Map<IEnumerable<PaiementDto>>(paiements);
        }

        public async Task<IEnumerable<PaiementDto>> GetAllAsync()
        {
            var paiements = await _paiementRepository.GetAllWithAbonnementAsync();
            return _mapper.Map<IEnumerable<PaiementDto>>(paiements);
        }
    }

}
