using System;
using System.Threading.Tasks;
using NUnit.Framework;
using NSubstitute;
using AutoMapper;
using JNBFitness.Application.Mapping;
using JNBFitness.Application.Services.Abonnement;
using JNBFitness.Infrastructure.Repositories;

namespace JNBFitness.Tests
{
    public class AbonnementServiceTests
    {
        private IMapper _mapper;
        private IAbonnementRepository _abRepo;
        private ITypeAbonnementConfigRepository _typeRepo;
        private AbonnementService _service;

        [SetUp]
        public void Setup()
        {
            _mapper = new MapperConfiguration(cfg => cfg.AddProfile(new MappingProfile())).CreateMapper();
            _abRepo = Substitute.For<IAbonnementRepository>();
            _typeRepo = Substitute.For<ITypeAbonnementConfigRepository>();
            _service = new AbonnementService(_abRepo, _typeRepo, _mapper);
        }

        [Test]
        public void CreateAbonnementAsync_WhenTypeNotFound_ShouldThrow()
        {
            var dto = new JNBFitness.Application.DTOs.Abonnement.CreateAbonnementDto { TypeAbonnementId = 10, ClientId = 1, DateDebut = DateTime.Now };
            _typeRepo.GetByIdAsync(dto.TypeAbonnementId).Returns(Task.FromResult<JNBFitness.Domain.Entities.TypeAbonnementConfig>(default!));
            Assert.ThrowsAsync<Exception>(async () => await _service.CreateAbonnementAsync(dto));
        }

        [Test]
        public async Task CreateAbonnementAsync_Salle_SetsDateFin()
        {
            // Arrange: type SALLE avec durée en mois
            var dto = new JNBFitness.Application.DTOs.Abonnement.CreateAbonnementDto { TypeAbonnementId = 1, ClientId = 2, DateDebut = new DateTime(2025, 1, 1) };
            var type = new JNBFitness.Domain.Entities.TypeAbonnementConfig { Id = 1, Type = JNBFitness.Domain.Enums.TypeAbonnement.SALLE, DureeEnMois = 3 };
            _typeRepo.GetByIdAsync(1).Returns(type);
            _abRepo.AddAsync(Arg.Any<JNBFitness.Domain.Entities.AbonnementClient>()).Returns(ci => (JNBFitness.Domain.Entities.AbonnementClient)ci[0]);

            // Act
            var res = await _service.CreateAbonnementAsync(dto);

            // Assert: DateFin = DateDebut + 3 mois
            Assert.That(res.DateFin, Is.EqualTo(dto.DateDebut.AddMonths(3)));
        }

        [Test]
        public async Task CreateAbonnementAsync_Pack_SetsSeancesRestantes()
        {
            // Arrange: type PACK_COACHING_5 sans nombre explicite -> par défaut à 5
            var dto = new JNBFitness.Application.DTOs.Abonnement.CreateAbonnementDto { TypeAbonnementId = 5, ClientId = 2, DateDebut = DateTime.Now };
            var type = new JNBFitness.Domain.Entities.TypeAbonnementConfig { Id = 5, Type = JNBFitness.Domain.Enums.TypeAbonnement.PACK_COACHING_5 };
            _typeRepo.GetByIdAsync(5).Returns(type);
            _abRepo.AddAsync(Arg.Any<JNBFitness.Domain.Entities.AbonnementClient>())
                .Returns(ci => (JNBFitness.Domain.Entities.AbonnementClient)ci[0]);

            // Act
            var res = await _service.CreateAbonnementAsync(dto);

            // Assert
            Assert.That(res.SeancesRestantes, Is.EqualTo(5));
        }
    }
}
