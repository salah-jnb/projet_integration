using System;
using System.Threading.Tasks;
using NUnit.Framework;
using NSubstitute;
using JNBFitness.Application.Services.Auth;
using JNBFitness.Infrastructure.Repositories;
using Microsoft.Extensions.Configuration;

namespace JNBFitness.Tests
{
    public class AuthServiceTests
    {
        private IUtilisateurRepository _utilRepo;
        private IClientRepository _clientRepo;
        private ICarteRepository _carteRepo;
        private IConfiguration _config;
        private AuthService _service;

        [SetUp]
        public void Setup()
        {
            _utilRepo = Substitute.For<IUtilisateurRepository>();
            _clientRepo = Substitute.For<IClientRepository>();
            _carteRepo = Substitute.For<ICarteRepository>();
            _config = Substitute.For<IConfiguration>();
            _config["Jwt:Key"].Returns("test_secret_key_12345678901234567890");
            _config["Jwt:ExpirationInMinutes"].Returns("60");
            _service = new AuthService(_utilRepo, _clientRepo, _carteRepo, _config);
        }

        [Test]
        public void LoginAsync_WhenInvalidPassword_ShouldThrow()
        {
            // Scenario: mauvais mot de passe -> exception
            var user = new JNBFitness.Domain.Entities.Utilisateur { Id = 1, Email = "u@jnb.tn", MotDePasse = Convert.ToBase64String(System.Text.Encoding.UTF8.GetBytes("hash")), Nom = "N", Prenom = "P" };
            _utilRepo.GetByEmailAsync("u@jnb.tn").Returns(user);
            var req = new JNBFitness.Application.DTOs.Auth.LoginRequestDto { Email = "u@jnb.tn", MotDePasse = "wrong" };
            Assert.ThrowsAsync<Exception>(async () => await _service.LoginAsync(req));
        }

        [Test]
        public void RegisterAsync_WhenEmailExists_ShouldThrow()
        {
            // Scenario: email déjà utilisé -> exception
            _utilRepo.EmailExistsAsync("a@b.com").Returns(true);
            var req = new JNBFitness.Application.DTOs.Auth.RegisterRequestDto { Email = "a@b.com", MotDePasse = "x", Nom = "N", Prenom = "P" };
            Assert.ThrowsAsync<Exception>(async () => await _service.RegisterAsync(req));
        }
    }
}

