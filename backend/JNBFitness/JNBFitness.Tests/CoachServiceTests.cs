using System;
using System.Threading.Tasks;
using NUnit.Framework;
using NSubstitute;
using AutoMapper;
using JNBFitness.Application.Mapping;
using JNBFitness.Application.Services.Utilisateur;
using JNBFitness.Infrastructure.Repositories;

namespace JNBFitness.Tests
{
    public class CoachServiceTests
    {
        private IMapper _mapper;
        private ICoachRepository _repo;
        private CoachService _service;

        [SetUp]
        public void Setup()
        {
            _mapper = new MapperConfiguration(cfg => cfg.AddProfile(new MappingProfile())).CreateMapper();
            _repo = Substitute.For<ICoachRepository>();
            _service = new CoachService(_repo, _mapper);
        }

        [Test]
        public void GetByIdAsync_WhenNotFound_ShouldThrow()
        {
            _repo.GetByUtilisateurIdAsync(1).Returns(Task.FromResult<JNBFitness.Domain.Entities.Coach>(default!));
            Assert.ThrowsAsync<Exception>(async () => await _service.GetByIdAsync(1));
        }

        [Test]
        public async Task UpdateCoachProfileAsync_Success_UpdatesFields()
        {
            // Arrange: coach existant
            var coach = new JNBFitness.Domain.Entities.Coach { UtilisateurId = 9, Specialites = "", Description = "" };
            _repo.GetByUtilisateurIdAsync(coach.UtilisateurId).Returns(coach);

            // Act: mise à jour profil
            var dto = await _service.UpdateCoachProfileAsync(coach.UtilisateurId, "Yoga", "Coach pro");

            // Assert: champs mis à jour et Update appelé
            Assert.That(dto.Specialites, Is.EqualTo("Yoga"));
            Assert.That(dto.Description, Is.EqualTo("Coach pro"));
            await _repo.Received(1).UpdateAsync(Arg.Is<JNBFitness.Domain.Entities.Coach>(c => c.Specialites == "Yoga" && c.Description == "Coach pro"));
        }
    }
}
