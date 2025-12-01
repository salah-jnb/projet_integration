using System;
using System.Threading.Tasks;
using NUnit.Framework;
using NSubstitute;
using AutoMapper;
using JNBFitness.Application.Mapping;
using JNBFitness.Application.Services.Article;
using JNBFitness.Infrastructure.Repositories;
using JNBFitness.Application.Services.Notification;

namespace JNBFitness.Tests
{
    public class ArticleServiceTests
    {
        private IMapper _mapper;
        private IArticleRepository _repo;
        private INotificationService _notif;
        private ArticleService _service;

        [SetUp]
        public void Setup()
        {
            _mapper = new MapperConfiguration(cfg => cfg.AddProfile(new MappingProfile())).CreateMapper();
            _repo = Substitute.For<IArticleRepository>();
            _notif = Substitute.For<INotificationService>();
            _service = new ArticleService(_repo, _mapper, _notif);
        }

        [Test]
        public async Task CreateArticleAsync_DefaultStatut_Brouillon()
        {
            // Scenario: pas de statut fourni -> BROUILLON
            _repo.AddAsync(Arg.Any<JNBFitness.Domain.Entities.Article>()).Returns(ci => (JNBFitness.Domain.Entities.Article)ci[0]);
            var dto = new JNBFitness.Application.DTOs.Article.CreateArticleDto { CoachId = 1, Titre = "T", Contenu = "C" };
            var res = await _service.CreateArticleAsync(dto);
            Assert.That(res.Statut, Is.EqualTo("BROUILLON"));
        }

        [Test]
        public void UpdateArticleAsync_WhenNotFound_ShouldThrow()
        {
            // Scenario: article introuvable -> exception
            _repo.GetByIdAsync(9).Returns(Task.FromResult<JNBFitness.Domain.Entities.Article>(default!));
            var upd = new JNBFitness.Application.DTOs.Article.UpdateArticleDto { Titre = "X" };
            Assert.ThrowsAsync<Exception>(async () => await _service.UpdateArticleAsync(9, upd));
        }

        [Test]
        public async Task ValidateArticleAsync_Publier_SendsNotification()
        {
            // Scenario: validation avec publication -> notification
            var article = new JNBFitness.Domain.Entities.Article { Id = 2, CoachId = 7, Titre = "T" };
            _repo.GetByIdAsync(2).Returns(article);
            var dto = new JNBFitness.Application.DTOs.Article.ValidateArticleDto { Publier = true, CommentaireAdmin = "OK" };
            var res = await _service.ValidateArticleAsync(2, dto);
            Assert.That(res.Statut, Is.EqualTo("PUBLIE"));
            await _notif.Received(1).CreateNotificationAsync(Arg.Any<JNBFitness.Application.DTOs.Notification.CreateNotificationDto>());
        }
    }
}

