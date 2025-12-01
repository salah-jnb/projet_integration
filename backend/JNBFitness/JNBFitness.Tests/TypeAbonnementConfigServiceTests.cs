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
    public class TypeAbonnementConfigServiceTests
    {
        private IMapper _mapper;
        private ITypeAbonnementConfigRepository _repo;
        private TypeAbonnementConfigService _service;

        [SetUp]
        public void Setup()
        {
            _mapper = new MapperConfiguration(cfg => cfg.AddProfile(new MappingProfile())).CreateMapper();
            _repo = Substitute.For<ITypeAbonnementConfigRepository>();
            _service = new TypeAbonnementConfigService(_repo, _mapper);
        }

        [Test]
        public void GetTypeByIdAsync_WhenNotFound_ShouldThrow()
        {
            // Scenario: type introuvable -> exception
            _repo.GetByIdAsync(1).Returns(Task.FromResult<JNBFitness.Domain.Entities.TypeAbonnementConfig>(default!));
            Assert.ThrowsAsync<Exception>(async () => await _service.GetTypeByIdAsync(1));
        }

        [Test]
        public void CreateTypeAsync_WhenTypeInvalid_ShouldThrow()
        {
            // Scenario: type invalide (ParseType échoue) -> exception
            var dto = new JNBFitness.Application.DTOs.Abonnement.CreateTypeAbonnementConfigDto { Type = "INVALIDE", Nom = "X" };
            Assert.ThrowsAsync<Exception>(async () => await _service.CreateTypeAsync(dto));
        }

        [Test]
        public async Task UpdateTypeAsync_Success_UpdatesFields()
        {
            // Scenario: mise à jour champs
            var entity = new JNBFitness.Domain.Entities.TypeAbonnementConfig { Id = 2, Nom = "Ancien", Actif = true };
            _repo.GetByIdAsync(2).Returns(entity);
            var upd = new JNBFitness.Application.DTOs.Abonnement.UpdateTypeAbonnementConfigDto { Nom = "Nouveau", Actif = false };
            var res = await _service.UpdateTypeAsync(2, upd);
            Assert.That(res.Nom, Is.EqualTo("Nouveau"));
        }
    }
}

