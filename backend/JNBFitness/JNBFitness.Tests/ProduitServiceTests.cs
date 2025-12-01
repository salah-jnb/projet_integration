using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using NUnit.Framework;
using NSubstitute;
using AutoMapper;
using JNBFitness.Application.Mapping;
using JNBFitness.Application.Services.Produit;
using JNBFitness.Application.DTOs.Produit;
using JNBFitness.Infrastructure.Repositories;
using JNBFitness.Domain.Entities;

namespace JNBFitness.Tests
{
    public class ProduitServiceTests
    {
        private IMapper _mapper;
        private IProduitRepository _repo;
        private ProduitService _service;

        [SetUp]
        public void Setup()
        {
            var config = new MapperConfiguration(cfg => cfg.AddProfile(new MappingProfile()));
            _mapper = config.CreateMapper();
            _repo = Substitute.For<IProduitRepository>();
            _service = new ProduitService(_repo, _mapper);
        }

        [Test]
        public async Task CreateProduitAsync_cree_produit_actif_et_mappe_les_champs()
        {
            var dto = new CreateProduitDto
            {
                Nom = "Whey Pro",
                Description = "Protéine de lactosérum",
                Prix = 3.5m,
                Categorie = "Supplements",
                ImageUrl = null
            };

            _repo.AddAsync(Arg.Any<Produit>()).Returns(ci => (Produit)ci[0]);
            var result = await _service.CreateProduitAsync(dto);
            Assert.That(result, Is.Not.Null);
            Assert.That(result.Actif, Is.True);
            Assert.That(result.Nom, Is.EqualTo(dto.Nom));
        }

        [Test]
        public void UpdateProduitAsync_lance_exception_si_introuvable()
        {
            var update = new UpdateProduitDto { Nom = "X" };
            var ex = Assert.ThrowsAsync<Exception>(async () => await _service.UpdateProduitAsync(999, update));
            Assert.That(ex!.Message, Is.EqualTo("Produit introuvable"));
        }

        [Test]
        public async Task UpdateProduitAsync_met_a_jour_les_champs_optionnels()
        {
            var produit = new Produit { Id = 1, Nom = "Ancien", Description = "Desc", Prix = 10m, Categorie = "Cat", Actif = true };
            _repo.GetByIdAsync(produit.Id).Returns(produit);

            var update = new UpdateProduitDto
            {
                Nom = "Nouveau",
                Description = "Desc2",
                Prix = 12.75m,
                Categorie = "Cat2",
                ImageUrl = "/img/p.png",
                Actif = false
            };

            var updated = await _service.UpdateProduitAsync(produit.Id, update);

            Assert.That(updated.Nom, Is.EqualTo("Nouveau"));
            Assert.That(updated.Description, Is.EqualTo("Desc2"));
            Assert.That(updated.Prix, Is.EqualTo(12.75m));
            Assert.That(updated.Categorie, Is.EqualTo("Cat2"));
            Assert.That(updated.Actif, Is.False);
        }

        [Test]
        public async Task DeleteProduitAsync_supprime_et_retourne_true()
        {
            var produit = new Produit { Id = 2, Nom = "A", Actif = true };
            _repo.GetByIdAsync(produit.Id).Returns(produit);
            var ok = await _service.DeleteProduitAsync(produit.Id);
            Assert.That(ok, Is.True);
        }

        [Test]
        public async Task GetProduitsActifsAsync_retourne_uniquement_actifs()
        {
            _repo.GetProduitsActifsAsync().Returns(new List<Produit> { new Produit { Id = 3, Nom = "Actif", Actif = true } });
            var list = await _service.GetProduitsActifsAsync();
            Assert.That(list.Count(), Is.EqualTo(1));
            Assert.That(list.First().Nom, Is.EqualTo("Actif"));
            Assert.That(list.First().Actif, Is.True);
        }
    }
}
