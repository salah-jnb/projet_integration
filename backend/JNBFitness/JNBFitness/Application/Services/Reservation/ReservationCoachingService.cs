using AutoMapper;
using JNBFitness.Application.DTOs.Reservation;
using JNBFitness.Domain.Entities;
using JNBFitness.Domain.Enums;
using JNBFitness.Infrastructure.Repositories;
using JNBFitness.Application.DTOs.Notification;
using JNBFitness.Application.Services.Notification;

namespace JNBFitness.Application.Services.Reservation
{
    public class ReservationCoachingService : IReservationCoachingService
    {
        private readonly IReservationCoachingRepository _reservationRepository;
        private readonly IClientRepository _clientRepository;
        private readonly ICoachRepository _coachRepository;
        private readonly IAbonnementRepository _abonnementRepository;
        private readonly IMapper _mapper;
        private readonly INotificationService _notificationService;

        public ReservationCoachingService(
            IReservationCoachingRepository reservationRepository,
            IClientRepository clientRepository,
            ICoachRepository coachRepository,
            IAbonnementRepository abonnementRepository,
            IMapper mapper,
            INotificationService notificationService)
        {
            _reservationRepository = reservationRepository;
            _clientRepository = clientRepository;
            _coachRepository = coachRepository;
            _abonnementRepository = abonnementRepository;
            _mapper = mapper;
            _notificationService = notificationService;
        }

        public async Task<ReservationCoachingDto> CreateReservationAsync(CreateReservationCoachingDto createDto)
        {
            // Validate client exists
            var client = await _clientRepository.GetByIdAsync(createDto.ClientId);
            if (client == null)
                throw new Exception("Client introuvable");

            // Validate coach exists
            var coach = await _coachRepository.GetByIdAsync(createDto.CoachId);
            if (coach == null)
                throw new Exception("Coach introuvable");

            // Validate client has active subscription with remaining sessions (> 0)
            var activeAbonnements = await _abonnementRepository.GetAbonnementsActifsAsync(createDto.ClientId);
            var abonnementAvecSeances = activeAbonnements
                .Where(a => a.SeancesRestantes.HasValue && a.SeancesRestantes.Value > 0)
                .OrderBy(a => a.DateFin ?? DateTime.MaxValue)
                .FirstOrDefault();

            if (abonnementAvecSeances == null)
                throw new Exception("Le client n'a pas d'abonnement actif avec des séances disponibles");

            // Check if time slot is available (default duration: 60 minutes)
            var isAvailable = await IsTimeSlotAvailableAsync(createDto.CoachId, createDto.DateSeance, 60);
            if (!isAvailable)
                throw new Exception("Le créneau horaire n'est pas disponible");

            // Validate session is not in the past
            if (createDto.DateSeance < DateTime.Now.AddHours(1))
                throw new Exception("La séance doit être programmée au moins 1 heure à l'avance");

            var reservation = new ReservationCoaching
            {
                ClientId = createDto.ClientId,
                CoachId = createDto.CoachId,
                DateSeance = createDto.DateSeance,
                DureeMinutes = 60,
                TypeSeance = Enum.Parse<TypeSeance>(createDto.TypeSeance),
                Statut = StatutReservation.EN_ATTENTE,
                DateReservation = DateTime.Now,
                
            };

            reservation = await _reservationRepository.AddAsync(reservation);

            // Décrémenter une séance sur l'abonnement utilisé
            abonnementAvecSeances.SeancesRestantes = (abonnementAvecSeances.SeancesRestantes ?? 0) - 1;
            await _abonnementRepository.UpdateAsync(abonnementAvecSeances);
            await _notificationService.CreateNotificationAsync(new CreateNotificationDto
            {
                DestinataireId = reservation.CoachId,
                Titre = "Nouvelle réservation coaching",
                Message = $"Séance prévue le {reservation.DateSeance:dd/MM/yyyy HH:mm}",
                Type = "RESERVATION_COACHING"
            });
            return _mapper.Map<ReservationCoachingDto>(reservation);
        }

        public async Task<IEnumerable<ReservationCoachingDto>> GetReservationsByClientIdAsync(long clientId)
        {
            var reservations = await _reservationRepository.GetReservationsByClientIdAsync(clientId);
            return _mapper.Map<IEnumerable<ReservationCoachingDto>>(reservations);
        }

        public async Task<IEnumerable<ReservationCoachingDto>> GetReservationsByCoachIdAsync(long coachId)
        {
            var reservations = await _reservationRepository.GetReservationsByCoachIdAsync(coachId);
            return _mapper.Map<IEnumerable<ReservationCoachingDto>>(reservations);
        }

        public async Task<ReservationCoachingDto> GetReservationByIdAsync(long reservationId)
        {
            var reservation = await _reservationRepository.GetReservationWithDetailsAsync(reservationId);
            return _mapper.Map<ReservationCoachingDto>(reservation);
        }

        public async Task<bool> CancelReservationAsync(long reservationId)
        {
            var reservation = await _reservationRepository.GetByIdAsync(reservationId);
            if (reservation == null)
                return false;

            // Only allow cancellation if session is not completed and not in the past
            if (reservation.Statut == StatutReservation.TERMINEE)
                throw new Exception("Impossible d'annuler une séance déjà terminée");

            if (reservation.DateSeance < DateTime.Now)
                throw new Exception("Impossible d'annuler une séance passée");

            reservation.Statut = StatutReservation.ANNULEE;
            await _reservationRepository.UpdateAsync(reservation);
            await _notificationService.CreateNotificationAsync(new CreateNotificationDto
            {
                DestinataireId = reservation.ClientId,
                Titre = "Réservation annulée",
                Message = "Votre réservation de coaching a été annulée",
                Type = "RESERVATION_ANNULEE"
            });
            return true;
        }

        public async Task<bool> CompleteReservationAsync(long reservationId)
        {
            var reservation = await _reservationRepository.GetByIdAsync(reservationId);
            if (reservation == null)
                return false;

            // Only allow completion if session is confirmed and in the past
            if (reservation.Statut != StatutReservation.CONFIRMEE)
                throw new Exception("Seule une séance confirmée peut être marquée comme terminée");

            if (reservation.DateSeance > DateTime.Now.AddMinutes(30))
                throw new Exception("La séance ne peut être marquée comme terminée que 30 minutes après son heure prévue");

            reservation.Statut = StatutReservation.TERMINEE;
            await _reservationRepository.UpdateAsync(reservation);
            await _notificationService.CreateNotificationAsync(new CreateNotificationDto
            {
                DestinataireId = reservation.ClientId,
                Titre = "Rappel d'avis",
                Message = "Merci de laisser une note pour votre séance",
                Type = "RAPPEL_NOTATION"
            });
            return true;
        }

        public async Task<bool> ConfirmReservationAsync(long reservationId)
        {
            var reservation = await _reservationRepository.GetByIdAsync(reservationId);
            if (reservation == null)
                return false;

            if (reservation.Statut != StatutReservation.EN_ATTENTE)
                throw new Exception("Seule une réservation en attente peut être confirmée");

            if (reservation.DateSeance < DateTime.Now)
                throw new Exception("Impossible de confirmer une séance passée");

            reservation.Statut = StatutReservation.CONFIRMEE;
            await _reservationRepository.UpdateAsync(reservation);
            await _notificationService.CreateNotificationAsync(new CreateNotificationDto
            {
                DestinataireId = reservation.ClientId,
                Titre = "Réservation confirmée",
                Message = "Votre séance de coaching est confirmée",
                Type = "RESERVATION_CONFIRMEE"
            });
            return true;
        }

        public async Task<bool> IsTimeSlotAvailableAsync(long coachId, DateTime dateSeance, int dureeMinutes)
        {
            var reservations = await _reservationRepository.GetReservationsByCoachIdAsync(coachId);
            var conflictingReservations = reservations.Where(r => 
                r.Statut == StatutReservation.CONFIRMEE &&
                r.DateSeance.Date == dateSeance.Date &&
                r.DureeMinutes.HasValue &&
                ((dateSeance >= r.DateSeance && dateSeance < r.DateSeance.AddMinutes((double)r.DureeMinutes.Value)) ||
                 (dateSeance.AddMinutes(dureeMinutes) > r.DateSeance && dateSeance.AddMinutes(dureeMinutes) <= r.DateSeance.AddMinutes((double)r.DureeMinutes.Value)) ||
                 (dateSeance <= r.DateSeance && dateSeance.AddMinutes(dureeMinutes) >= r.DateSeance.AddMinutes((double)r.DureeMinutes.Value)))
            );

            return !conflictingReservations.Any();
        }
    }
}
