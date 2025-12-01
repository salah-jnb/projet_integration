using System;
using System.Linq;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Linq.Expressions;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.AspNetCore.TestHost;
using Microsoft.Extensions.DependencyInjection;
using JNBFitness.Infrastructure.Repositories;
using JNBFitness.Domain.Entities;
using JNBFitness.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace JNBFitness.IntegrationTests
{
    public class CustomWebApplicationFactory : WebApplicationFactory<Program>
    {
        protected override void ConfigureWebHost(IWebHostBuilder builder)
        {
            builder.ConfigureTestServices(services =>
            {
                var dbContextDescriptor = services.FirstOrDefault(d => d.ServiceType == typeof(DbContextOptions<ApplicationDbContext>));
                if (dbContextDescriptor != null)
                {
                    services.Remove(dbContextDescriptor);
                }
                services.AddDbContext<ApplicationDbContext>(opts =>
                {
                    opts.UseInMemoryDatabase("IntegrationTestsDb");
                    opts.EnableSensitiveDataLogging();
                    opts.EnableDetailedErrors();
                });
                // Authentification de test qui accepte toutes les requêtes
                services.AddAuthentication("Test")
                    .AddScheme<AuthenticationSchemeOptions, TestAuthHandler>("Test", options => { });
                services.PostConfigureAll<AuthenticationOptions>(opts =>
                {
                    opts.DefaultAuthenticateScheme = "Test";
                    opts.DefaultChallengeScheme = "Test";
                });

                // Remplacer IProduitRepository par une implémentation en mémoire
                var descriptor = services.FirstOrDefault(d => d.ServiceType == typeof(IProduitRepository));
                if (descriptor != null) services.Remove(descriptor);
                services.AddSingleton<IProduitRepository>(new InMemoryProduitRepository());
            });
        }
    }

    internal class InMemoryProduitRepository : IProduitRepository
    {
        private readonly List<Produit> _items = new();
        private long _nextId = 1;

        public Task<Produit> GetByIdAsync(long id) => Task.FromResult(_items.FirstOrDefault(x => x.Id == id));
        public Task<IEnumerable<Produit>> GetAllAsync() => Task.FromResult(_items.AsEnumerable());
        public Task<IEnumerable<Produit>> FindAsync(System.Linq.Expressions.Expression<Func<Produit, bool>> predicate)
            => Task.FromResult(_items.AsQueryable().Where(predicate).AsEnumerable());
        public Task<Produit> AddAsync(Produit entity)
        {
            if (entity.Id == 0) entity.Id = _nextId++;
            _items.Add(entity);
            return Task.FromResult(entity);
        }
        public Task UpdateAsync(Produit entity)
        {
            var idx = _items.FindIndex(x => x.Id == entity.Id);
            if (idx >= 0) _items[idx] = entity;
            return Task.CompletedTask;
        }
        public Task DeleteAsync(Produit entity)
        {
            _items.RemoveAll(x => x.Id == entity.Id);
            return Task.CompletedTask;
        }
        public Task<bool> ExistsAsync(long id) => Task.FromResult(_items.Any(x => x.Id == id));
        public Task<int> CountAsync() => Task.FromResult(_items.Count);
        public Task<IEnumerable<Produit>> GetProduitsActifsAsync() => Task.FromResult(_items.Where(x => x.Actif).AsEnumerable());
        public Task<IEnumerable<Produit>> GetProduitsByCategorieAsync(string categorie)
            => Task.FromResult(_items.Where(x => x.Categorie == categorie && x.Actif).AsEnumerable());
    }
}
