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
    public class TransfertServiceTests
    {
        private IMapper _mapper;
        private ITransfertRepository _transfertRepo;
        private IEcritureLedgerRepository _ledgerRepo;
        private ICarteRepository _carteRepo;
        private INotificationService _notif;
        private TransfertService _service;

        [SetUp]
        public void Setup()
        {
            _mapper = new MapperConfiguration(cfg => cfg.AddProfile(new MappingProfile())).CreateMapper();
            _transfertRepo = Substitute.For<ITransfertRepository>();
            _ledgerRepo = Substitute.For<IEcritureLedgerRepository>();
            _carteRepo = Substitute.For<ICarteRepository>();
            _notif = Substitute.For<INotificationService>();
            _service = new TransfertService(_transfertRepo, _ledgerRepo, _carteRepo, _mapper, _notif);
        }

        [Test]
        public void CreateTransfertAsync_WhenRecepteurMissing_ShouldThrow()
        {
            var dto = new JNBFitness.Application.DTOs.Paiement.CreateTransfertDto { EmetteurCarteId = 1, RecepteurCarteId = 2, MontantEuro = 10m, Devise = "TND", Motif = "Test" };
            _carteRepo.GetByIdAsync(dto.EmetteurCarteId).Returns(Task.FromResult(new JNBFitness.Domain.Entities.Carte { Id = 1, UtilisateurId = 10, Numero = "X", Libelle = "Carte Em", Devise = "TND" }));
            _carteRepo.GetByIdAsync(dto.RecepteurCarteId).Returns(Task.FromResult<JNBFitness.Domain.Entities.Carte>(default!));
            Assert.ThrowsAsync<Exception>(async () => await _service.CreateTransfertAsync(dto));
        }

        [Test]
        public async Task CreateTransfertAsync_Success_CreatesLedgerAndNotification()
        {
            // Arrange
            var dto = new JNBFitness.Application.DTOs.Paiement.CreateTransfertDto { EmetteurCarteId = 3, RecepteurCarteId = 4, MontantEuro = 1m, Devise = "TND", Motif = "Payment" };
            _carteRepo.GetByIdAsync(3).Returns(Task.FromResult(new JNBFitness.Domain.Entities.Carte { Id = 3, UtilisateurId = 30, Numero = "EM-3", Libelle = "E", Devise = "TND" }));
            _carteRepo.GetByIdAsync(4).Returns(Task.FromResult(new JNBFitness.Domain.Entities.Carte { Id = 4, UtilisateurId = 40, Numero = "RC-4", Libelle = "R", Devise = "TND" }));
            _transfertRepo.AddAsync(Arg.Any<JNBFitness.Domain.Entities.Transfert>()).Returns(ci => { var t = (JNBFitness.Domain.Entities.Transfert)ci[0]; t.Id = 77; return t; });

            // Act
            var res = await _service.CreateTransfertAsync(dto);

            // Assert: écritures et notification créées
            await _ledgerRepo.Received(1).AddAsync(Arg.Is<JNBFitness.Domain.Entities.EcritureLedger>(e => e.TransfertId == 77 && e.Sens == JNBFitness.Domain.Enums.SensEcriture.DEBIT));
            await _ledgerRepo.Received(1).AddAsync(Arg.Is<JNBFitness.Domain.Entities.EcritureLedger>(e => e.TransfertId == 77 && e.Sens == JNBFitness.Domain.Enums.SensEcriture.CREDIT));
            await _notif.Received(1).CreateNotificationAsync(Arg.Any<JNBFitness.Application.DTOs.Notification.CreateNotificationDto>());
        }

        [Test]
        public void CreateTransfertAsync_WhenMontantNonPositif_ShouldThrow()
        {
            // Arrange
            var dto = new JNBFitness.Application.DTOs.Paiement.CreateTransfertDto { EmetteurCarteId = 3, RecepteurCarteId = 4, MontantEuro = 0m, Devise = "TND", Motif = "Test" };
            _carteRepo.GetByIdAsync(3).Returns(Task.FromResult(new JNBFitness.Domain.Entities.Carte { Id = 3, UtilisateurId = 30, Numero = "EM-3", Libelle = "E", Devise = "TND" }));
            _carteRepo.GetByIdAsync(4).Returns(Task.FromResult(new JNBFitness.Domain.Entities.Carte { Id = 4, UtilisateurId = 40, Numero = "RC-4", Libelle = "R", Devise = "TND" }));

            // Act/Assert
            Assert.ThrowsAsync<Exception>(async () => await _service.CreateTransfertAsync(dto));
        }
    }
}
