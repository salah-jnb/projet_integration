using System.Net;
using System.Net.Http;
using System.Net.Http.Json;
using System.Threading.Tasks;
using Microsoft.Extensions.DependencyInjection;
using NUnit.Framework;

namespace JNBFitness.IntegrationTests
{
    public class TransfertsIntegrationTests
    {
        private CustomWebApplicationFactory _factory;
        private HttpClient _client;

        [SetUp]
        public void Setup()
        {
            _factory = new CustomWebApplicationFactory();
            _client = _factory.CreateClient();
        }

        [Test]
        public async Task CreateTransfert_Success_ReturnsCreated()
        {
            var reg1 = new JNBFitness.Application.DTOs.Auth.RegisterRequestDto { Email = "em@jnb.tn", MotDePasse = "pwd", Nom = "E", Prenom = "M" };
            var reg2 = new JNBFitness.Application.DTOs.Auth.RegisterRequestDto { Email = "rc@jnb.tn", MotDePasse = "pwd", Nom = "R", Prenom = "C" };
            await _client.PostAsJsonAsync("/api/Auth/register", reg1);
            await _client.PostAsJsonAsync("/api/Auth/register", reg2);

            using var scope = _factory.Services.CreateScope();
            var cartes = scope.ServiceProvider.GetRequiredService<JNBFitness.Infrastructure.Repositories.ICarteRepository>();
            var em = await cartes.GetByUtilisateurIdAsync(1);
            var rc = await cartes.GetByUtilisateurIdAsync(2);

            var dto = new JNBFitness.Application.DTOs.Paiement.CreateTransfertDto { EmetteurCarteId = em.Id, RecepteurCarteId = rc.Id, MontantEuro = 1m, Devise = "TND", Motif = "Test" };
            var resp = await _client.PostAsJsonAsync("/api/Transferts", dto);
            Assert.That(resp.StatusCode, Is.EqualTo(HttpStatusCode.Created));
        }

        [Test]
        public async Task CreateTransfert_MontantZero_Returns400()
        {
            var reg1 = new JNBFitness.Application.DTOs.Auth.RegisterRequestDto { Email = "e2@jnb.tn", MotDePasse = "pwd", Nom = "E", Prenom = "M" };
            var reg2 = new JNBFitness.Application.DTOs.Auth.RegisterRequestDto { Email = "r2@jnb.tn", MotDePasse = "pwd", Nom = "R", Prenom = "C" };
            await _client.PostAsJsonAsync("/api/Auth/register", reg1);
            await _client.PostAsJsonAsync("/api/Auth/register", reg2);

            using var scope = _factory.Services.CreateScope();
            var cartes = scope.ServiceProvider.GetRequiredService<JNBFitness.Infrastructure.Repositories.ICarteRepository>();
            var em = await cartes.GetByUtilisateurIdAsync(3);
            var rc = await cartes.GetByUtilisateurIdAsync(4);

            var dto = new JNBFitness.Application.DTOs.Paiement.CreateTransfertDto { EmetteurCarteId = em.Id, RecepteurCarteId = rc.Id, MontantEuro = 0m, Devise = "TND", Motif = "Test" };
            var resp = await _client.PostAsJsonAsync("/api/Transferts", dto);
            Assert.That(resp.StatusCode, Is.EqualTo(HttpStatusCode.BadRequest));
        }
    }
}
