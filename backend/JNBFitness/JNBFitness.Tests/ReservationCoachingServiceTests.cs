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
    public class ReservationCoachingServiceTests
    {
        private IMapper _mapper;
        private IReservationCoachingRepository _resRepo;
        private IClientRepository _clientRepo;
        private ICoachRepository _coachRepo;
        private IAbonnementRepository _abRepo;
        private INotificationService _notif;
        private ReservationCoachingService _service;

        [SetUp]
        public void Setup()
        {
            _mapper = new MapperConfiguration(cfg => cfg.AddProfile(new MappingProfile())).CreateMapper();
            _resRepo = Substitute.For<IReservationCoachingRepository>();
            _clientRepo = Substitute.For<IClientRepository>();
            _coachRepo = Substitute.For<ICoachRepository>();
            _abRepo = Substitute.For<IAbonnementRepository>();
            _notif = Substitute.For<INotificationService>();
            _service = new ReservationCoachingService(_resRepo, _clientRepo, _coachRepo, _abRepo, _mapper, _notif);
        }

        [Test]
        public void CreateReservationAsync_WhenNoSessionsLeft_ShouldThrow()
        {
            // Scenario: aucun abonnement avec séances restantes -> exception
            _clientRepo.GetByIdAsync(1).Returns(new JNBFitness.Domain.Entities.Client { UtilisateurId = 1 });
            _coachRepo.GetByIdAsync(2).Returns(new JNBFitness.Domain.Entities.Coach { UtilisateurId = 2 });
            _abRepo.GetAbonnementsActifsAsync(1).Returns(Enumerable.Empty<JNBFitness.Domain.Entities.AbonnementClient>());
            var dto = new JNBFitness.Application.DTOs.Reservation.CreateReservationCoachingDto { ClientId = 1, CoachId = 2, DateSeance = DateTime.Now.AddHours(2), TypeSeance = "SEANCE_UNIQUE" };
            Assert.ThrowsAsync<Exception>(async () => await _service.CreateReservationAsync(dto));
        }

        [Test]
        public async Task CancelReservationAsync_Success_ReturnsTrue()
        {
            // Scenario: annulation réussie
            var r = new JNBFitness.Domain.Entities.ReservationCoaching { Id = 3, ClientId = 1, DateSeance = DateTime.Now.AddHours(2), Statut = JNBFitness.Domain.Enums.StatutReservation.EN_ATTENTE };
            _resRepo.GetByIdAsync(3).Returns(r);
            var ok = await _service.CancelReservationAsync(3);
            Assert.That(ok, Is.True);
            await _resRepo.Received(1).UpdateAsync(Arg.Is<JNBFitness.Domain.Entities.ReservationCoaching>(x => x.Statut == JNBFitness.Domain.Enums.StatutReservation.ANNULEE));
        }
    }
}

