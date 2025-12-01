using System;
using System.Threading.Tasks;
using NUnit.Framework;
using NSubstitute;
using AutoMapper;
using JNBFitness.Application.Mapping;
using JNBFitness.Application.Services.Parrainage;
using JNBFitness.Infrastructure.Repositories;
using JNBFitness.Application.Services.Notification;

namespace JNBFitness.Tests
{
    public class ParrainageServiceTests
    {
        private IMapper _mapper;
        private IParrainageRepository _parrRepo;
        private IClientRepository _clientRepo;
        private INotificationService _notif;
        private ParrainageService _service;

        [SetUp]
        public void Setup()
        {
            _mapper = new MapperConfiguration(cfg => cfg.AddProfile(new MappingProfile())).CreateMapper();
            _parrRepo = Substitute.For<IParrainageRepository>();
            _clientRepo = Substitute.For<IClientRepository>();
            _notif = Substitute.For<INotificationService>();
            _service = new ParrainageService(_parrRepo, _clientRepo, _mapper, _notif);
        }

        [Test]
        public async Task ValiderParrainageAsync_WhenNotFound_ShouldReturnFalse()
        {
            _parrRepo.GetByIdAsync(1).Returns(Task.FromResult<JNBFitness.Domain.Entities.Parrainage>(default!));
            var result = await _service.ValiderParrainageAsync(1);
            Assert.That(result, Is.False);
        }

        [Test]
        public void ValiderParrainageAsync_WhenAlreadyValidated_ShouldThrow()
        {
            // Arrange: parrainage déjà validé
            var p = new JNBFitness.Domain.Entities.Parrainage { Id = 5, ParrainId = 1, FilleulId = 2, Valide = true };
            _parrRepo.GetByIdAsync(5).Returns(p);

            // Act/Assert
            Assert.ThrowsAsync<Exception>(async () => await _service.ValiderParrainageAsync(5));
        }

        [Test]
        public async Task CreateParrainageAsync_Success_CreatesEntry()
        {
            // Arrange: parrainage inexistant, parrain et filleul valides
            var dto = new JNBFitness.Application.DTOs.Parrainage.CreateParrainageDto { ParrainId = 10, FilleulId = 11 };
            _parrRepo.ExistsParrainageAsync(10, 11).Returns(false);
            _clientRepo.GetByIdAsync(10).Returns(new JNBFitness.Domain.Entities.Client { UtilisateurId = 10 });
            _clientRepo.GetByIdAsync(11).Returns(new JNBFitness.Domain.Entities.Client { UtilisateurId = 11 });

            // Act
            var ok = await _service.CreateParrainageAsync(dto);

            // Assert
            Assert.That(ok, Is.True);
            await _parrRepo.Received(1).AddAsync(Arg.Any<JNBFitness.Domain.Entities.Parrainage>());
        }
    }
}
