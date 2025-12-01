using System;
using System.Linq;
using System.Threading.Tasks;
using NUnit.Framework;
using NSubstitute;
using AutoMapper;
using JNBFitness.Application.Mapping;
using JNBFitness.Application.Services.Paiement;
using JNBFitness.Infrastructure.Repositories;

namespace JNBFitness.Tests
{
    public class PaiementServiceTests
    {
        private IMapper _mapper;
        private IPaiementRepository _repo;
        private PaiementService _service;

        [SetUp]
        public void Setup()
        {
            _mapper = new MapperConfiguration(cfg => cfg.AddProfile(new MappingProfile())).CreateMapper();
            _repo = Substitute.For<IPaiementRepository>();
            _service = new PaiementService(_repo, _mapper);
        }

        [Test]
        public async Task CreatePaiementAsync_Success_ReturnsDto()
        {
            // Scenario: création paiement valide
            _repo.AddAsync(Arg.Any<JNBFitness.Domain.Entities.Paiement>()).Returns(ci => (JNBFitness.Domain.Entities.Paiement)ci[0]);
            var dto = new JNBFitness.Application.DTOs.Paiement.CreatePaiementDto { ClientId = 1, AbonnementId = 2, Montant = 10m, MethodePaiement = "CARTE" };
            var res = await _service.CreatePaiementAsync(dto);
            Assert.That(res.Montant, Is.EqualTo(10m));
        }
    }
}

