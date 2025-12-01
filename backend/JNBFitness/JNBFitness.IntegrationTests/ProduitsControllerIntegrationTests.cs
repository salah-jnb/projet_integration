using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text;
using NUnit.Framework;
using System.Threading.Tasks;

namespace JNBFitness.IntegrationTests
{
    public class ProduitsControllerIntegrationTests
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
        public async Task Post_Produit_Multipart_PrixDecimal_Success()
        {
            // Scenario: création avec prix décimal "3.5" et contenus multipart
            var content = new MultipartFormDataContent();
            content.Add(new StringContent("Whey Pro"), "Nom");
            content.Add(new StringContent("Protéine"), "Description");
            content.Add(new StringContent("Supplements"), "Categorie");
            content.Add(new StringContent("3.5"), "Prix");

            var resp = await _client.PostAsync("/api/Produits", content);
            Assert.That(resp.StatusCode, Is.EqualTo(HttpStatusCode.Created));
            var dto = await resp.Content.ReadFromJsonAsync<JNBFitness.Application.DTOs.Produit.ProduitDto>();
            Assert.That(dto!.Nom, Is.EqualTo("Whey Pro"));
        }

        [Test]
        public async Task Post_Produit_PrixInvalide_Returns400()
        {
            // Scenario: prix invalide -> 400
            var content = new MultipartFormDataContent();
            content.Add(new StringContent("Produit"), "Nom");
            content.Add(new StringContent("abc"), "Prix");
            var resp = await _client.PostAsync("/api/Produits", content);
            Assert.That(resp.StatusCode, Is.EqualTo(HttpStatusCode.BadRequest));
        }

        [Test]
        public async Task Post_Produit_NomManquant_Returns400()
        {
            // Scenario: nom manquant -> 400
            var content = new MultipartFormDataContent();
            content.Add(new StringContent("3.5"), "Prix");
            var resp = await _client.PostAsync("/api/Produits", content);
            Assert.That(resp.StatusCode, Is.EqualTo(HttpStatusCode.BadRequest));
        }
    }
}
