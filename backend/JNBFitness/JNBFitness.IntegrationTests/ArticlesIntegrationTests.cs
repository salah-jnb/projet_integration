using System.Net;
using System.Net.Http;
using System.Net.Http.Json;
using System.Threading.Tasks;
using NUnit.Framework;

namespace JNBFitness.IntegrationTests
{
    public class ArticlesIntegrationTests
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
        public async Task CreateArticle_Brouillon_ReturnsCreated()
        {
            var content = new MultipartFormDataContent();
            content.Add(new StringContent("1"), "coachId");
            content.Add(new StringContent("Titre"), "titre");
            content.Add(new StringContent("Contenu"), "contenu");
            var resp = await _client.PostAsync("/api/Articles", content);
            Assert.That(resp.StatusCode, Is.EqualTo(HttpStatusCode.Created));
        }

        [Test]
        public async Task ValidateArticle_Publier_ReturnsOk()
        {
            var content = new MultipartFormDataContent();
            content.Add(new StringContent("2"), "coachId");
            content.Add(new StringContent("Titre"), "titre");
            content.Add(new StringContent("Contenu"), "contenu");
            var create = await _client.PostAsync("/api/Articles", content);
            var dto = await create.Content.ReadFromJsonAsync<JNBFitness.Application.DTOs.Article.ArticleDto>();

            var body = new JNBFitness.Application.DTOs.Article.ValidateArticleDto { Publier = true, CommentaireAdmin = "OK" };
            var resp = await _client.PutAsJsonAsync($"/api/Articles/{dto!.Id}/valider", body);
            Assert.That(resp.StatusCode, Is.EqualTo(HttpStatusCode.OK));
        }
    }
}
