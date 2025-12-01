using System;
using System.Linq;
using System.Threading.Tasks;
using NUnit.Framework;
using NSubstitute;
using AutoMapper;
using JNBFitness.Application.Mapping;
using JNBFitness.Application.Services.Reservation;
using JNBFitness.Infrastructure.Repositories;
using JNBFitness.Application.Services.Notification;

namespace JNBFitness.Tests
{
    public class ReservationCoursServiceTests
    {
        private IMapper _mapper;
        private IReservationCoursCollectifRepository _resRepo;
        private ISeanceCoursCollectifRepository _seanceRepo;
        private IAbonnementRepository _abRepo;
        private ICoursCollectifRepository _coursRepo;
        private INotificationService _notif;
        private ReservationCoursService _service;

        [SetUp]
        public void Setup()
        {
            _mapper = new MapperConfiguration(cfg => cfg.AddProfile(new MappingProfile())).CreateMapper();
            _resRepo = Substitute.For<IReservationCoursCollectifRepository>();
            _seanceRepo = Substitute.For<ISeanceCoursCollectifRepository>();
            _abRepo = Substitute.For<IAbonnementRepository>();
            _coursRepo = Substitute.For<ICoursCollectifRepository>();
            _notif = Substitute.For<INotificationService>();
            _service = new ReservationCoursService(_resRepo, _seanceRepo, _abRepo, _coursRepo, _mapper, _notif);
        }

        [Test]
        public void CreateReservationAsync_WhenNoActiveSubscription_ShouldThrow()
        {
            // Scenario: pas d'abonnement cours collectifs actif -> exception
            _abRepo.GetAbonnementsActifsAsync(1).Returns(Enumerable.Empty<JNBFitness.Domain.Entities.AbonnementClient>());
            var dto = new JNBFitness.Application.DTOs.Reservation.CreateReservationCoursDto { ClientId = 1, SeanceCoursCollectifId = 2 };
            Assert.ThrowsAsync<Exception>(async () => await _service.CreateReservationAsync(dto));
        }

        [Test]
        public void CreateReservationAsync_WhenAlreadyReserved_ShouldThrow()
        {
            // Scenario: réservation existante pour la même séance -> exception
            var ab = new JNBFitness.Domain.Entities.AbonnementClient { TypeAbonnement = new JNBFitness.Domain.Entities.TypeAbonnementConfig { Type = JNBFitness.Domain.Enums.TypeAbonnement.COURS_COLLECTIFS }, DateFin = DateTime.Now.AddDays(1) };
            _abRepo.GetAbonnementsActifsAsync(1).Returns(new[] { ab });
            _resRepo.FindAsync(Arg.Any<System.Linq.Expressions.Expression<Func<JNBFitness.Domain.Entities.ReservationCoursCollectif, bool>>>())
                .Returns(new[] { new JNBFitness.Domain.Entities.ReservationCoursCollectif { Id = 1 } });
            var dto = new JNBFitness.Application.DTOs.Reservation.CreateReservationCoursDto { ClientId = 1, SeanceCoursCollectifId = 2 };
            Assert.ThrowsAsync<Exception>(async () => await _service.CreateReservationAsync(dto));
        }
    }
}

