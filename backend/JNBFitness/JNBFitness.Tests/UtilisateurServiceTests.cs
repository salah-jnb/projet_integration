using System;
using System.Threading.Tasks;
using NUnit.Framework;
using NSubstitute;
using AutoMapper;
using JNBFitness.Application.Mapping;
using JNBFitness.Application.Services.Utilisateur;
using JNBFitness.Infrastructure.Repositories;
using JNBFitness.Domain.Entities;
using JNBFitness.Domain.Enums;
using JNBFitness.Application.DTOs.Utilisateur;

namespace JNBFitness.Tests
{
    public class UtilisateurServiceTests
    {
        private IMapper _mapper;
        private IUtilisateurRepository _utilRepo;
        private IClientRepository _clientRepo;
        private ICoachRepository _coachRepo;
        private ICarteRepository _carteRepo;
        private UtilisateurService _service;

        [SetUp]
        public void Setup()
        {
            _mapper = new MapperConfiguration(cfg => cfg.AddProfile(new MappingProfile())).CreateMapper();
            _utilRepo = Substitute.For<IUtilisateurRepository>();
            _clientRepo = Substitute.For<IClientRepository>();
            _coachRepo = Substitute.For<ICoachRepository>();
            _carteRepo = Substitute.For<ICarteRepository>();
            _service = new UtilisateurService(_utilRepo, _clientRepo, _coachRepo, _carteRepo, _mapper);
        }

        [Test]
        public void CreateUserAsync_WhenEmailExists_ShouldThrow()
        {
            var dto = new CreateUserDto { Email = "a@b.com", MotDePasse = "x", Nom = "n", Prenom = "p", TypeUtilisateur = JNBFitness.Domain.Enums.TypeUtilisateur.CLIENT };
            _utilRepo.EmailExistsAsync(dto.Email).Returns(true);
            Assert.ThrowsAsync<Exception>(async () => await _service.CreateUserAsync(dto));
        }

        [Test]
        public async Task CreateUserAsync_Client_Success_CreatesCardAndClient()
        {
            // Arrange: email libre, dépôts retournent entités persistées
            var dto = new CreateUserDto { Email = "new@jnb.tn", MotDePasse = "pwd", Nom = "Nom", Prenom = "Prenom", TypeUtilisateur = TypeUtilisateur.CLIENT };
            _utilRepo.EmailExistsAsync(dto.Email).Returns(false);
            _utilRepo.AddAsync(Arg.Any<Utilisateur>()).Returns(ci => { var u = (Utilisateur)ci[0]; u.Id = 42; return u; });
            _carteRepo.AddAsync(Arg.Any<Carte>()).Returns(ci => (Carte)ci[0]);
            _clientRepo.AddAsync(Arg.Any<Client>()).Returns(ci => (Client)ci[0]);

            // Act: création utilisateur client
            var result = await _service.CreateUserAsync(dto);

            // Assert: utilisateur créé et interactions effectuées
            Assert.That(result.Email, Is.EqualTo(dto.Email));
            _carteRepo.Received(1).AddAsync(Arg.Is<Carte>(c => c.UtilisateurId == 42 && c.Devise == "TND"));
            _clientRepo.Received(1).AddAsync(Arg.Is<Client>(c => c.UtilisateurId == 42));
        }

        [Test]
        public async Task ChangeStatutAsync_ValidEnum_UpdatesStatut()
        {
            // Arrange: utilisateur existant
            var user = new Utilisateur { Id = 7, Email = "u@jnb.tn", Statut = StatutUtilisateur.EN_ATTENTE, MotDePasse = "x", Nom = "N", Prenom = "P" };
            _utilRepo.GetByIdAsync(user.Id).Returns(user);

            // Act: changement de statut
            var dto = await _service.ChangeStatutAsync(user.Id, "ACTIF");

            // Assert: statut mis à jour et update appelé
            Assert.That(dto.Statut, Is.EqualTo("ACTIF"));
            _utilRepo.Received(1).UpdateAsync(Arg.Is<Utilisateur>(u => u.Statut == StatutUtilisateur.ACTIF));
        }
    }
}
