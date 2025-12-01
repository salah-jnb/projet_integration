using AutoMapper;
using JNBFitness.Application.DTOs.Reservation;
using JNBFitness.Domain.Entities;
using JNBFitness.Domain.Enums;
using JNBFitness.Infrastructure.Repositories;
using JNBFitness.Application.DTOs.Notification;
using JNBFitness.Application.Services.Notification;

namespace JNBFitness.Application.Services.Reservation
{
    public class ReservationCoursService : IReservationCoursService
    {
        private readonly IReservationCoursCollectifRepository _reservationRepository;
        private readonly ISeanceCoursCollectifRepository _seanceRepository;
        private readonly IAbonnementRepository _abonnementRepository;
        private readonly ICoursCollectifRepository _coursRepository;
        private readonly IMapper _mapper;
        private readonly INotificationService _notificationService;

        public ReservationCoursService(IReservationCoursCollectifRepository reservationRepository, ISeanceCoursCollectifRepository seanceRepository, IAbonnementRepository abonnementRepository, ICoursCollectifRepository coursRepository, IMapper mapper, INotificationService notificationService)
        {
            _reservationRepository = reservationRepository;
            _seanceRepository = seanceRepository;
            _abonnementRepository = abonnementRepository;
            _coursRepository = coursRepository;
            _mapper = mapper;
            _notificationService = notificationService;
        }

        public async Task<ReservationCoursCollectifDto> CreateReservationAsync(CreateReservationCoursDto createDto)
        {
            var abonnementsActifs = await _abonnementRepository.GetAbonnementsActifsAsync(createDto.ClientId);
            var hasCoursCollectifs = abonnementsActifs.Any(a =>
                a.TypeAbonnement.Type == TypeAbonnement.COURS_COLLECTIFS &&
                (!a.DateFin.HasValue || a.DateFin.Value >= DateTime.Now));

            if (!hasCoursCollectifs)
                throw new Exception("Un abonnement cours collectifs actif est requis pour réserver");

            // Interdire toute nouvelle réservation si une réservation existe déjà pour cette séance (même annulée)
            var existing = await _reservationRepository.FindAsync(r =>
                r.ClientId == createDto.ClientId &&
                r.SeanceCoursCollectifId == createDto.SeanceCoursCollectifId);

            if (existing.Any())
                throw new Exception("Vous avez déjà réservé (ou annulé) cette séance");

            // Validate session availability
            var seance = await _seanceRepository.GetByIdAsync(createDto.SeanceCoursCollectifId);
            if (seance == null)
                throw new Exception("Séance introuvable");
            if (seance.Annulee)
                throw new Exception("Séance annulée");
            if (seance.DateSeance < DateTime.Now)
                throw new Exception("Séance passée");
            if (seance.PlacesDisponibles <= 0)
                throw new Exception("Plus de places disponibles");

            var reservation = new ReservationCoursCollectif
            {
                ClientId = createDto.ClientId,
                SeanceCoursCollectifId = createDto.SeanceCoursCollectifId,
                Statut = StatutReservation.CONFIRMEE,
                DateReservation = DateTime.Now,
                DelaiAnnulationHeures = createDto.DelaiAnnulationHeures
            };

            reservation = await _reservationRepository.AddAsync(reservation);
            // Decrement available places for the session
            seance.PlacesDisponibles = Math.Max(0, seance.PlacesDisponibles - 1);
            await _seanceRepository.UpdateAsync(seance);
            await _notificationService.CreateNotificationAsync(new CreateNotificationDto
            {
                DestinataireId = reservation.ClientId,
                Titre = "Réservation confirmée",
                Message = "Votre réservation au cours collectif est confirmée",
                Type = "RESERVATION_COURS_CONFIRMEE"
            });
            var cours = await _coursRepository.GetByIdAsync(seance.CoursCollectifId);
            await _notificationService.CreateNotificationAsync(new CreateNotificationDto
            {
                DestinataireId = cours.CoachId,
                Titre = "Nouvelle réservation cours collectif",
                Message = $"Une réservation a été effectuée pour le {seance.DateSeance:dd/MM/yyyy HH:mm}",
                Type = "RESERVATION_COURS"
            });
            return _mapper.Map<ReservationCoursCollectifDto>(reservation);
        }

        public async Task<IEnumerable<ReservationCoursCollectifDto>> GetReservationsByClientIdAsync(long clientId)
        {
            var reservations = await _reservationRepository.GetReservationsByClientIdAsync(clientId);
            return _mapper.Map<IEnumerable<ReservationCoursCollectifDto>>(reservations);
        }

        public async Task<IEnumerable<ReservationCoursCollectifDto>> GetReservationsBySeanceIdAsync(long seanceId)
        {
            var reservations = await _reservationRepository.GetReservationsBySeanceIdAsync(seanceId);
            return _mapper.Map<IEnumerable<ReservationCoursCollectifDto>>(reservations);
        }

        public async Task<bool> CancelReservationAsync(long reservationId)
        {
            var reservation = await _reservationRepository.GetByIdAsync(reservationId);
            if (reservation == null)
                throw new Exception("Réservation introuvable");

            reservation.Statut = StatutReservation.ANNULEE;
            await _reservationRepository.UpdateAsync(reservation);
            await _notificationService.CreateNotificationAsync(new CreateNotificationDto
            {
                DestinataireId = reservation.ClientId,
                Titre = "Réservation annulée",
                Message = "Votre réservation au cours collectif a été annulée",
                Type = "RESERVATION_COURS_ANNULEE"
            });

            return true;
        }
    }
}
