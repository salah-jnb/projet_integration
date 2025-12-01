using System.Net;
using System.Net.Http;
using System.Net.Http.Json;
using System.Threading.Tasks;
using NUnit.Framework;

namespace JNBFitness.IntegrationTests
{
    public class AuthIntegrationTests
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
        public async Task Register_Success_ReturnsCreated()
        {
            var body = new JNBFitness.Application.DTOs.Auth.RegisterRequestDto
            {
                Email = "int-test@jnb.tn",
                MotDePasse = "pwd",
                Nom = "Nom",
                Prenom = "Prenom"
            };
            var resp = await _client.PostAsJsonAsync("/api/Auth/register", body);
            Assert.That(resp.StatusCode, Is.EqualTo(HttpStatusCode.Created));
        }

        [Test]
        public async Task Login_InvalidPassword_Returns401()
        {
            var reg = new JNBFitness.Application.DTOs.Auth.RegisterRequestDto
            {
                Email = "login@jnb.tn",
                MotDePasse = "right",
                Nom = "N",
                Prenom = "P"
            };
            await _client.PostAsJsonAsync("/api/Auth/register", reg);

            var login = new JNBFitness.Application.DTOs.Auth.LoginRequestDto
            {
                Email = "login@jnb.tn",
                MotDePasse = "wrong"
            };
            var resp = await _client.PostAsJsonAsync("/api/Auth/login", login);
            Assert.That(resp.StatusCode, Is.EqualTo(HttpStatusCode.Unauthorized));
        }
    }
}
