using AutoMapper;
using JNBFitness.Application.DTOs.Cours;
using JNBFitness.Domain.Entities;
using JNBFitness.Infrastructure.Repositories;
using System.Collections.Generic;
using System.Threading.Tasks;
using JNBFitness.Application.DTOs.Notification;
using JNBFitness.Application.Services.Notification;

namespace JNBFitness.Application.Services.Cours
{
    public class SeanceCoursCollectifService : ISeanceCoursCollectifService
    {
        private readonly ISeanceCoursCollectifRepository _repo;
        private readonly ICoursCollectifRepository _coursRepo;
        private readonly IReservationCoursCollectifRepository _reservationRepo;
        private readonly IMapper _mapper;
        private readonly INotificationService _notificationService;

        public SeanceCoursCollectifService(ISeanceCoursCollectifRepository repo, ICoursCollectifRepository coursRepo, IReservationCoursCollectifRepository reservationRepo, IMapper mapper, INotificationService notificationService)
        {
            _repo = repo;
            _coursRepo = coursRepo;
            _reservationRepo = reservationRepo;
            _mapper = mapper;
            _notificationService = notificationService;
        }

        public async Task<IEnumerable<SeanceCoursCollectifDto>> GetSeancesDisponiblesAsync()
        {
            var seances = await _repo.GetSeancesDisponiblesAsync();
            return _mapper.Map<IEnumerable<SeanceCoursCollectifDto>>(seances);
        }

        public async Task<IEnumerable<SeanceCoursCollectifDto>> GetSeancesDisponiblesByCoachIdAsync(long coachId)
        {
            var seances = await _repo.GetSeancesDisponiblesByCoachIdAsync(coachId);
            return _mapper.Map<IEnumerable<SeanceCoursCollectifDto>>(seances);
        }

        public async Task<SeanceCoursCollectifDto> CreateSeanceAsync(CreateSeanceCoursCollectifDto createDto)
        {
            var cours = await _coursRepo.GetByIdAsync(createDto.CoursCollectifId);
            if (cours == null)
                throw new Exception("Cours introuvable");

            if (createDto.DateSeance < DateTime.Now)
                throw new Exception("La séance doit être programmée dans le futur");

            var seance = new SeanceCoursCollectif
            {
                CoursCollectifId = createDto.CoursCollectifId,
                DateSeance = createDto.DateSeance,
                PlacesDisponibles = createDto.PlacesDisponibles,
                Annulee = false
            };

            seance = await _repo.AddAsync(seance);
            await _notificationService.CreateNotificationAsync(new CreateNotificationDto
            {
                DestinataireId = cours.CoachId,
                Titre = "Séance planifiée",
                Message = $"Séance du cours {cours.Nom} le {seance.DateSeance:dd/MM/yyyy HH:mm}",
                Type = "SEANCE_PLANIFIEE"
            });
            return _mapper.Map<SeanceCoursCollectifDto>(seance);
        }

        public async Task<IEnumerable<SeanceCoursCollectifDto>> GetAllSeancesAsync()
        {
            var seances = await _repo.GetAllWithCoursAsync();
            return _mapper.Map<IEnumerable<SeanceCoursCollectifDto>>(seances);
        }

        public async Task<SeanceCoursCollectifDto> UpdateSeanceAsync(long id, UpdateSeanceCoursCollectifDto updateDto)
        {
            var seance = await _repo.GetByIdAsync(id);
            if (seance == null)
                throw new Exception("Séance introuvable");

            if (updateDto.DateSeance.HasValue)
            {
                if (updateDto.DateSeance.Value < DateTime.Now)
                    throw new Exception("La séance doit être programmée dans le futur");
                seance.DateSeance = updateDto.DateSeance.Value;
            }
            if (updateDto.PlacesDisponibles.HasValue)
            {
                seance.PlacesDisponibles = Math.Max(0, updateDto.PlacesDisponibles.Value);
            }
            if (updateDto.Annulee.HasValue)
            {
                seance.Annulee = updateDto.Annulee.Value;
            }

            await _repo.UpdateAsync(seance);
            return _mapper.Map<SeanceCoursCollectifDto>(seance);
        }

        public async Task<bool> AnnulerSeanceAsync(long id)
        {
            var seance = await _repo.GetByIdAsync(id);
            if (seance == null)
                throw new Exception("Séance introuvable");
            seance.Annulee = true;
            await _repo.UpdateAsync(seance);
            return true;
        }

        public async Task<bool> DeleteSeanceAsync(long id)
        {
            var seance = await _repo.GetByIdAsync(id);
            if (seance == null)
                throw new Exception("Séance introuvable");
            var reservations = await _reservationRepo.GetReservationsBySeanceIdAsync(id);
            if (reservations.Any())
                throw new Exception("Suppression impossible: des réservations existent. Annulez la séance.");
            await _repo.DeleteAsync(seance);
            return true;
        }
    }
}