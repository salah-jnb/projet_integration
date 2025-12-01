using System;
using System.Threading.Tasks;
using NUnit.Framework;
using NSubstitute;
using AutoMapper;
using JNBFitness.Application.Mapping;
using JNBFitness.Application.Services.Notification;
using JNBFitness.Infrastructure.Repositories;
using JNBFitness.Application.Services.Communication;

namespace JNBFitness.Tests
{
    public class NotificationServiceTests
    {
        private IMapper _mapper;
        private INotificationRepository _repo;
        private IEmailService _email;
        private IUtilisateurRepository _userRepo;
        private NotificationService _service;

        [SetUp]
        public void Setup()
        {
            _mapper = new MapperConfiguration(cfg => cfg.AddProfile(new MappingProfile())).CreateMapper();
            _repo = Substitute.For<INotificationRepository>();
            _email = Substitute.For<IEmailService>();
            _userRepo = Substitute.For<IUtilisateurRepository>();
            _service = new NotificationService(_repo, _mapper, _email, _userRepo);
        }

        [Test]
        public async Task CreateNotificationAsync_WithEmail_SendsEmail()
        {
            // Scenario: destinataire avec email -> envoi email
            _repo.AddAsync(Arg.Any<JNBFitness.Domain.Entities.Notification>()).Returns(ci => (JNBFitness.Domain.Entities.Notification)ci[0]);
            _userRepo.GetByIdAsync(10).Returns(new JNBFitness.Domain.Entities.Utilisateur { Id = 10, Email = "u@jnb.tn", Nom = "N", Prenom = "P", MotDePasse = "x" });
            var dto = new JNBFitness.Application.DTOs.Notification.CreateNotificationDto { DestinataireId = 10, Titre = "T", Message = "M", Type = "INFO" };
            var res = await _service.CreateNotificationAsync(dto);
            await _email.Received(1).SendAsync("u@jnb.tn", Arg.Any<string>(), Arg.Any<string>());
            await _repo.Received(1).UpdateAsync(Arg.Any<JNBFitness.Domain.Entities.Notification>());
        }

        [Test]
        public void MarquerCommeLueAsync_WhenNotFound_ShouldThrow()
        {
            // Scenario: notification introuvable -> exception
            _repo.GetByIdAsync(99).Returns(Task.FromResult<JNBFitness.Domain.Entities.Notification>(default!));
            Assert.ThrowsAsync<Exception>(async () => await _service.MarquerCommeLueAsync(99));
        }
    }
}

