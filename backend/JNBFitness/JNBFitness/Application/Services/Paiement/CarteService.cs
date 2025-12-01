using AutoMapper;
using JNBFitness.Application.DTOs.Paiement;
using JNBFitness.Application.DTOs.Notification;
using JNBFitness.Application.Services.Notification;
using JNBFitness.Infrastructure.Repositories;

namespace JNBFitness.Application.Services.Paiement
{
    public class CarteService : ICarteService
    {
        private readonly ICarteRepository _carteRepository;
        private readonly IMapper _mapper;
        private readonly INotificationService _notificationService;

        public CarteService(ICarteRepository carteRepository, IMapper mapper, INotificationService notificationService)
        {
            _carteRepository = carteRepository;
            _mapper = mapper;
            _notificationService = notificationService;
        }

        public async Task<CarteDto> GetCarteByUtilisateurIdAsync(long utilisateurId)
        {
            var carte = await _carteRepository.GetByUtilisateurIdAsync(utilisateurId);
            if (carte == null)
                throw new Exception("Carte introuvable");

            return _mapper.Map<CarteDto>(carte);
        }

        public async Task<CarteDto> RechargerCarteAsync(long carteId, decimal montantEuro)
        {
            var carte = await _carteRepository.GetByIdAsync(carteId);
            if (carte == null)
                throw new Exception("Carte introuvable");

            carte.SoldeCent += (long)(montantEuro * 100);
            carte.DateMiseAJour = DateTime.Now;

            await _carteRepository.UpdateAsync(carte);
            await _notificationService.CreateNotificationAsync(new CreateNotificationDto
            {
                DestinataireId = carte.UtilisateurId,
                Titre = "Recharge de carte",
                Message = $"Votre carte a été rechargée de {montantEuro} EUR",
                Type = "RECHARGE_CARTE"
            });
            return _mapper.Map<CarteDto>(carte);
        }

        public async Task<IEnumerable<CarteDto>> GetAllAsync()
        {
            var cartes = await _carteRepository.GetAllAsync();
            return _mapper.Map<IEnumerable<CarteDto>>(cartes);
        }

        public async Task<CarteDto> DiminuerMontantCarteAsync(long carteId, decimal montantEuro)
        {
            var carte = await _carteRepository.GetByIdAsync(carteId);
            if (carte == null)
                throw new Exception("Carte introuvable");

            var montantCent = (long)(montantEuro * 100); // Convertir en centimes
            if (carte.SoldeCent < montantCent)
                throw new Exception("Solde insuffisant");

            carte.SoldeCent -= montantCent;
            carte.DateMiseAJour = DateTime.Now;

            await _carteRepository.UpdateAsync(carte);
            return _mapper.Map<CarteDto>(carte);
        }

        public async Task<CarteDto> TransfererMontantAsync(long carteSourceId, long carteDestinationId, decimal montantEuro, string Devise)
        {
            var carteSource = await _carteRepository.GetByIdAsync(carteSourceId);
            var carteDestination = await _carteRepository.GetByIdAsync(carteDestinationId);

            if (carteSource == null || carteDestination == null)
                throw new Exception("Une des cartes est introuvable");

            var montantCent = (long)(montantEuro * 100); // Convertir en centimes
            if (carteSource.SoldeCent < montantCent)
                throw new Exception("Solde insuffisant sur la carte source");

            // Effectuer le transfert
            carteSource.SoldeCent -= montantCent;
            carteDestination.SoldeCent += montantCent;
            
            carteSource.DateMiseAJour = DateTime.Now;
            carteDestination.DateMiseAJour = DateTime.Now;

            await _carteRepository.UpdateAsync(carteSource);
            await _carteRepository.UpdateAsync(carteDestination);

            return _mapper.Map<CarteDto>(carteSource);
        }
    }
}
