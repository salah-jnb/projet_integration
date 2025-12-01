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
    public class DisponibiliteServiceTests
    {
        private IMapper _mapper;
        private IDisponibiliteCoachRepository _repo;
        private DisponibiliteService _service;

        [SetUp]
        public void Setup()
        {
            _mapper = new MapperConfiguration(cfg => cfg.AddProfile(new MappingProfile())).CreateMapper();
            _repo = Substitute.For<IDisponibiliteCoachRepository>();
            _service = new DisponibiliteService(_repo, _mapper);
        }

        [Test]
        public void UpdateAsync_WhenNotFound_ShouldThrow()
        {
            _repo.GetByIdAsync(1).Returns(Task.FromResult<JNBFitness.Domain.Entities.DisponibiliteCoach>(default!));
            var dto = new JNBFitness.Application.DTOs.Utilisateur.DisponibiliteCoachDto();
            Assert.ThrowsAsync<Exception>(async () => await _service.UpdateAsync(1, dto));
        }

        [Test]
        public async Task CreateAsync_Success_ReturnsDto()
        {
            // Arrange
            var input = new JNBFitness.Application.DTOs.Utilisateur.DisponibiliteCoachDto
            {
                JourSemaine = "Lundi",
                HeureDebut = new TimeSpan(9,0,0),
                HeureFin = new TimeSpan(11,0,0),
                Actif = true
            };
            _repo.AddAsync(Arg.Any<JNBFitness.Domain.Entities.DisponibiliteCoach>())
                .Returns(ci => (JNBFitness.Domain.Entities.DisponibiliteCoach)ci[0]);

            // Act
            var dto = await _service.CreateAsync(5, input);

            // Assert
            Assert.That(dto.JourSemaine, Is.EqualTo("Lundi"));
            await _repo.Received(1).AddAsync(Arg.Is<JNBFitness.Domain.Entities.DisponibiliteCoach>(d => d.CoachId == 5));
        }
    }
}
