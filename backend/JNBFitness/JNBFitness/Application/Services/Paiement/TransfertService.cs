using AutoMapper;
using JNBFitness.Application.DTOs.Paiement;
using JNBFitness.Application.DTOs.Notification;
using JNBFitness.Application.Services.Notification;
using JNBFitness.Domain.Entities;
using JNBFitness.Domain.Enums;
using JNBFitness.Infrastructure.Repositories;

namespace JNBFitness.Application.Services.Paiement
{
    public class TransfertService : ITransfertService
    {
        private readonly ITransfertRepository _transfertRepository;
        private readonly IEcritureLedgerRepository _ecritureLedgerRepository;
        private readonly ICarteRepository _carteRepository;
        private readonly IMapper _mapper;
        private readonly INotificationService _notificationService;

        public TransfertService(ITransfertRepository transfertRepository, IEcritureLedgerRepository ecritureLedgerRepository, ICarteRepository carteRepository, IMapper mapper, INotificationService notificationService)
        {
            _transfertRepository = transfertRepository;
            _ecritureLedgerRepository = ecritureLedgerRepository;
            _carteRepository = carteRepository;
            _mapper = mapper;
            _notificationService = notificationService;
        }

        public async Task<TransfertDto> CreateTransfertAsync(CreateTransfertDto createDto)
        {
            // Validation des cartes pour éviter les erreurs de contrainte FK
            var carteEmetteur = await _carteRepository.GetByIdAsync(createDto.EmetteurCarteId);
            if (carteEmetteur == null)
                throw new Exception($"Carte émettrice introuvable (id={createDto.EmetteurCarteId})");

            var carteRecepteur = await _carteRepository.GetByIdAsync(createDto.RecepteurCarteId);
            if (carteRecepteur == null)
                throw new Exception($"Carte réceptrice introuvable (id={createDto.RecepteurCarteId})");

            if (createDto.MontantEuro <= 0)
                throw new Exception("Le montant doit être supérieur à 0");

            var transfert = new Transfert
            {
                Reference = $"TR-{DateTime.Now.Ticks}",
                EmetteurCarteId = createDto.EmetteurCarteId,
                RecepteurCarteId = createDto.RecepteurCarteId,
                MontantCent = (long)(createDto.MontantEuro),
                Devise = createDto.Devise,
                Motif = createDto.Motif,
                DateTransfert = DateTime.Now,
                Statut = StatutTransfert.SUCCES
            };

            transfert = await _transfertRepository.AddAsync(transfert);

            try
            {
                // Créer les écritures ledger : DEBIT pour l’émetteur, CREDIT pour le récepteur
                var dateEcriture = DateTime.Now;
                var debit = new EcritureLedger
                {
                    TransfertId = transfert.Id,
                    CarteId = transfert.EmetteurCarteId,
                    Sens = SensEcriture.DEBIT,
                    MontantCent = transfert.MontantCent,
                    DateEcriture = dateEcriture
                };

                var credit = new EcritureLedger
                {
                    TransfertId = transfert.Id,
                    CarteId = transfert.RecepteurCarteId,
                    Sens = SensEcriture.CREDIT,
                    MontantCent = transfert.MontantCent,
                    DateEcriture = dateEcriture
                };

                await _ecritureLedgerRepository.AddAsync(debit);
                await _ecritureLedgerRepository.AddAsync(credit);

                await _notificationService.CreateNotificationAsync(new CreateNotificationDto
                {
                    DestinataireId = carteRecepteur.UtilisateurId,
                    Titre = "Transfert reçu",
                    Message = $"Vous avez reçu {createDto.MontantEuro} {createDto.Devise} de la carte {carteEmetteur.Numero}",
                    Type = "TRANSFERT_RECU"
                });
            }
            catch
            {
                // En cas d’échec des écritures, rollback du transfert pour cohérence
                await _transfertRepository.DeleteAsync(transfert);
                throw;
            }

            return _mapper.Map<TransfertDto>(transfert);
        }

        public async Task<IEnumerable<TransfertDto>> GetTransfertsByCarteIdAsync(long carteId)
        {
            var transferts = await _transfertRepository.GetTransfertsByCarteIdAsync(carteId);
            return _mapper.Map<IEnumerable<TransfertDto>>(transferts);
        }
    }
}
