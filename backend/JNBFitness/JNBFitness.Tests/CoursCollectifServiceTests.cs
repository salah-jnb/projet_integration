using System;
using System.Threading.Tasks;
using NUnit.Framework;
using NSubstitute;
using AutoMapper;
using JNBFitness.Application.Mapping;
using JNBFitness.Application.Services.Cours;
using JNBFitness.Infrastructure.Repositories;

namespace JNBFitness.Tests
{
    public class CoursCollectifServiceTests
    {
        private IMapper _mapper;
        private ICoursCollectifRepository _repo;
        private CoursCollectifService _service;

        [SetUp]
        public void Setup()
        {
            _mapper = new MapperConfiguration(cfg => cfg.AddProfile(new MappingProfile())).CreateMapper();
            _repo = Substitute.For<ICoursCollectifRepository>();
            _service = new CoursCollectifService(_repo, _mapper);
        }

        [Test]
        public void GetCoursByIdAsync_WhenNotFound_ShouldThrow()
        {
            _repo.GetByIdAsync(1).Returns(Task.FromResult<JNBFitness.Domain.Entities.CoursCollectif>(default!));
            Assert.ThrowsAsync<Exception>(async () => await _service.GetCoursByIdAsync(1));
        }

        [Test]
        public async Task CreateCoursAsync_Success_SetsActifTrue()
        {
            // Arrange
            _repo.AddAsync(Arg.Any<JNBFitness.Domain.Entities.CoursCollectif>())
                .Returns(ci => (JNBFitness.Domain.Entities.CoursCollectif)ci[0]);
            var dto = new JNBFitness.Application.DTOs.Cours.CreateCoursCollectifDto { Nom = "Cardio", Description = "Desc" };

            // Act
            var res = await _service.CreateCoursAsync(dto);

            // Assert
            Assert.That(res.Actif, Is.True);
        }

        [Test]
        public async Task DeleteCoursAsync_Success_ReturnsTrue()
        {
            // Arrange
            var cours = new JNBFitness.Domain.Entities.CoursCollectif { Id = 99, Nom = "X" };
            _repo.GetByIdAsync(99).Returns(cours);

            // Act
            var ok = await _service.DeleteCoursAsync(99);

            // Assert
            Assert.That(ok, Is.True);
            await _repo.Received(1).DeleteAsync(cours);
        }
    }
}
