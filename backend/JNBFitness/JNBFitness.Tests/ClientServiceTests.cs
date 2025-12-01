using System;
using System.Linq;
using System.Threading.Tasks;
using NUnit.Framework;
using NSubstitute;
using AutoMapper;
using JNBFitness.Application.Mapping;
using JNBFitness.Application.Services.Utilisateur;
using JNBFitness.Infrastructure.Repositories;

namespace JNBFitness.Tests
{
    public class ClientServiceTests
    {
        private IMapper _mapper;
        private IClientRepository _repo;
        private ClientService _service;

        [SetUp]
        public void Setup()
        {
            _mapper = new MapperConfiguration(cfg => cfg.AddProfile(new MappingProfile())).CreateMapper();
            _repo = Substitute.For<IClientRepository>();
            _service = new ClientService(_repo, _mapper);
        }

        [Test]
        public void GetByCodeParrainageAsync_WhenNotFound_ShouldThrow()
        {
            _repo.GetByCodeParrainageAsync("CODE").Returns(Task.FromResult<JNBFitness.Domain.Entities.Client>(default!));
            Assert.ThrowsAsync<Exception>(async () => await _service.GetByCodeParrainageAsync("CODE"));
        }

        [Test]
        public async Task GetAllClientsAsync_ReturnsMappedList()
        {
            // Arrange: liste de clients actifs
            _repo.GetClientsActifsAsync().Returns(new[] {
                new JNBFitness.Domain.Entities.Client { UtilisateurId = 1 },
                new JNBFitness.Domain.Entities.Client { UtilisateurId = 2 }
            });

            // Act
            var list = await _service.GetAllClientsAsync();

            // Assert
            Assert.That(list.Count(), Is.EqualTo(2));
        }
    }
}
