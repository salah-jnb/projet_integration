using System;
using System.Threading.Tasks;
using NUnit.Framework;
using NSubstitute;
using AutoMapper;
using JNBFitness.Application.Mapping;
using JNBFitness.Application.Services.Paiement;
using JNBFitness.Infrastructure.Repositories;
using JNBFitness.Application.Services.Notification;

namespace JNBFitness.Tests
{
    public class CarteServiceTests
    {
        private IMapper _mapper;
        private ICarteRepository _repo;
        private INotificationService _notif;
        private CarteService _service;

        [SetUp]
        public void Setup()
        {
            _mapper = new MapperConfiguration(cfg => cfg.AddProfile(new MappingProfile())).CreateMapper();
            _repo = Substitute.For<ICarteRepository>();
            _notif = Substitute.For<INotificationService>();
            _service = new CarteService(_repo, _mapper, _notif);
        }

        [Test]
        public void DiminuerMontantCarteAsync_WhenInsufficient_ShouldThrow()
        {
            var carte = new JNBFitness.Domain.Entities.Carte { Id = 1, SoldeCent = 100, Numero = "TEST-0001", Libelle = "Carte Test", Devise = "TND" };
            _repo.GetByIdAsync(carte.Id).Returns(carte);
            Assert.ThrowsAsync<Exception>(async () => await _service.DiminuerMontantCarteAsync(carte.Id, 2m));
        }

        [Test]
        public async Task RechargerCarteAsync_Success_UpdatesSoldeAndNotifies()
        {
            // Arrange: carte existante
            var carte = new JNBFitness.Domain.Entities.Carte { Id = 2, UtilisateurId = 10, SoldeCent = 0, Numero = "C-2", Libelle = "Carte 2", Devise = "TND" };
            _repo.GetByIdAsync(carte.Id).Returns(carte);

            // Act: recharge de 3.5 EUR
            var dto = await _service.RechargerCarteAsync(carte.Id, 3.5m);

            // Assert: solde mis à jour et notification envoyée
            Assert.That(dto.SoldeCent, Is.EqualTo(350));
            await _repo.Received(1).UpdateAsync(Arg.Is<JNBFitness.Domain.Entities.Carte>(c => c.SoldeCent == 350));
            await _notif.Received(1).CreateNotificationAsync(Arg.Any<JNBFitness.Application.DTOs.Notification.CreateNotificationDto>());
        }
    }
}
