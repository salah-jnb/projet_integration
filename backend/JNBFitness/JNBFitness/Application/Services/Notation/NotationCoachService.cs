using AutoMapper;
using JNBFitness.Application.DTOs.Notation;
using JNBFitness.Domain.Entities;
using JNBFitness.Domain.Enums;
using JNBFitness.Infrastructure.Repositories;
using JNBFitness.Application.DTOs.Notification;
using JNBFitness.Application.Services.Notification;

namespace JNBFitness.Application.Services.Notation
{
    public class NotationCoachService : INotationCoachService
    {
        private readonly INotationCoachRepository _notationRepo;
        private readonly IReservationCoachingRepository _reservationRepo;
        private readonly ICoachRepository _coachRepo;
        private readonly IMapper _mapper;
        private readonly INotificationService _notificationService;

        public NotationCoachService(INotationCoachRepository notationRepo, IReservationCoachingRepository reservationRepo, ICoachRepository coachRepo, IMapper mapper, INotificationService notificationService)
        {
            _notationRepo = notationRepo;
            _reservationRepo = reservationRepo;
            _coachRepo = coachRepo;
            _mapper = mapper;
            _notificationService = notificationService;
        }

        public async Task<NotationCoachDto> CreateAsync(CreateNotationCoachDto dto)
        {
            var reservation = await _reservationRepo.GetByIdAsync(dto.ReservationCoachingId);
            if (reservation == null)
                throw new Exception("Réservation introuvable");
            if (reservation.Statut != StatutReservation.TERMINEE)
                throw new Exception("La réservation n'est pas terminée");
            if (reservation.ClientId != dto.ClientId || reservation.CoachId != dto.CoachId)
                throw new Exception("Incohérence des identifiants");

            var existing = await _notationRepo.GetByReservationIdAsync(dto.ReservationCoachingId);
            if (existing != null)
                throw new Exception("Vous avez déjà noté cette séance");

            var entity = new NotationCoach
            {
                ClientId = dto.ClientId,
                CoachId = dto.CoachId,
                ReservationCoachingId = dto.ReservationCoachingId,
                Note = dto.Note,
                Commentaire = dto.Commentaire ?? string.Empty,
                DateNotation = DateTime.Now
            };

            entity = await _notationRepo.AddAsync(entity);

            var coach = await _coachRepo.GetByIdAsync(dto.CoachId);
            if (coach != null)
            {
                var notes = (await _notationRepo.GetByCoachIdAsync(dto.CoachId)).ToList();
                coach.NombreAvis = notes.Count;
                coach.NoteGlobale = notes.Count == 0 ? 0 : (decimal)notes.Average(n => n.Note);
                await _coachRepo.UpdateAsync(coach);
            }

            await _notificationService.CreateNotificationAsync(new CreateNotificationDto
            {
                DestinataireId = dto.CoachId,
                Titre = "Nouvel avis reçu",
                Message = $"Vous avez reçu une note de {entity.Note}/5",
                Type = "AVIS_RECU"
            });

            return _mapper.Map<NotationCoachDto>(entity);
        }

        public async Task<IEnumerable<NotationCoachDto>> GetByCoachIdAsync(long coachId)
        {
            var list = await _notationRepo.GetByCoachIdAsync(coachId);
            return _mapper.Map<IEnumerable<NotationCoachDto>>(list);
        }

        public async Task<NotationCoachDto?> GetByReservationIdAsync(long reservationId)
        {
            var item = await _notationRepo.GetByReservationIdAsync(reservationId);
            return item == null ? null : _mapper.Map<NotationCoachDto>(item);
        }
    }
}