using System;
using System.Threading.Tasks;
using NUnit.Framework;
using NSubstitute;
using AutoMapper;
using JNBFitness.Application.Mapping;
using JNBFitness.Application.Services.Cours;
using JNBFitness.Infrastructure.Repositories;
using JNBFitness.Application.Services.Notification;

namespace JNBFitness.Tests
{
    public class SeanceCoursCollectifServiceTests
    {
        private IMapper _mapper;
        private ISeanceCoursCollectifRepository _repo;
        private ICoursCollectifRepository _coursRepo;
        private IReservationCoursCollectifRepository _reservationRepo;
        private INotificationService _notif;
        private SeanceCoursCollectifService _service;

        [SetUp]
        public void Setup()
        {
            _mapper = new MapperConfiguration(cfg => cfg.AddProfile(new MappingProfile())).CreateMapper();
            _repo = Substitute.For<ISeanceCoursCollectifRepository>();
            _coursRepo = Substitute.For<ICoursCollectifRepository>();
            _reservationRepo = Substitute.For<IReservationCoursCollectifRepository>();
            _notif = Substitute.For<INotificationService>();
            _service = new SeanceCoursCollectifService(_repo, _coursRepo, _reservationRepo, _mapper, _notif);
        }

        [Test]
        public void CreateSeanceAsync_WhenCoursMissing_ShouldThrow()
        {
            var dto = new JNBFitness.Application.DTOs.Cours.CreateSeanceCoursCollectifDto { CoursCollectifId = 1, DateSeance = DateTime.Now.AddDays(1), PlacesDisponibles = 10 };
            _coursRepo.GetByIdAsync(dto.CoursCollectifId).Returns(Task.FromResult<JNBFitness.Domain.Entities.CoursCollectif>(default!));
            Assert.ThrowsAsync<Exception>(async () => await _service.CreateSeanceAsync(dto));
        }

        [Test]
        public async Task CreateSeanceAsync_Success_TriggersNotification()
        {
            // Arrange
            var dto = new JNBFitness.Application.DTOs.Cours.CreateSeanceCoursCollectifDto { CoursCollectifId = 2, DateSeance = DateTime.Now.AddDays(2), PlacesDisponibles = 12 };
            _coursRepo.GetByIdAsync(2).Returns(new JNBFitness.Domain.Entities.CoursCollectif { Id = 2, CoachId = 50, Nom = "Boxe" });
            _repo.AddAsync(Arg.Any<JNBFitness.Domain.Entities.SeanceCoursCollectif>())
                .Returns(ci => (JNBFitness.Domain.Entities.SeanceCoursCollectif)ci[0]);

            // Act
            var res = await _service.CreateSeanceAsync(dto);

            // Assert
            Assert.That(res.PlacesDisponibles, Is.EqualTo(12));
            await _notif.Received(1).CreateNotificationAsync(Arg.Any<JNBFitness.Application.DTOs.Notification.CreateNotificationDto>());
        }

        [Test]
        public void UpdateSeanceAsync_WhenPastDate_ShouldThrow()
        {
            // Arrange
            _repo.GetByIdAsync(3).Returns(new JNBFitness.Domain.Entities.SeanceCoursCollectif { Id = 3, DateSeance = DateTime.Now.AddDays(3) });
            var update = new JNBFitness.Application.DTOs.Cours.UpdateSeanceCoursCollectifDto { DateSeance = DateTime.Now.AddDays(-1) };

            // Act/Assert
            Assert.ThrowsAsync<Exception>(async () => await _service.UpdateSeanceAsync(3, update));
        }

        [Test]
        public async Task DeleteSeanceAsync_WhenReservationsExist_ShouldThrow()
        {
            // Arrange
            _repo.GetByIdAsync(4).Returns(new JNBFitness.Domain.Entities.SeanceCoursCollectif { Id = 4 });
            _reservationRepo.GetReservationsBySeanceIdAsync(4).Returns(new[] { new JNBFitness.Domain.Entities.ReservationCoursCollectif { Id = 100 } });

            // Act/Assert
            Assert.ThrowsAsync<Exception>(async () => await _service.DeleteSeanceAsync(4));
        }

        [Test]
        public async Task AnnulerSeanceAsync_Success_SetsAnnuleeTrue()
        {
            // Arrange
            var s = new JNBFitness.Domain.Entities.SeanceCoursCollectif { Id = 5, Annulee = false };
            _repo.GetByIdAsync(5).Returns(s);

            // Act
            var ok = await _service.AnnulerSeanceAsync(5);

            // Assert
            Assert.That(ok, Is.True);
            await _repo.Received(1).UpdateAsync(Arg.Is<JNBFitness.Domain.Entities.SeanceCoursCollectif>(x => x.Annulee));
        }
    }
}
